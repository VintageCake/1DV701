package assign1;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

public class tcpLightClient {
	public static void main(String[] args) throws IOException {
		Socket s = new Socket();
		s.connect(new InetSocketAddress("127.0.0.1", 4950));
		
		BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		DataOutputStream out = new DataOutputStream(s.getOutputStream());
		
		String outputMessage = "hello";
		out.writeBytes(outputMessage);
		
		char[] test = new char[outputMessage.length()];
		in.read(test);
		String inputMessage = String.valueOf(test);
		if (outputMessage.compareTo(inputMessage) == 0) {
			System.out.println("OK");
		}
		System.out.println(inputMessage);
		s.close();
		
	}
}
