package hangmanrules;

import java.io.*;
import java.net.*;
import reactorapi.Handle;

public class ServerSocketHandle implements Handle<Socket> {

	private ServerSocket sSocket;
	
	public ServerSocketHandle() throws IOException {
		sSocket = new ServerSocket(0);
		System.out.println("" + sSocket.getLocalPort());
		System.out.flush();
	}
	
	@Override
	public Socket read() {
		try {
			Socket s = sSocket.accept();
			return s;
		} catch (IOException ie) {
			return null;
		}
	}

	public void close() {
		try {
			sSocket.close();
		} catch (IOException e) {
			return;
		}
	}
}
