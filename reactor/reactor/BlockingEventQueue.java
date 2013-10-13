package reactor;

import reactorapi.BlockingQueue;

import java.util.LinkedList;
import java.util.List;

public class BlockingEventQueue<T> implements BlockingQueue<Event<? extends T>> {
	
	private final int capacity;
	private final LinkedList<Event<? extends T>> ll;
	
	public BlockingEventQueue(int capacity) {
		// TODO: Implement BlockingEventQueue(int).
		ll = new LinkedList<Event<? extends T>>();
		this.capacity = capacity;
	}

	public int getSize() {
		// throw new UnsupportedOperationException();
		// TODO: Implement BlockingEventQueue.getSize().
		return ll.size();
	}

	public int getCapacity() {
		// throw new UnsupportedOperationException(); // Replace this.
		// TODO: Implement BlockingEventQueue.getCapacity().
		return capacity;
	}

	public synchronized Event<? extends T> get() throws InterruptedException {
		// throw new UnsupportedOperationException(); // Replace this.
		// TODO: Implement BlockingEventQueue.get().
		while (ll.size() == 0) {
			wait();
		}
		
		if (ll.size() == capacity) {
			notifyAll();
		}
		
		return ll.remove();
	}

	public synchronized List<Event<? extends T>> getAll() {
		// throw new UnsupportedOperationException(); // Replace this.
		// TODO: Implement BlockingEventQueue.getAll().
		try {
			while (ll.size() == 0) {
				wait();
			}
		} catch (InterruptedException e) {
			return new LinkedList<Event<? extends T>>();
		}
		
		
		if (ll.size() == capacity) {
			notifyAll();
		}
		
		// deep copy and then clear
		LinkedList<Event<? extends T>> eventList = new LinkedList<Event<? extends T>>(ll);
		ll.clear();
		
		return eventList;
	}

	public synchronized void put(Event<? extends T> event) throws InterruptedException {
		// TODO: Implement BlockingEventQueue.put(Event).
		while (ll.size() == capacity) {
				wait();
		}
		
		if (ll.size() == 0) {
			notifyAll();
		}

		ll.add(event);
	}

	// Add other methods and variables here as needed.
}