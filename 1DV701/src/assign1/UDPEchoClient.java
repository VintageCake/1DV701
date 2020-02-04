package assign1;

/*
  UDPEchoClient.java
  Author: Love Samuelsson ls223qx@student.lnu.se
  Date: 2020-01-30
  
  A simple echo client made in java, using sockets for the networking portion.
  Made by essentially modifying a pre-existing program that did not contain error handling.
  
  This program uses the abstract socket made by me for the VG task 2.
*/

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import assign1.abstractions.UDPClientSocket;

public class UDPEchoClient {
	public static final int MYPORT = 0; // In sockets, 0 means to use a client port in the DYNAMIC (private) port range.
	public static final String MSG = "An Echo message!";
	public static final int TIMEOUT_MS = 50; // Sets message timeout

	public static void main(String[] args) {
		if (args.length != 4) { // Check that we get 4 arguments in
			System.err.println("Usage: Destination address, Port, BufferSize (in bytes), sendrate");
			System.exit(1);
		}
		
		/* Can be used to define custom message length, for testing different message sizes and layer 3 fragmentation
		 * 
		String MSG = "";
		int defLength = 1400;
		for (int i = 0; i < defLength; i++) {
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
			for (int i = 0; i < ipSplit.length; i++) {
				int test = Integer.parseInt(ipSplit[i]);
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
		UDPClientSocket clientSocket = null;
		try {
			// Uses the new abstract implementation, (IP, DESTINATION PORT, SOURCE PORT)
			clientSocket = new UDPClientSocket(args[0], portNumber, MYPORT);
			clientSocket.setTimeout(TIMEOUT_MS); // Set max wait for a .receive().
		}
		catch (SocketException e1) {
			e1.printStackTrace();
			System.err.println("\nSocket binding failed");
			System.exit(1);
		}

		/* Create datagram packet for sending message */
		DatagramPacket sendPacket = new DatagramPacket(MSG.getBytes(), MSG.length(), clientSocket.getDestination());

		/* Create datagram packet for receiving echoed message */
		DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);

		// nasty nested do-while
		do {
			long end = System.currentTimeMillis() + 1000; // Timer for 1s
			int packetsShipped = 0;
			int failures = 0;

			do {
				try {
					clientSocket.send(sendPacket);
					clientSocket.receive(receivePacket);
				}
				catch (PortUnreachableException e) { // Port Unreachable seems to either not work with DatagramSocket, or my firewall is blocking ICMP.
					System.err.println("\n Port unreachable, server no longer accepting packets");
					System.exit(1);
				}
				catch (IllegalArgumentException e) { // Basically impossible to ever reach this, would require someone to edit the dPacket dest addr in debug mode.
					System.err.println("\n Tried to send malformed packet, destination address invalid");
					System.exit(1);
				}
				catch (SocketTimeoutException e) { // .receive() will throw timeout when no response is heard for the user defined length of TIMEOUT_MS
					failures++;
				}
				catch (IOException e) {
					System.err.println("\n General error");
					System.exit(1);
				}

				/* Compare sent and received message */
				String receivedString = new String(receivePacket.getData(), receivePacket.getOffset(),
						receivePacket.getLength());
				
				// Console messages removed to improve performance, refer to 2.2.1 in the report.
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
