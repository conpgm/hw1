package reactor;

import reactorapi.*;

import java.util.HashMap;

/**
 * This class is thread-safe. If this class works with application in the same thread, keyword "volatile" and
 * "synchronized" can be removed to improve the performance.
 */
public class Dispatcher {
	
	private final BlockingEventQueue<Object> queue;
	private final HashMap<EventHandler<?>, WorkerThread<?>> hMap;
	private volatile boolean running; 
	
	public Dispatcher() {
		this(10);
	}

	public Dispatcher(int capacity) {
		queue = new BlockingEventQueue<Object>(capacity);
		hMap = new HashMap<EventHandler<?>, WorkerThread<?>>();
		running = true;
	}

	public void handleEvents() throws InterruptedException {
		while (running) {
			try {
				Event<?> event = select();
				if (event.getEvent() == null) {
					removeHandler(event.getHandler());
				} else if (hMap.containsKey(event.getHandler())) {
					event.handle();
				}
			} catch (InterruptedException e) {
				if (running) {
					throw e;
				}
			}
		}
		System.out.println("Dispatcher stopped normally.");
	}

	public Event<?> select() throws InterruptedException {
		return queue.get();
	}

	public synchronized void addHandler(EventHandler<?> h) {
		if (!hMap.containsKey(h)) {
			WorkerThread<?> thread = new WorkerThread<>(h, queue);
			thread.start();
			hMap.put(h, thread);
		} else {
			System.err.println("Handler has already registered.");
		}
	}

	public synchronized void removeHandler(EventHandler<?> h) {
		if (hMap.containsKey(h)) {
			
			hMap.get(h).cancelThread();
			hMap.remove(h);
			
			/**
			 *  stop running dispatcher if all handlers are removed. Because select() might be blocked, 
			 *  we need interrupt current thread to stop dispatcher.
			 */
			if (hMap.isEmpty()) {
				running = false;
				Thread.currentThread().interrupt();
			}
		} else {
			System.err.println("Handler has not registered yet.");
		}
	}

	// Add methods and fields as needed.
}
