package hangmanrules;

import java.io.*;
import java.net.*;
import reactorapi.*;

class ClientSocketHandle implements Handle<String> {
	private Socket socket;
	private BufferedReader in;
	private PrintStream out;
	
	public ClientSocketHandle(Socket s) {
		socket = s;

		try {
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			out = new PrintStream(socket.getOutputStream(), true);
		} catch (Exception e) {
			throw new RuntimeException("Internal socket error");
		}
	}
	
	@Override
	public String read() {
		try {
			return in.readLine();
		} catch (IOException e) {
			return null;
		}
	}
	
	public void write(String s) {
		out.println(s);
	}
	
	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			return ;
		}
	}
}


public class ClientSocketHandler implements EventHandler<String> {

	private ClientSocketHandle handle;
	
	public ClientSocketHandler(Socket s) {
		handle = new ClientSocketHandle(s);
	}
	
	@Override
	public Handle<String> getHandle() {
		return handle;
	}

	@Override
	public void handleEvent(String s) {
		
	}
}
