package assign1.abstractions;
/*
  UDPClientSocket.java
  Author: Love Samulesson ls223qx@student.lnu.se
  Date: 2020-01-31
  
  An extension of the abstract socket that provides an implementation of a UDP client socket.
  
*/

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPClientSocket extends AbstractSocket {
	private DatagramSocket socket;

	// Constructor that builds using the constructor present in AbstractSocket.
	// Creates a destination and source address with ports, providing an easy way to extract source and destination.
	public UDPClientSocket(String address, int destPort, int sourcePort) throws SocketException {
		super(address, destPort, sourcePort);
		socket = new DatagramSocket(this.getSource());
		socket.connect(this.getDestination());
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

	// Returns the socket itself
	public DatagramSocket getSocket() {
		return socket;
	}

	@Override
	boolean isClosed() {
		return socket.isClosed();
	}

}
