package reactor;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

class Producer implements Runnable {
	
	private final BlockingEventQueue<Integer> bq;
	private final int numTrials;
	private final AtomicInteger pushSum;
	
	public Producer (BlockingEventQueue<Integer> bq, int numTrials, AtomicInteger pushSum) {
		this.bq = bq;
		this.numTrials = numTrials;
		this.pushSum = pushSum;
	}
	
	public void run() {
		int seed = (hashCode() ^ (int)System.nanoTime());
		int sum = 0;
		for (int i = 0; i < numTrials; i++) {
			try {
				bq.put(new Event<Integer>(seed, null));
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			sum += seed;
			seed = xorshift(seed);
			Thread.yield();
		}
		pushSum.getAndAdd(sum);
	}
	
	private int xorshift(int y) {
		y ^= (y << 6);
		y ^= (y >>> 21);
		y ^= (y << 7);
		return y;
	}
}

class Consumer implements Runnable {
	
	private final BlockingEventQueue<Integer> bq;
	private final int numTrials;
	private final AtomicInteger popSum;
	
	private static final boolean testGetAll = true;
	
	public Consumer (BlockingEventQueue<Integer> bq, int numTrials, AtomicInteger popSum) {
		this.bq = bq;
		this.numTrials = numTrials;
		this.popSum = popSum;
	}
	
	public void run() {
		int sum = 0;
		for (int i = 0; i < numTrials; i++) {
			try {
				if (testGetAll) {
					// Apart from getAll() method, test get() method as well
					sum = bq.get().getEvent();
					popSum.getAndAdd(sum);

					// we should invoke AtomInteger methods as much as possible to improve performance
					// Here we use local variable to save the sum value
					List<Event<? extends Integer>> list = bq.getAll();
					Iterator<Event<? extends Integer>> itr = list.iterator();
					sum = 0;
					while(itr.hasNext()) {
						Event<? extends Integer> element = itr.next();
						sum += element.getEvent();
					}
					popSum.getAndAdd(sum);
					
					// slow consumption so as to give producer more time to produce items 
					Thread.sleep(100);					
				} else {
					sum += bq.get().getEvent();
				}				
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}

			Thread.yield();
		}

		if (!testGetAll) {
			popSum.getAndAdd(sum);
		}
	}
}

public class BlockingEventQueueTest {

	@Test
	public void test() {
		final ExecutorService pool = Executors.newCachedThreadPool();
		final AtomicInteger pushSum = new AtomicInteger(0);
		final AtomicInteger popSum = new AtomicInteger(0);
		final BlockingEventQueue<Integer> bq = new BlockingEventQueue<Integer>(20);
		
		try {
			// create 2000 threads and each thread does 1000 operations
			for (int i = 0; i < 1000; i++) {
				pool.execute(new Producer(bq, 1000, pushSum));
				pool.execute(new Consumer(bq, 1000, popSum));
			}
			
			pool.shutdown();
			pool.awaitTermination(60, TimeUnit.SECONDS);
			
			System.out.println("sum:" + pushSum.get() + " " + popSum.get());
			assertTrue(pushSum.get() == popSum.get());
		} catch (Exception e) {
			fail("Test safety failed.");
		}
	}
}
