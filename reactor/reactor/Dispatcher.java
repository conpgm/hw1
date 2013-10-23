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
	
	public Dispatcher() {
		this(10);
	}

	public Dispatcher(int capacity) {
		queue = new BlockingEventQueue<Object>(capacity);
		hMap = new HashMap<EventHandler<?>, WorkerThread<?>>();
		
//		System.out.println("Initializing..");
	}

	public void handleEvents() throws InterruptedException {
		
		if (hMap.isEmpty()) {
			System.err.println("Events must not be read by the Reactor from a handle before registration.");
			return ;
		}
		
		while (!hMap.isEmpty()) {
			Event<?> event = select();
//			System.out.println(event.toString());
			if (event.getEvent() == null) {
//				System.out.println("Removing " + event.getHandler().hashCode() + " due to null event");
				removeHandler(event.getHandler());
			} else if (hMap.containsKey(event.getHandler())) {
				
				event.handle();
			}
		}
//		System.out.println("Dispatcher stopped normally.");
	}

	public Event<?> select() throws InterruptedException {
		return queue.get();
	}

	public <T> void addHandler(EventHandler<T> h) {
		if (!hMap.containsKey(h)) {
			WorkerThread<T> thread = new WorkerThread<T>(h, queue);
			thread.start();
			hMap.put(h, thread);
//			System.out.println("Added <" + h.hashCode() + ", " + thread.getName() + ">");
		} else {
//			System.err.println("Handler has already registered.");
		}
	}

	public void removeHandler(EventHandler<?> h) {
		if (hMap.containsKey(h)) {
			// cancel worker thread if it is still running
			hMap.get(h).cancelThread();
			hMap.remove(h);
//			System.out.println("Removed " + h.hashCode());
		} else {
//			System.err.println("Handler has not registered yet.");
		}
	}
	// Add methods and fields as needed.
}
