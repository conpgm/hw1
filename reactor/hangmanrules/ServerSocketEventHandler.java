package hangmanrules;

import java.io.IOException;
import java.net.*;

import reactor.Dispatcher;
import reactorapi.*;

public class ServerSocketEventHandler implements EventHandler<Socket> {

	private ServerSocketHandle handle;
	private Dispatcher dp;
	
	public ServerSocketEventHandler (Dispatcher dp) throws IOException {
		this.dp = dp;
		handle = new ServerSocketHandle();
	}
	
	@Override
	public Handle<Socket> getHandle() {
		return handle;
	}

	@Override
	public void handleEvent(Socket s) {
		dp.addHandler(new ClientSocketEventHandler(s));
	}
}
