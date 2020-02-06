package assign1.abstractions;
/*
  UDPServerSocket.java
  Author: Love Samulesson ls223qx@student.lnu.se
  Date: 2020-01-31
  
  An extension of the abstract socket that provides an implementation of a UDP server socket.
  
*/

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPServerSocket extends AbstractSocket {
	private DatagramSocket socket;

	// Calls super which creates an InetSocketAddress containing the server bind, then creates the socket itself using that constructed InetSocketAddress
	public UDPServerSocket(int sourcePort) throws SocketException {
		super(sourcePort);
		socket = new DatagramSocket(this.getSource());
	}

	// Methods below just provide abstraction for methods in the socket itself.
	@Override
	public void close() {
		socket.close();
	}

	public void send(DatagramPacket s) throws IOException {
		socket.send(s);
	}

	public void receive(DatagramPacket rec) throws IOException {
		socket.receive(rec);
	}

	@Override
	public boolean isBound() {
		return socket.isBound();
	}

	@Override
	public void setTimeout(int ms) throws SocketException {
		socket.setSoTimeout(ms);
	}

	public DatagramSocket getSocket() {
		return socket;
	}

	@Override
	boolean isClosed() {
		return socket.isClosed();
	}

}
