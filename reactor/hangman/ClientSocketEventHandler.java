package hangman;

import java.io.*;
import java.net.*;
import java.util.Iterator;
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
			throw new RuntimeException("client socket handle error");
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
	
	public void write(String str) {
		out.println(str);
	}
	
	public void close() {
		try {
			if (!socket.isClosed()) 
				socket.close();
		} catch (IOException e) {
			return ;
		}
	}
}


public class ClientSocketEventHandler implements EventHandler<String> {

	private ClientSocketHandle handle;
	private HangmanRules<ClientSocketHandle> hr;
	private ServerSocketHandle ssh;
	
	public ClientSocketEventHandler(HangmanRules<ClientSocketHandle> hr, ServerSocketHandle ssh, Socket s) {
		handle = new ClientSocketHandle(s);
		this.hr = hr;
		this.ssh = ssh;
	}
	
	@Override
	public Handle<String> getHandle() {
		return handle;
	}

	@Override
	public void handleEvent(String s) {
		// client quits the game or the game has ended
		if (s == null) {
			handle.close();
			hr.removePlayer(hr.getPlayerByData(handle));
			return ;
		}
		
		// the client transmits his/her name
		if (hr.getPlayerByData(handle) == null) {
			hr.addNewPlayer(handle, s);
			handle.write(hr.getStatus());
			return ;
		}
		
		// the client guesses data
		char g = s.charAt(0);
		hr.makeGuess(g);
		
		
		// broadcast current state to all players
		String msgState = hr.getPlayerByData(handle).getGuessString(g);
		
		Iterator<HangmanRules<ClientSocketHandle>.Player> itr1 = hr.getPlayers().iterator();
		while (itr1.hasNext()) {
			HangmanRules<ClientSocketHandle>.Player player = itr1.next();
			player.playerData.write(msgState);
		}
		
		// if game ends, terminate server by closing all sockets including server socket
		if (hr.gameEnded()) {
			// close all client sockets
			Iterator<HangmanRules<ClientSocketHandle>.Player> itr2 = hr.getPlayers().iterator();
			while (itr2.hasNext()) {
				itr2.next().playerData.close();
			}
			
			// close server socket
			ssh.close();
		}
	}
}
