package reactor;

import reactorapi.BlockingQueue;

import java.util.LinkedList;
import java.util.List;


class CountingSemaphore {
	private int value;

	CountingSemaphore(int permits) {
		value = permits;
	}
	
	public synchronized void acquire() throws InterruptedException{
		while(value == 0) wait();
		value--;
	}
	
	public synchronized int drain() throws InterruptedException {	
		while (value == 0) wait();
		int tmp = value;
		value = 0; 
		return tmp;
	}
	
	public synchronized void release() {
		value++;
		notify();
	}
	
	public synchronized void release(int permits) {
		value += permits;
		for (int i = 0; i < permits; i++) 
			notify();
	}
}

public class BlockingEventQueue<T> implements BlockingQueue<Event<? extends T>> {
	
	private final int capacity;
	private final LinkedList<Event<? extends T>> ll;
	
	private CountingSemaphore notEmpty, notFull;
	
	public BlockingEventQueue(int capacity) {
		ll = new LinkedList<Event<? extends T>>();
		this.capacity = capacity;
		
		notEmpty = new CountingSemaphore(0);
		notFull = new CountingSemaphore(capacity);
	}

	public int getSize() {
		return ll.size();
	}

	public int getCapacity() {
		return capacity;
	}

	public Event<? extends T> get() throws InterruptedException {
//		while (ll.size() == 0) {
//			wait();
//		}
//		
//		if (ll.size() == capacity) {
//			notifyAll();
//		}
//		
//		return ll.remove();
		
		notEmpty.acquire();
		
		Event<? extends T> event;
		synchronized (ll) {
			event = ll.remove();
		}
		
		notFull.release();
		
		return event;
	}

	public List<Event<? extends T>> getAll() throws InterruptedException {
//		try {
//			while (ll.size() == 0) {
//				wait();
//			}
//		} catch (InterruptedException e) {
//			return new LinkedList<Event<? extends T>>();
//		}
//		
//		
//		if (ll.size() == capacity) {
//			notifyAll();
//		}
		
		int permits = notEmpty.drain();

		LinkedList<Event<? extends T>> eventList;
		synchronized (ll) {
			// deep copy and then clear
			eventList = new LinkedList<Event<? extends T>>(ll);
			ll.clear();
		}	
		
		notFull.release(permits);
		
		return eventList;
	}

	public void put(Event<? extends T> event) throws InterruptedException {
//		while (ll.size() == capacity) {
//				wait();
//		}
//		
//		if (ll.size() == 0) {
//			notifyAll();
//		}
//
//		ll.add(event);
		
		notFull.acquire();
		
		synchronized (ll) {
			ll.add(event);
		}
		
		notEmpty.release();
	}

	// Add other methods and variables here as needed.
}