package hangman;

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
			if (!sSocket.isClosed())
				sSocket.close();
		} catch (IOException e) {
			return;
		}
	}
}


public class ServerSocketEventHandler implements EventHandler<Socket> {

	private ServerSocketHandle handle;
	private Dispatcher dp;
	private HangmanRules<ClientSocketHandle> hr;
	
	public ServerSocketEventHandler (Dispatcher dp, HangmanRules<ClientSocketHandle> hr) throws IOException {
		handle = new ServerSocketHandle();
		this.dp = dp;
		this.hr = hr;
	}
	
	@Override
	public Handle<Socket> getHandle() {
		return handle;
	}

	@Override
	public void handleEvent(Socket s) {
		// game has ended
		if (s == null) {
			handle.close();
			return ;
		}
		
		// establish client connection
		try {
			dp.addHandler(new ClientSocketEventHandler(hr, handle, s));
		} catch (RuntimeException e) {
			System.err.println(e.getMessage());
			try {
				s.close();
			} catch (IOException e1) {
				return;
			}
		}
	}
}
