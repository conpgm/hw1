package reactor;

import reactorapi.*;

public class WorkerThread<T> extends Thread {
	
	private volatile boolean stopped;
	private final EventHandler<T> handler;
	private final BlockingEventQueue<Object> queue;

	
	public WorkerThread(EventHandler<T> eh, BlockingEventQueue<Object> q) {
		handler = eh;
		queue = q;
		stopped = false;
		
//		System.out.println(this.getName() + " initializing...");
	}

	public void run() {
		while (!stopped) {
			try {
				T obj = handler.getHandle().read();			
				queue.put(new Event<T>(obj, handler));
				if (obj == null) stopped = true;
			} catch (InterruptedException e){
				if (!stopped) {
					System.err.println("Thread interrupted.");
					return ;
				}
			}
		}
//		System.out.println(this.getName() + " ended normally.");
	}

	public void cancelThread() {
		if (isAlive()) {
//			System.out.println(this.getName() + " canceling...");
			stopped = true;
			interrupt();
		}
	}
}