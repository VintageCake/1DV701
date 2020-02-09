package assign1;

import java.io.IOException;
import java.net.ServerSocket;

import assign1.abstractions.TCPClientSocket;
import assign1.abstractions.TCPServerSocket;

public class TCPEchoServer {
	// Port which we should listen to
	private static final int MYPORT = 4950;
	// Timeout set for dead connections are 90s.
	private static final int TIMEOUT_MS = 90000;

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Need to define buffer size as argument!");
			System.exit(1);
		}

		int bufSize = 0;
		try {
			bufSize = verifyBuffer(args[0]);
		}
		catch (NumberFormatException e1) {
			System.err.println(e1.getMessage());
			System.exit(1);
		}

		TCPServerSocket welcome = null;
		try {
			welcome = new TCPServerSocket(MYPORT);
		}
		// ServerSocket throws error basically only with no network adapter enabled or port already bound.
		catch (IOException e) {
			System.err.println("Server socket creation failed: " + e.getMessage());
			System.exit(1);
		}

		System.out.println(java.time.LocalDateTime.now() + " Server started... listening on port: (" + welcome.getSource().getPort() + ") with buffer size: (" + bufSize + ")");

		// Main server loop
		try {
			while (true) {
				// welcome.accept() is blocking, waits until new connection is opened.
				// Tosses the newly created socket into a new thread, multithreads the connections.
				Thread t = new Thread(new TCPThread(new TCPClientSocket(welcome.accept()), bufSize, TIMEOUT_MS));
				t.start();
			}
		}
		// Handles exceptions thrown by accept()
		/*
		I couldn't get that method to throw anything even when trying really hard,
		so I assume it only throws when something terrible has happened
		*/
		catch (IOException e) {
			System.err.println("Server socket failed, reason: " + e.getMessage());
			System.err.println("Exiting...");
			System.exit(1);
		}
	}

	// Tests the buffer argument for any weird stuff, throws exception on out-of-range error or when argument is not an int
	private static int verifyBuffer(String buffer) {
		int testBuffer = Integer.parseInt(buffer);
		if (testBuffer < 1 || testBuffer > 100000) {
			throw new NumberFormatException("Invalid buffer size, please use range between 1-100,000");
		}
		return testBuffer;
	}
}
