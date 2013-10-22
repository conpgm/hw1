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
	
	public Consumer (BlockingEventQueue<Integer> bq, int numTrials, AtomicInteger popSum) {
		this.bq = bq;
		this.numTrials = numTrials;
		this.popSum = popSum;
	}
	
	public void run() {
		int sum = 0;
		for (int i = 0; i < numTrials; i++) {
			try {
				popSum.getAndAdd(bq.get().getEvent());
				popSum.getAndAdd(bq.get().getEvent());
				List<Event<? extends Integer>> list = bq.getAll();
				Iterator<Event<? extends Integer>> itr = list.iterator();
				while(itr.hasNext()) {
					Event<? extends Integer> element = itr.next();
					
					popSum.getAndAdd(element.getEvent());
					
//					sum += element.getEvent();
//					System.out.println("sum " + popSum.get());
				}
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		
			Thread.yield();
		}
		//popSum.getAndAdd(sum);
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
			if (pool.awaitTermination(60, TimeUnit.SECONDS)) {
				System.out.println(Integer.toString(pushSum.get()) + " " + Integer.toString(popSum.get()));
				assertTrue(pushSum.get() == popSum.get());
			} else {
				System.out.println("timeout " + Integer.toString(pushSum.get()) + " " + Integer.toString(popSum.get()));
				assertTrue(pushSum.get() == popSum.get());
				//fail("Test safety timeout.");
			}
		} catch (Exception e) {
			fail("Test safety failed.");
		}
	}
}
