package assign1;

import java.io.IOException;
import java.net.PortUnreachableException;
import java.net.SocketException;

import assign1.abstractions.TCPClientSocket;

public class TCPThread implements Runnable {
	TCPClientSocket sock = null;
	int buffer = 0;
	int TIMEOUT_MS = 0;

	public TCPThread(TCPClientSocket tcpClientSocket, int bufferSize, int timeout) {
		sock = tcpClientSocket;
		buffer = bufferSize;
		TIMEOUT_MS = timeout;
	}

	@Override
	public void run() {
		while (true) {
			try {
				sock.setTimeout(TIMEOUT_MS);
				sock.setupIO();

				/*
				 * While the socket is open, perform blocking operating until socket is ready to
				 * output data. Depending on length of data, stay in do-while loop and scoop up
				 * everything. Stay in do-while until all data has been received and buffer is
				 * empty OR buffer is full
				 * 
				 * When buffer is full, or program has received all data - activate send flag
				 * and send on next loop. Data is sent in a new byte variable that is the exact
				 * length of the data that is to be sent, issues arise when sending a byte[]
				 * that is partially empty!!
				 */
				boolean sendData = false;
				byte[] data = null;
				while (!sock.isClosed()) {
					if (sendData) { 
						sock.write(data);
						sendData = false;
					}
					else {
						data = sock.read(buffer);
						sendData = true;
					}
				}
			}
			// On any exception, break out of the loop and close socket, terminate thread.
			catch (PortUnreachableException e) {
				System.err.println("Echo not delivered, port is unreachable");
				break;
			}
			catch (SocketException e) {
				System.err.println("Connection from " + sock.getDestination().getAddress() + ":" + sock.getDestination().getPort() + " closed, reason: " + "(" + e.getMessage() + ")");
				break;
			}
			catch (IOException e) {
				System.err.println("General error");
				e.printStackTrace();
				break;
			}
		}
		/*
		 * Attempt to close socket when client has sent RST or FIN/ACK,
		 * or when client has 'soft closed' the connection with no warning and timeout was triggered.
		 */
		try {
			sock.close();
		}
		catch (IOException e) {
			System.err.println("Socket already closed/Error when closing socket");
		}
	}

}
