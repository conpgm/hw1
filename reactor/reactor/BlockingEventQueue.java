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
	private final ArrayDeque<Event<? extends T>> queue;
	
	private CountingSemaphore notEmpty, notFull;
	
	public BlockingEventQueue(int capacity) {
		queue = new ArrayDeque<Event<? extends T>>(capacity);
		this.capacity = capacity;
		
		notEmpty = new CountingSemaphore(0);
		notFull = new CountingSemaphore(capacity);
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
			event = queue.remove();
//			System.out.println("get 1");
		}
		
		notFull.release();
		
		return event;
	}

	public List<Event<? extends T>> getAll() throws InterruptedException {
		
		int permits = notEmpty.drain();

		ArrayList<Event<? extends T>> list;
		synchronized (queue) {
			
			list = new ArrayList<Event<? extends T>>(permits);

			for (int i = 0; i < permits; i++) {
				list.add(queue.remove());
			}
//			System.out.println("getAll " + permits);
		}	
		
		notFull.release(permits);
		
		return list;
	}

	public void put(Event<? extends T> event) throws InterruptedException {
		
		notFull.acquire();
		
		synchronized (queue) {
			queue.add(event);
//			System.out.println(event.getEvent());
		}
		
		notEmpty.release();
	}
	// Add other methods and variables here as needed.
}