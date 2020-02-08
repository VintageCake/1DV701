package assign1.abstractions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public class TCPClientSocket extends AbstractSocket {
	private Socket tcpCSocket = null;
	private OutputStream out = null;
	private InputStream in = null;

	// Main constructor
	public TCPClientSocket(String destAddress, int destPort, int sourcePort) throws IOException {
		super(destAddress, destPort, sourcePort);
		// Initializes destination and source InetSocketAddresses
	}

	public TCPClientSocket(Socket s) { // Can put a socket already in use straight into this abstraction layer instead of creating a new socket.
		super(s.getInetAddress().getHostAddress(), s.getPort(), s.getLocalPort());
		tcpCSocket = s;
	}

	// Creates the socket and connects to the destination.
	public void create() throws IOException {
		tcpCSocket = new Socket(this.getDestination().getAddress(), this.getDestination().getPort());
	}

	// Sets up both streams, throws an IOException if socket has already been closed/disconnected for some reason.
	public void setupIO() throws IOException {
		out = tcpCSocket.getOutputStream();
		in = tcpCSocket.getInputStream();
	}

	public void write(byte[] bytes, int length) throws IOException {
		out.write(bytes, 0, length);
	}

	/* Shenanigans when using read()
	Why does this break when using it for my server? Why can't i just ask in.available and then simply read the amount of bytes given?
	Because the program doesn't correctly detect a TCP FIN/ACK or RST when doing that - you HAVE to test the connection with a read.

	Whenever a socket gets closed by a RST or FIN/ACK, the stream really doesn't know it has been exhausted.
	It only knows when you try to READ from it. Then it will flag EOF and return -1 if you are using read().
	Furthermore, SocketException is only thrown on a write operation.
	Even more strange, it's only thrown after TWO writes. What??

	I used the following approach in a PREVIOUS iteration of this program, read -1 and try to write -1 to the output stream until it threw a socket exception. That works, but is ugly.

	My solution? Attempt to read 1 byte when the amount of bytes in the TCP buffer is currently 0.
	This puts the program into a blocking operation while also ensuring that an input stream going EOF returns -1 to higher levels of my program.

	 */
	// Read the whole buffer or until buffer length is hit, returns int corresponding to amount of bytes read from buffer.
	public Integer read(byte[] buffer) throws IOException {
		int read = 0;
		int bytesToRead = 1;

		read = in.read(buffer, 0, bytesToRead); // attempt to read 1 byte
		if (read == -1) { // If stream is "end of file", connection has been killed.
			throw new SocketException("Connection was terminated by host");
		}

		bytesToRead = in.available();
		if (bytesToRead > 0) {
			if (bytesToRead > buffer.length-1) { // Special case handling, when input stream had more bytes than array size
				bytesToRead = buffer.length-1;
			}
		}
		else {
			bytesToRead = 0;
		}
		read += in.read(buffer, 1, bytesToRead); // read the rest of the bytes available, starting from index 1 in the buffer
		return read;

	}
	/*
	This is a very interesting method, it simply takes whatever is present in the input stream and shoves it into the output stream.
	Perfect! But... it doesn't throw an exception when using programs like netcat, that sends a FIN/ACK instead of an RST.
	This means that the connection will again just be kinda left hanging open, not great. Unusable for the tasks, also only first implemented in java 9.

	public void echo() throws IOException {
		in.transferTo(out);
	}
	*/

	public boolean hasData() throws IOException {
		return (in.available() > 0);
	}

	@Override
	public void close() throws IOException {
		tcpCSocket.close();
	}

	public boolean isBound() {
		return tcpCSocket.isBound();
	}

	@Override
	public void setTimeout(int ms) throws SocketException {
		tcpCSocket.setSoTimeout(ms);

	}

	// If special socket methods not abstracted away is needed
	public Socket getSocket() {
		return tcpCSocket;
	}

	public boolean isClosed() {
		return tcpCSocket.isClosed();
	}
}
