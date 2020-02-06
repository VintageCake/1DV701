package assign1.old;

/*
  UDPEchoClient.java
  Author: Love Samuelsson ls223qx@student.lnu.se
  Date: 2020-01-30
  
  A simple echo client made in java, using sockets for the networking portion.
  Made by essentially modifying a pre-existing program that did not contain error handling.
  
  This is the old UDPEchoClient that does NOT use my own abstract implementation of Sockets.
*/

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class UDPEchoClientOld {
	public static final int MYPORT = 0; // In sockets, 0 means to use a client port in the DYNAMIC (private) port range.
	public static final String MSG = "An Echo message!";
	public static final int TIMEOUT_MS = 50; // Sets message timeout

	public static void main(String[] args) {
		if (args.length != 4) {
			System.err.println("Usage: Destination address, Port, BufferSize (in bytes), sendrate");
			System.exit(1);
		}
		
		/* Can be used to define custom message length with a repeating character
		String MSG = "";
		for (int i = 0; i < 1400; i++) {
			MSG = MSG.concat("A");
		}
		*/

		// Initialise variables for port number, buffer, send rate.
		Integer portNumber = null;
		byte[] buf = null;
		Integer sendRate = null;

		// large try-catch block that validates and sanity checks all input arguments
		try {
			String ip = args[0];
			String[] ipSplit = ip.split("\\.");
			if (ipSplit.length < 4) {
				throw new NumberFormatException("Invalid IP, use format: 0-255.0-255.0-255.0-255");
			}
			for (String s : ipSplit) {
				int test = Integer.parseInt(s);
				if (test > 255 || test < 0) {
					throw new NumberFormatException("Invalid IP, use format: 0-255.0-255.0-255.0-255");
				}
			}

			portNumber = Integer.parseInt(args[1]);
			if (portNumber < 0 || portNumber > ((int) (Math.pow(2, 16)) - 1)) {
				throw new NumberFormatException("Invalid port range, range has to be integer 0-65535");
			}

			int testBuffer = Integer.parseInt(args[2]);
			if (testBuffer < 1 || testBuffer > 100000) {
				throw new NumberFormatException("Invalid buffer size, please use range between 1-100,000");
			}
			buf = new byte[testBuffer];

			sendRate = Integer.parseInt(args[3]);
			if (sendRate < 0 || sendRate > 50000) {
				throw new NumberFormatException("Send rate out of range, use 0-50,000");
			}
		}
		catch (NumberFormatException e) {
			System.err.println("Invalid argument found:");
			System.err.println(e.getMessage());

			System.err.println("Exiting...");
			System.exit(1);
		}

		// Handles the case where the argument for message rate is 0.
		// Sets boolean to let program know we want to terminate after one message.
		boolean oneTime = false;
		if (sendRate == 0) {
			sendRate = 1;
			oneTime = true;
		}

		/* Create socket */
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket(null);

			/* Create local endpoint using bind() */
			SocketAddress localBindPoint = new InetSocketAddress(MYPORT);
			socket.bind(localBindPoint); // Can throw SocketException if port already bound
			socket.setSoTimeout(TIMEOUT_MS); // Packet timeout timer configuration, socket.receive() throws on timeout.
		}
		catch (SocketException e) {
			System.err.println("Socket binding failed: " + e.getMessage());
			System.exit(1);
		}

		/* Create remote endpoint */
		SocketAddress remoteBindPoint = new InetSocketAddress(args[0], portNumber);

		/* Create datagram packet for sending message */
		DatagramPacket sendPacket = new DatagramPacket(MSG.getBytes(), MSG.length(), remoteBindPoint);

		/* Create datagram packet for receiving echoed message */
		DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);

		// nasty nested do-while
		do {
			long end = System.currentTimeMillis() + 1000;
			int packetsShipped = 0;
			int failures = 0;

			do {
				try {
					socket.send(sendPacket);
					socket.receive(receivePacket);
				}
				catch (PortUnreachableException e) { // Port Unreachable seems to either not work with DatagramSocket, or my firewall is blocking ICMP.
					System.err.println("\n Port unreachable, server no longer accepting packets");
					System.exit(1);
				}
				catch (IllegalArgumentException e) {
					System.err.println("\n Malformed paket, wrong destination address");
					System.exit(1);
				}
				catch (SocketTimeoutException e) {
					//System.err.println(
					//		"Response wait time has timed out (" + TIMEOUT_MS + "ms), server did not respond in time");
					//System.exit(1);
					failures++;
				}
				catch (IOException e) {
					System.err.println("\n General error");
					System.exit(1);
				}

				/* Compare sent and received message */
				String receivedString = new String(receivePacket.getData(), receivePacket.getOffset(),
						receivePacket.getLength());

				// Console messages removed to improve performance
				if (receivedString.compareTo(MSG) == 0) {
					//System.out.printf("%d bytes sent and received%n", receivePacket.getLength());
					packetsShipped++;
				}
				else {
					//System.out.printf("Sent and received msg not equal!%n");
					failures++;
				}
			}
			while (!oneTime && System.currentTimeMillis() < end && packetsShipped < sendRate);

			System.out.println("------");
			System.out.println("Successfully echoed " + packetsShipped + " out of " + sendRate + " messages");
			System.out.println("Malformed packets or timeouts: " + failures);
			System.out.println("------");
		}
		while (!oneTime);
	}
}