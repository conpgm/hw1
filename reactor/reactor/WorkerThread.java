package reactor;

import reactorapi.*;

public class WorkerThread<T> extends Thread {
	private volatile boolean stopped;
	private final EventHandler<T> handler;
	private final BlockingEventQueue<Object> queue;

	// Additional fields are allowed.

	public WorkerThread(EventHandler<T> eh, BlockingEventQueue<Object> q) {
		handler = eh;
		queue = q;
		stopped = false;
	}

	public void run() {
		while (!stopped) {
			try {
				T obj = handler.getHandle().read();			
				queue.put(new Event<T>(obj, handler));
				if (obj == null) return ;
			} catch (InterruptedException e){
				if (stopped) {
					System.out.println("Thread stopped normally.");
				} else {
					System.err.println("Thread interrupted.");
				}
			}
		}
	}

	public void cancelThread() {
		if (isAlive()) {
			stopped = true;
			interrupt();
		}
	}
}