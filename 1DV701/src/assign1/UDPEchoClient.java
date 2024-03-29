package assign1;

/*
  UDPEchoClient.java
  Author: Love Samuelsson ls223qx@student.lnu.se
  Date: 2020-01-30
  
  A simple echo client made in java, using sockets for the networking portion.
  Made by essentially modifying a pre-existing program that did not contain error handling.
  
  This program uses the abstract socket made by me for the VG task 2.
*/

import assign1.abstractions.UDPClientSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Scanner;

public class UDPEchoClient {
	public static final int MYPORT = 0; // In sockets, 0 means to use a client port in the DYNAMIC (private) port range.
	public static final int TIMEOUT_MS = 50; // Sets message timeout
	public static String MSG = "An Echo message!";

	public static void main(String[] args) {
		if (args.length != 4) { // Check that we get 4 arguments in
			System.err.println("Usage: Destination address, Port, BufferSize (in bytes), sendrate");
			System.exit(1);
		}

		//Can be used to define custom message length, for testing different message sizes and layer 3 fragmentation
		/*
		MSG = "";
		int defLength = 3000;
		for (int i = 0; i < defLength; i++) {
			MSG = MSG.concat("A");
		}
		*/

		// Initialise variables for port number, buffer, send rate.
		Integer portNumber = null;
		byte[] buf = null;
		Integer sendRate = null;

		// large try-catch block that validates and sanity checks all input arguments
		// Throws an exception when invalid or unexpected argument is found, with custom message.
		try {
			// Splits IP arg into four parts, parses the parts as Integers and checks if they are in range 0-255.
			String ip = args[0];
			String[] ipSplit = ip.split("\\."); // . means any character, so you have to escape it for regex to work.
			if (ipSplit.length < 4) {
				throw new NumberFormatException("Invalid IP, use format: 0-255.0-255.0-255.0-255");
			}
			for (String s : ipSplit) {
				int test = Integer.parseInt(s);
				if (test > 255 || test < 0) {
					throw new NumberFormatException("Invalid IP, use format: 0-255.0-255.0-255.0-255");
				}
			}

			// Parses the port number, throws if not an int
			portNumber = Integer.parseInt(args[1]);
			// Checks to see if port is inside valid range, throws a NumberFormatException if out of range.
			if (portNumber < 0 || portNumber > ((int) (Math.pow(2, 16)) - 1)) {
				throw new NumberFormatException("Invalid port range, range has to be integer 0-65535");
			}

			// Parses the buffer size, throws if not an int
			int testBuffer = Integer.parseInt(args[2]);
			// Checks to see if buffer is within a valid range, throws exception if not.
			if (testBuffer < 1 || testBuffer > 100000) {
				throw new NumberFormatException("Invalid buffer size, please use range between 1-100,000");
			}
			// Creates the byte buffer used for receiving data.
			buf = new byte[testBuffer];

			// Checks if send rate is an int, throws exception if not. Throws exception if send rate is not within range.
			sendRate = Integer.parseInt(args[3]);
			if (sendRate < 0 || sendRate > 50000) {
				throw new NumberFormatException("Send rate out of range, use 0-50,000");
			}
		}
		// If any argument is not within expected parameters, tell user why and terminate the program.
		catch (NumberFormatException e) {
			System.err.println("Invalid argument found:");
			System.err.println(e.getMessage());

			System.err.println("Exiting...");
			System.exit(1);
		}

		// Handles the case where the argument for message rate is 0.
		// Sets boolean to let program know we want to terminate after one message.
		boolean doNotLoop = false;
		if (sendRate == 0) {
			sendRate = 1;
			doNotLoop = true;
		}

		// This if-block takes care of the case where the message size is larger than the receive buffer, which means the program is destined to fail.
		if (buf.length < MSG.getBytes().length) {
			System.err.println("Receive buffer length: (" + buf.length + ") is smaller than total message length: (" + MSG.getBytes().length + ")");
			System.err.println("Message received from server will only be a substring of total message, message comparison will always return fail!");
			Scanner s = new Scanner(System.in);
			System.err.println("Do you want to continue? (y/n)");

			// Following block of code is a switch block that handles user input after prompt.
			// Repeats if user input is anything other than "y" or "n".
			// Continues program execution by breaking out if input is "y"
			// Terminates program if input is "n"
			String decision = "";
			boolean repeat;
			do {
				decision = s.nextLine();
				switch (decision) {
					case "y":
						repeat = false;
						System.out.println("Program continuing...");
						break;

					case "n":
						System.err.println("Exiting...");
						System.exit(1);

					default:
						repeat = true;
						System.err.println("Please enter \"y\" or \"n\"");
				}
			} while (repeat);
		}

		// Creates the socket with my abstraction layer
		UDPClientSocket clientSocket = null;
		try {
			// Uses the new abstract implementation, (IP, DESTINATION PORT, SOURCE PORT)
			clientSocket = new UDPClientSocket(args[0], portNumber, MYPORT);
			clientSocket.setTimeout(TIMEOUT_MS); // Set max wait for a .receive() until packet is considered a loss.
		}
		// Basically only throws an exception when the port was already in use.
		catch (SocketException e) {
			System.err.println("\nSocket binding failed: " + e.getMessage());
			System.exit(1);
		}

		// Create datagram packet for sending message
		DatagramPacket sendPacket = new DatagramPacket(MSG.getBytes(), MSG.length(), clientSocket.getDestination());

		// Creates datagram packet for the receive portion of the program.
		DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);

		// Main loop
		do {
			long end = System.currentTimeMillis() + 1000; // Timer for 1s
			int packetsShipped = 0;
			int failures = 0;

			do {
				try {
					clientSocket.send(sendPacket);
					clientSocket.receive(receivePacket); // Blocks until packet has come in, or until timeout is hit
				}
				// Port Unreachable seems to not really work even though my socket is 'connected', unfortunately.
				catch (PortUnreachableException e) {
					System.err.println("\n Port unreachable, server no longer accepting packets");
					doNotLoop = true;
					failures++;
					break;
				}
				// If receive address is different from the connected socket address. Really only possible with extremely broken NAT or something.
				catch (IllegalArgumentException e) {
					System.err.println("\n Tried to send malformed packet, destination address does not match expected value");
					System.exit(1);
				}
				// socket.receive() will throw timeout when no response is heard for the user defined length of TIMEOUT_MS
				catch (SocketTimeoutException e) {
					failures++;
				}
				// General issue
				catch (IOException e) {
					System.err.println("General error: " + e.getMessage());
					System.exit(1);
				}

				/* Compare sent and received message */
				String receivedString = new String(receivePacket.getData(), receivePacket.getOffset(),
						receivePacket.getLength());

				// Console messages removed to improve performance, uncomment to get debug output.
				if (receivedString.compareTo(MSG) == 0) {
					//System.out.printf("%d bytes sent and received%n", receivePacket.getLength());
					packetsShipped++;
				}
				else {
					//System.out.printf("Sent and received msg not equal!%n");
					failures++;
				}
			}
			while (!doNotLoop && System.currentTimeMillis() < end && packetsShipped + failures < sendRate);

			// Waits out the remaining time, if any.
			while (System.currentTimeMillis() < end) {
				try {
					// Improves performance of program so it doesn't soak up 100% core util when waiting.
					Thread.sleep(0, 500);
				}
				// We don't really need to do anything here, since this main thread isn't intended to be ran as a Runnable.
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					System.err.println(e.getMessage());
					doNotLoop = true;
				}
			}

			// Print last informational before next pass of packets are to be sent, or before program termination.
			System.out.println("[Echoed " + packetsShipped + " out of " + sendRate + " packets ------ " + "Malformed packets or timeouts: " + failures + "]");

		}
		while (!doNotLoop);
	}

}
