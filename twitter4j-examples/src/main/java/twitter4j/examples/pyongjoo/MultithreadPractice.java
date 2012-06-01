package twitter4j.examples.pyongjoo;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.*;


public class MultithreadPractice {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		BlockingQueue<Item> queue = new ArrayBlockingQueue<Item>(5);

		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		
		scheduler.scheduleAtFixedRate(new Producer(queue), 0, 1, SECONDS);
		
		for (int i = 0; i < 3; i++) {
			try {
				queue.take();
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			System.out.println(i);
		}
		
		scheduler.shutdown();
	}
}

class Producer implements Runnable {
	private final BlockingQueue<Item> queue;
	
	Producer(BlockingQueue<Item> q) { queue = q; }
	
	public void run() {
			queue.offer(new Item());
	}
}

class Item {}
