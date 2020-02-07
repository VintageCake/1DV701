package assign1.abstractions;

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
