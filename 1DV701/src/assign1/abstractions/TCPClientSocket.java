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

	public TCPClientSocket(String destAddress, int destPort, int sourcePort) throws IOException {
		super(destAddress, destPort, sourcePort);
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

	// Read the whole buffer or until buffer length is hit, returns int corresponding to amount of bytes read from buffer.
	public Integer read(byte[] buffer) throws IOException {
		int read = 0;
		int length;
		if ((length = in.available()) > 0) {
			if (length > buffer.length) { // Special case handling, when input stream had more bytes than array size
				length = buffer.length;
			}
			read = in.read(buffer, 0, length);
		}
		return read;
	}

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

	public Socket getSocket() {
		return tcpCSocket;
	} // If special socket methods not abstracted away is needed

	public boolean isClosed() {
		return tcpCSocket.isClosed();
	}
}
