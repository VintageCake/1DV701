package assign1;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.UnknownHostException;

import assign1.abstractions.TCPClientSocket;

public class TCPEchoClient {
	public static final int MYPORT = 0; // In sockets, 0 means to use a client port in the DYNAMIC (private) port range.
	public static final String MSG = "An Echo message!";
	public static final int TIMEOUT_MS = 50; // Sets message timeout

	public static void main(String[] args) {
		if (args.length != 4) {
			System.err.println("Usage: Destination address, Port, BufferSize (in bytes), sendrate");
			System.exit(1);
		}
		
		// Add warning if send buffer is smaller than message to be sent (or maybe iterate the bytes through the buffer??)

		/*
		 * Can be used to define custom message length with a repeating character String
		 * MSG = ""; int defLength = 1400; for (int i = 0; i < defLength; i++) { MSG =
		 * MSG.concat("A"); }
		 */

		// Initialise variables for port number, buffer, send rate.
		Integer portNumber = null;
		int buf = 0;
		Integer sendRate = null;

		// try-catch block that validates and sanity checks all input arguments
		try {
			verifyIP(args[0]);
			portNumber = verifyPort(args[1]);
			buf = verifyBuffer(args[2]);
			sendRate = verifySendRate(args[3]);

		}
		catch (NumberFormatException e) {
			System.err.println("Invalid argument found:");
			System.err.println(e.getMessage());

			System.err.println("Exiting...");
			System.exit(1);
		}

		boolean oneTime = false;
		if (sendRate == 0) {
			sendRate = 1;
			oneTime = true;
		}

		// Argument setup finished
		TCPClientSocket tcpSocket = createSocket(args[0], portNumber, MYPORT);
		try {
			tcpSocket.setupIO();
		}
		catch (IOException e) {
			System.out.println("Socket creation failed: " + e.getMessage());
			System.exit(1);
		}
		do {
			long end = System.currentTimeMillis() + 1000;
			int messagesShipped = 0;
			int failures = 0;

			do {
				String receivedMessage = "";
				try {
					tcpSocket.write(MSG.getBytes());

					while (receivedMessage.length() < MSG.length()) {
						receivedMessage = receivedMessage.concat(new String(tcpSocket.read(buf)));
					}
				}
				catch (Exception e) {
					System.out.println(e.getMessage());
					oneTime = true;
					break;
				}
				if (receivedMessage.compareTo(MSG) == 0) {
					messagesShipped++;
				}
				else {
					failures++;
				}
			}
			while (!oneTime && System.currentTimeMillis() < end && messagesShipped < sendRate);

			System.out.println("------");
			System.out.println("Successfully echoed " + messagesShipped + " out of " + sendRate + " messages");
			System.out.println("Malformed packets or timeouts: " + failures);
			System.out.println("------");
		}
		while (!oneTime);

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
