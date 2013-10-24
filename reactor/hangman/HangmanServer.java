package hangman;

import java.io.*;
import reactor.*;


public class HangmanServer {

	public static void main(String[] args) {
		
		if (args.length != 2) {
			System.err.println("Usage: java hangman.HangmanServer <word> <tries>");
			System.exit(-1);
		}
		
		Dispatcher dp = new Dispatcher();
		HangmanRules<ClientSocketHandle> hr = new HangmanRules<ClientSocketHandle>(args[0], Integer.parseInt(args[1]));		
		
		try {
			dp.addHandler(new ServerSocketEventHandler(dp, hr));
			dp.handleEvents();
		} catch (IOException e) {
			System.err.println("server socket handle error");
			System.exit(-2);
		} catch (InterruptedException e) {
			System.err.println("dispatcher interruptted");
			System.exit(-3);
		}
	}

}
