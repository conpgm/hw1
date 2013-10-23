package hangmanrules;

import java.net.*;
import reactorapi.*;

public class ClientSocketEventHandler implements EventHandler<String> {

	private ClientSocketHandle handle;
	
	public ClientSocketEventHandler(Socket s) {
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
