package reactor;

import reactorapi.BlockingQueue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;


class CountingSemaphore {
	private volatile int value;

	CountingSemaphore(int permits) {
		value = permits;
	}
	
	public synchronized void acquire() throws InterruptedException{
		while(value == 0) wait();
//		System.out.println("acquire " + value);
		value--;
	}
	
	public synchronized int drain() throws InterruptedException {	
		while (value == 0) wait();
		
//		System.out.println("drain " + value);
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
		notifyAll();
	}
}

public class BlockingEventQueue<T> implements BlockingQueue<Event<? extends T>> {
	
	private final int capacity;
	private final ArrayDeque<Event<? extends T>> queue;
	
	private CountingSemaphore notEmpty, notFull;
	
	public BlockingEventQueue(int capacity) {
		queue = new ArrayDeque<Event<? extends T>>(capacity);
		this.capacity = capacity;
		
		notEmpty = new CountingSemaphore(0);
		notFull = new CountingSemaphore(capacity);
		System.out.println("init size " + capacity);
	}

	public int getSize() {
		return queue.size();
	}

	public int getCapacity() {
		return capacity;
	}

	public Event<? extends T> get() throws InterruptedException {

		notEmpty.acquire();
		
		Event<? extends T> event;
		synchronized (queue) {
			System.out.println("get 1");
			event = queue.remove();
		}
		
		notFull.release();
		
		return event;
	}

	public List<Event<? extends T>> getAll() throws InterruptedException {
		
		int permits = notEmpty.drain();

//		System.out.println(permits);
		ArrayList<Event<? extends T>> list;
		synchronized (queue) {
			System.out.println("getAll " + permits);
			list = new ArrayList<Event<? extends T>>(permits);

			for (int i = 0; i < permits; i++) {
				list.add(queue.remove());
			}
			
		}	
		
		notFull.release(permits);
		
		return list;
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
		
		synchronized (queue) {
			System.out.println(event.getEvent());
			queue.add(event);
		}
		
		notEmpty.release();
	}

	// Add other methods and variables here as needed.
}