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
	public void setupIO() throws IOException {
		out = tcpCSocket.getOutputStream();
		in = tcpCSocket.getInputStream();
	}
	
	public void write(byte[] bytes) throws IOException {
		out.write(bytes);
	}
	// Read until buffer empty or buffer length is hit, then return a byte with exact length as message received.¨
	// TODO - Fix this to read n-bytes of the buffer that is equivalent to the amount of bytes available, which can be done by using in.available().
	// You should not consume unneeded amount of bytes from the memory!! Use the same buffer as defined in the beginning.
	public byte[] read(int bufferLength) throws IOException {
		byte[] temp = new byte[bufferLength];
		int i = 0;
		do { // Stay until buffer full or all data handled
			temp[i++] = (byte) in.read();
			if (i >= bufferLength) {
				break;
			}
		}
		while (in.available() > 0);
		byte[] formattedTmp = new byte[i];
		System.arraycopy(temp, 0, formattedTmp, 0, i);
		return formattedTmp;
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
	}
	public boolean isClosed() {
		return tcpCSocket.isClosed();
	}
}
