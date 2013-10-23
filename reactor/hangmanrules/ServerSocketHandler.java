package hangmanrules;

import java.io.*;
import java.net.*;
import reactorapi.*;

import reactor.Dispatcher;

class ServerSocketHandle implements Handle<Socket> {

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


public class ServerSocketHandler implements EventHandler<Socket> {

	private ServerSocketHandle handle;
	private Dispatcher dp;
	
	public ServerSocketHandler (Dispatcher dp) throws IOException {
		this.dp = dp;
		handle = new ServerSocketHandle();
	}
	
	@Override
	public Handle<Socket> getHandle() {
		return handle;
	}

	@Override
	public void handleEvent(Socket s) {
		dp.addHandler(new ClientSocketHandler(s));
	}
}
