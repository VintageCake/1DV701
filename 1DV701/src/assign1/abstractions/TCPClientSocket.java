package assign1.abstractions;
/*
  TCPClientSocket.java
  Author: Love Samulesson ls223qx@student.lnu.se
  Date: 2020-01-31
  
  An extension of the abstract socket that provides an implementation of a TCP client socket.
  
*/
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;

public class TCPClientSocket extends AbstractSocket {
	private Socket tcpCSocket = null;
	private DataOutputStream out = null;
	private BufferedReader in = null;
	
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
		out = new DataOutputStream(tcpCSocket.getOutputStream());
		in = new BufferedReader(new InputStreamReader(tcpCSocket.getInputStream()));
	}
	
	public void write(byte[] bytes) throws IOException {
		out.write(bytes);
	}
	public byte[] read(int bufferLength) throws IOException {
		byte[] temp = new byte[bufferLength];
		int i = 0;
		do { // Stay until buffer full or all data handled
			temp[i++] = (byte) in.read();
			if (i >= bufferLength) {
				break;
			}
		}
		while (in.ready());
		byte[] formattedTmp = new byte[i];
		System.arraycopy(temp, 0, formattedTmp, 0, i);
		return formattedTmp;
	}
	public boolean hasData() throws IOException {
		return in.ready();
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
