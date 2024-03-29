package assign1;
/*
  TCPThread.java
  Author: Love Samulesson ls223qx@student.lnu.se
  Date: 2020-02-07

  A class that is intended to be run as a thread.
  Handles a single TCP connection and echoes back whatever comes into the input stream.

*/
import assign1.abstractions.TCPClientSocket;

import java.io.IOException;
import java.net.PortUnreachableException;
import java.net.SocketException;

public class TCPThread implements Runnable {
	TCPClientSocket sock;
	byte[] buffer;
	int timeoutMs;

	public TCPThread(TCPClientSocket tcpClientSocket, int bufferSize, int timeout) {
		sock = tcpClientSocket;
		buffer = new byte[bufferSize];
		timeoutMs = timeout;
	}

	@Override
	public void run() {
		System.out.println(java.time.LocalTime.now() + " Incoming connection from "
				+ sock.getDestination().getAddress().getHostAddress() + ":" + sock.getDestination().getPort()
				+ " opened");
		while (true) {
			try {
				// Set how many ms this thread should wait until connection is declared abandoned by other side, terminating thread.
				sock.setTimeout(timeoutMs);
				// Sets up the 'backend' InputStream and OutputStream
				sock.setupIO();

				// sock.getSocket().setTcpNoDelay(true); // Inefficient terrible command! Disables Nagle's algorithm.

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
				int bytesRead = 0;
				while (!sock.isClosed()) {
					if (sendData) {
						sock.write(buffer, bytesRead);
						sendData = false;
					}
					else {
						bytesRead = sock.read(buffer);
						if (bytesRead > 0) {
							sendData = true;
						}
						else if (bytesRead == -1) {
							throw new SocketException("InputStream reached EOF, Connection was terminated");
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
