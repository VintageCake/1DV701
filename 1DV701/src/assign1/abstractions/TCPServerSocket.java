package assign1.abstractions;
/*
  TCPServerSocket.java
  Author: Love Samuelsson ls223qx@student.lnu.se
  Date: 2020-02-07

  An extension of the abstract socket that provides an implementation of a TCP server socket.

*/
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class TCPServerSocket extends AbstractSocket {
	ServerSocket s;

	public TCPServerSocket(int sourcePort) throws IOException {
		super(sourcePort);
		s = new ServerSocket(sourcePort);
	}

	public Socket accept() throws IOException {
		return s.accept();
	}

	@Override
	public boolean isBound() {
		return s.isBound();
	}

	@Override
	public void setTimeout(int ms) throws SocketException {
		s.setSoTimeout(ms);
	}

	@Override
	public void close() throws IOException {
		s.close();
	}

	@Override
	public boolean isClosed() {
		return s.isClosed();
	}
}
