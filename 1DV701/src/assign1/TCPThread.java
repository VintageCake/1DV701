package assign1;

import java.io.IOException;
import java.net.PortUnreachableException;
import java.net.SocketException;

import assign1.abstractions.TCPClientSocket;

public class TCPThread implements Runnable {
	TCPClientSocket sock;
	byte[] buffer ;
	int TIMEOUT_MS;

	public TCPThread(TCPClientSocket tcpClientSocket, int bufferSize, int timeout) {
		sock = tcpClientSocket;
		buffer = new byte[bufferSize];
		TIMEOUT_MS = timeout;
	}

	@Override
	public void run() {
		System.out.println(java.time.LocalTime.now() + " Incoming connection from "
				+ sock.getDestination().getAddress().getHostAddress() + ":" + sock.getDestination().getPort()
				+ " opened");
		while (true) {
			try {
				sock.setTimeout(TIMEOUT_MS);
				sock.setupIO();

				/*
				While the socket is open, loop through these conditions.
				When data has appeared in the buffer, TCPClientSocket.read() will return an integer corresponding
				to the length of data read from the input stream.

				The buffer is filled from the bottom up, without doing any kind of operation to the buffer beforehand.
				This means that old array elements may be present, the length return is important here.
				When length n of data has been put into the buffer, send n elements of the array into the output stream.

				See TCPClientSocket for additional detail about the implementation of the methods write() and read().
				 */
				boolean sendData = false;
				int len = 0;
				while (!sock.isClosed()) {
					if (sendData) {
						sock.write(buffer, len);
						sendData = false;
					}
					else {
						len = sock.read(buffer);
						if (len > 0) {
							sendData = true;
						}
					}
				}
			}
			// On any exception, break out of the loop and close socket, terminate thread.
			catch (PortUnreachableException e) {
				System.err.println("Echo not delivered, port is unreachable");
				break;
			}
			// SocketException in this case usually means the destination host has closed their connection
			catch (SocketException e) {
				System.out.println(java.time.LocalTime.now() + " Connection from "
						+ sock.getDestination().getAddress().getHostAddress() + ":" + sock.getDestination().getPort()
						+ " closed, reason: " + "(" + e.getMessage() + ")");
				break;
			}
			catch (IOException e) {
				System.err.println("General error" + e.getMessage());
				break;
			}
		}
		/*
		 * Attempt to close socket when client has sent RST or FIN/ACK, or when client
		 * has 'soft closed' the connection with no warning and timeout was triggered.
		 */
		try {
			sock.close();
		}
		catch (IOException e) {
			System.err.println("Socket already closed/Error when closing socket");
		}
	}

}
