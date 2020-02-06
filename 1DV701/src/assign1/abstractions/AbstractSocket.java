package assign1.abstractions;
/*
  AbstractSocket.java
  Author: Love Samulesson ls223qx@student.lnu.se
  Date: 2020-01-31
  
  An abstract class that provides some useful variables and methods that are used in both TCP and UDP sockets.
  
*/

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;

public abstract class AbstractSocket {
	private InetSocketAddress destFullAddress = null;
	private InetSocketAddress sourceFullAddress = null;

	// Super constructor that establishes destination and source
	AbstractSocket(String destIp, int destPort, int sourcePort) {
		destFullAddress = new InetSocketAddress(destIp, destPort);
		sourceFullAddress = new InetSocketAddress(sourcePort);
	}

	// Super constructor that establishes source only, used for server sockets
	AbstractSocket(int sourcePort) {
		sourceFullAddress = new InetSocketAddress(sourcePort);
	}

	// Some abstract functions that are common among the types of sockets we are using, exceptions are thrown into the program calling the methods.
	abstract boolean isBound();

	abstract void setTimeout(int ms) throws SocketException;

	abstract void close() throws IOException;

	abstract boolean isClosed();

	// Returns the destination address if we are using a client socket, null if using a server socket.
	public InetSocketAddress getDestination() {
		return destFullAddress;
	}

	// Returns the source address of the socket, typically wildcard_address:port
	public InetSocketAddress getSource() {
		return sourceFullAddress;
	}
}
