package assign1.old;

/*
  UDPEchoServer.java
  Author: Love Samulesson ls223qx@student.lnu.se
  Date: 2020-01-30
  
  A simple UDP server that sends back whatever is received by using the socket api.
  This is the old UDPEchoServer that does NOT use my own abstract implementation of Sockets.
  
*/

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.net.SocketAddress;
import java.net.SocketException;

public class UDPEchoServerOld {
	public static final int MYPORT = 4950;

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Need to define buffer size as argument!");
			System.exit(1);
		}

		byte[] buf = null;
		try {
			int testBuffer = Integer.parseInt(args[0]);
			if (testBuffer < 1 || testBuffer > 100000) {
				throw new NumberFormatException("Invalid buffer size, please use range between 1-100,000");
			}
			buf = new byte[testBuffer];
		}
		catch (NumberFormatException e1) {
			System.err.println(e1.getMessage());
			System.exit(1);
		}

		/* Create socket */
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket(null);

			/* Create local bind point */
			SocketAddress localBindPoint = new InetSocketAddress(MYPORT);
			socket.bind(localBindPoint);
		}
		catch (SocketException e) {
			System.err.println("Socket creation error, port likely already in use");
			e.printStackTrace();
			System.exit(1);
		}

		System.out.println(java.time.LocalDateTime.now() + " Server started... listening on port " + MYPORT);
		int counter = 0;
		while (true) {
			try {
				/* Create datagram packet for receiving message */
				DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);

				/* Receiving message */
				socket.receive(receivePacket);

				/* Create datagram packet for sending message */
				DatagramPacket sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(),
						receivePacket.getAddress(), receivePacket.getPort());

				/* Send message */
				socket.send(sendPacket);
				//System.out.printf(java.time.LocalTime.now() + " " + counter++ + " UDP echo request from %s", receivePacket.getAddress().getHostAddress());
				//System.out.printf(" using port %d%n", receivePacket.getPort());
			}
			catch (PortUnreachableException e) {
				System.err.println("Echo not delivered, port is unreachable");
				System.exit(1);
			}
			catch (IOException e) {
				System.err.println("General error");
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}