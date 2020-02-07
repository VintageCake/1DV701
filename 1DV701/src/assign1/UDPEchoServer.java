package assign1;
/*
  UDPEchoServer.java
  Author: Love Samulesson ls223qx@student.lnu.se
  Date: 2020-02-01
  
  A UDP server that sends back whatever is received by using the socket api.
  This program uses the abstract socket created for VG task 2.
  
*/

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.PortUnreachableException;
import java.net.SocketException;

import assign1.abstractions.UDPServerSocket;

public class UDPEchoServer {
	public static final int MYPORT = 4950;

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Need to define buffer size as argument!");
			System.exit(1);
		}

		// Buffer creation and input validation
		byte[] buf = null;
		try {
			int testBuffer = verifyBuffer(args[0]);
			buf = new byte[testBuffer];
		}
		catch (NumberFormatException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}

		/* Create socket */
		UDPServerSocket serverSocket = null;
		try {
			serverSocket = new UDPServerSocket(MYPORT);
		}
		catch (SocketException e) {
			System.err.println("Socket creation error: " + e.getMessage());
			System.exit(1);
		}

		System.out.println(java.time.LocalDateTime.now() + " Server started... listening on port: " + MYPORT);

		// TODO - Check how the server breaks when message received is larger than the buffer.
		// Main server loop
		@SuppressWarnings("unused") int counter = 0;
		while (true) {
			try {
				/* Create datagram packet for receiving message */
				DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);

				/* Receiving message */
				serverSocket.receive(receivePacket);

				/* Create datagram packet for sending message */
				DatagramPacket sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(),
						receivePacket.getAddress(), receivePacket.getPort());

				/* Send message */
				serverSocket.send(sendPacket);
				// Debug messages really only kill performance, not needed for every packet received.
				//System.out.printf(java.time.LocalTime.now() + " " + counter++ + " UDP echo request from %s", receivePacket.getAddress().getHostAddress());
				//System.out.printf(" using port %d%n", receivePacket.getPort());
			}
			catch (PortUnreachableException e) {
				System.err.println("Echo not delivered: " + e.getMessage());
			}
			catch (IOException e) {
				System.err.println("General error: " + e.getMessage());
				System.exit(1);
			}
		}
	}

	// Throws exception when buffer argument is invalid (not int) or out of range
	private static int verifyBuffer(String buffer) {
		int testBuffer = Integer.parseInt(buffer);
		if (testBuffer < 1 || testBuffer > 100000) {
			throw new NumberFormatException("Invalid buffer size, please use range between 1-100,000");
		}
		return testBuffer;
	}

}
