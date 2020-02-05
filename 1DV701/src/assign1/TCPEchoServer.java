package assign1;

import java.io.IOException;
import java.net.ServerSocket;

import assign1.abstractions.TCPClientSocket;

public class TCPEchoServer {
	public static final int MYPORT = 4950;
	private static final int TIMEOUT_MS = 0;

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

		ServerSocket welcome = null;
		try {
			welcome = new ServerSocket(MYPORT);
		}
		catch (IOException e1) { // ServerSocket throws error basically either with no network adapter enabled or port already bound.
			System.err.println("Server socket creation failed, port likely already in use");
			System.err.println(e1.getMessage());
			System.exit(1);
		}
		System.out.println(java.time.LocalDateTime.now() + " Server started... listening on port: " + welcome.getLocalPort());
		
		// Main server loop
		try  {
			while (true) {
				// welcome.accept() is blocking, waits until new connection is opened.
				// Tosses the newly created socket into a new thread, multithreads the clients.
				Thread t = new Thread(new TCPThread(new TCPClientSocket(welcome.accept()), bufSize, TIMEOUT_MS));
				t.start();
			}
		}
		catch (IOException e) {
			System.err.println("Server socket failed, reason: " + e.getMessage());
			System.err.println("Exiting...");
			System.exit(1);
		}
	}

	private static int verifyBuffer(String buffer) {
		int testBuffer = Integer.parseInt(buffer);
		if (testBuffer < 1 || testBuffer > 100000) {
			throw new NumberFormatException("Invalid buffer size, please use range between 1-100,000");
		}
		return testBuffer;
	}
}
