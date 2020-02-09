package assign1;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

import assign1.abstractions.TCPClientSocket;

public class TCPEchoClient {
	public static final int MYPORT = 0; // In sockets, 0 means to use a client port in the DYNAMIC (private) port range.
	public static final String MSG = "An Echo message!";
	public static final int TIMEOUT_MS = 1000; // Sets message timeout

	public static void main(String[] args) {
		if (args.length != 4) {
			System.err.println("Usage: \"Destination address\", \"Port\", \"BufferSize\" (in bytes), \"send rate\"");
			System.exit(1);
		}

		/*
		 * Can be used to define custom message length with a repeating character String
		 * MSG = "";
		 * int defLength = 1400;
		 * for (int i = 0; i < defLength; i++) {
		 * MSG = MSG.concat("A");
		 * }
		 */


		// Initialise variables for port number, buffer, send rate.
		Integer portNumber = null;
		byte[] buf = null;
		Integer sendRate = null;

		// try-catch block that validates and sanity checks all input arguments
		try {
			verifyIP(args[0]);
			portNumber = verifyPort(args[1]);
			buf = new byte[verifyBuffer(args[2])];
			sendRate = verifySendRate(args[3]);

		}
		catch (NumberFormatException e) {
			System.err.println("Invalid argument found:");
			System.err.println(e.getMessage());

			System.err.println("Exiting...");
			System.exit(1);
		}

		boolean doNotLoop = false;
		if (sendRate == 0) {
			sendRate = 1;
			doNotLoop = true;
		}

		// Handles the case where the receive buffer length is smaller than message length
		// If the receive buffer is smaller than message length, the program still works fine, but with degraded performance.
		if (buf.length < MSG.getBytes().length) {
			System.err.println("Receive buffer length: (" + buf.length + ") is smaller than total message length: (" + MSG.getBytes().length + ")");
			System.err.println("Message received from server will only be a substring of total message, performance will be degraded");
			Scanner s = new Scanner(System.in);
			System.err.println("Do you want to continue? (y/n)");

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

		// Argument validation finished, main program below


		// Creates client socket and sets up IO
		TCPClientSocket tcpSocket = createSocket(args[0], portNumber, MYPORT);
		try {
			// Creates InputStream and OutputStream in the 'backend' abstraction class.
			tcpSocket.setupIO();
			tcpSocket.setTimeout(TIMEOUT_MS);
		}
		catch (IOException e) {
			System.out.println("Socket creation failed: " + e.getMessage());
			System.exit(1);
		}
		System.out.println("Connection established with server, starting to echo messages...");

		// Main loop of program
		do {
			long end = System.currentTimeMillis() + 1000; // Set timer, 1s from now
			int messagesShipped = 0;
			int failures = 0;
			boolean restFailed = false;

			do {
				String receivedMessage = "";
				try {
					tcpSocket.write(MSG.getBytes(), MSG.getBytes().length);

					while (receivedMessage.length() < MSG.length()) {
						// Fills buffer array with bytes from TCP stream (if there are any), read is how many new bytes there are from pos 0.
						int read = tcpSocket.read(buf);
						if (read > 0) { // if we got new bytes, concat the message
							receivedMessage = receivedMessage.concat(new String(buf, 0, read));
						}
					}
				}
				catch (IOException e) { // If anything goes wrong, tell user why - break out of loop, tell program to initialize final print
					System.out.println("Something went wrong: " + e.getMessage());
					System.out.println("Stopping execution and printing last set of data...");
					failures++;
					restFailed = true;
					doNotLoop = true;
					break;
				}

				// Compares message, 0 means same content which means everything went well.
				if (receivedMessage.compareTo(MSG) == 0) {
					messagesShipped++;
				}
				else {
					failures++;
				}
			}
			while (!doNotLoop && System.currentTimeMillis() < end && messagesShipped+failures < sendRate);

			// Waits out the remaining time, if any.
			while(System.currentTimeMillis() < end) {
				try {
					//
					Thread.sleep(0, 500);
				}
				// We don't really need to do anything here, since this main thread isn't intended to be ran as a Runnable.
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					System.err.println(e.getMessage());
					doNotLoop = true;
				}
			}

			System.out.println("[Echoed " + messagesShipped + " out of " + sendRate + " messages ------ " + "Malformed messages or timeouts: " + failures + "]");
			if (restFailed) {
				System.out.println("[Remaining packets failed to be sent due to error]");
			}
		}
		while (!doNotLoop);

		// When main loop is finished, try to close the socket.
		try {
			tcpSocket.close();
		}
		catch (IOException e) {
			System.err.println("Socket already closed: " + e.getMessage());
			System.exit(1);
		}
	}

	/*
	 * Initialise socket by calling creation methods for TCPClientSocket
	 * Handles all errors by telling user about error, and then closing the program.
	 */
	private static TCPClientSocket createSocket(String ip, int destPort, int myPort) {
		TCPClientSocket tcpSocket = null;
		try {
			tcpSocket = new TCPClientSocket(ip, destPort, myPort);
			tcpSocket.create();
		}
		catch (UnknownHostException e) {
			System.err.println("Invalid IP");
			System.exit(1);
		}
		catch (ConnectException e) {
			System.err.println("Connection failed, destination host did not respond");
			System.exit(1);
		}
		catch (SocketException e) {
			System.err.println("Socket creation failed");
			System.exit(1);
		}
		catch (IOException e) {
			System.err.println("General failure: " + e.getMessage());
			System.exit(1);
		}

		return tcpSocket;
	}

	// All methods below will throw a NumberFormatException with a custom message when input is unexpected in some way.
	// Verifies IP by splitting into four strings, verifying that those are all in the range of 0-255 when parsing them.
	private static void verifyIP(String ip) {
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
	}

	// Verifies port range, range is 0 to 2^16-1.
	private static Integer verifyPort(String port) {
		int portNumber = Integer.parseInt(port);
		if (portNumber < 0 || portNumber > ((int) (Math.pow(2, 16)) - 1)) {
			throw new NumberFormatException("Invalid port range, range has to be integer 0-65535");
		}
		return portNumber;
	}

	private static int verifyBuffer(String buffer) {
		int testBuffer = Integer.parseInt(buffer);
		if (testBuffer < 1 || testBuffer > 100000) {
			throw new NumberFormatException("Invalid buffer size, please use range between 1-100,000");
		}
		return testBuffer;
	}

	private static Integer verifySendRate(String rate) {
		int sendRate = Integer.parseInt(rate);
		if (sendRate < 0 || sendRate > 50000) {
			throw new NumberFormatException("Send rate out of range, use 0-50,000");
		}
		return sendRate;
	}
}
