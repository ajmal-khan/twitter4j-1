package twitter4j.examples.pyongjoo.networkCrawler;


import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.examples.pyongjoo.CustomConfig;


public class NetworkCrawler {

	public static void main(String[] args)  {
		NetworkCrawler crawler = new NetworkCrawler();
		
		crawler.run();
	}
	
	public void run() {
		System.out.println("hello world");
	}
}


/** Manage workers and tasks.
 * 
 * A task represents a node from which to expand to explore a part of the large network.
 * 
 * A worker retrieves neighbor information after reading the node data from the task.
 * 
 * @author yongjoo
 *
 */
class Manager {
	
}


/** A unit to define a entity to work on
 * 
 * Includes information that a worker needs to know.
 * 
 * @author yongjoo
 *
 */
class Task {
	private int depth;
	private long user_id;
	
	Task(int depth, long user_id) {
		this.depth = depth;
		this.user_id = user_id;
	}
	
	long getUserId() {
		return user_id;
	}
	
	int getDepth() {
		return depth;
	}
}


/** Handles retrieving information from Twitter
 * 
 * @author yongjoo
 *
 */
class Worker {
	
	final ResourceQueue rq;
	
	/**
	 * We think a user who's followed more than the limit is a sort of celebrity,
	 * and will discard those users in the final network constructed.
	 */
	final int followerLimit = 1000;
	
	/**
	 * We think a user who's following more than the limit is a sort of celebrity,
	 * and will discard those users in the final network constructed.
	 */
	final int followingLimit = 1000;
	
	
	public Worker(ResourceQueue q) {
		rq = q;
	}
	
	
	public ArrayList<Long> getNeighbor(long userId) throws FollowingLimitExceedException, FollowerLimitExceedException {
		// Get following
		ArrayList<Long> followingList = getFollowing(userId);
		
		if (followingList.size() > followingLimit) {
			throw new FollowingLimitExceedException(userId);
		}
		
		// Get followers
		ArrayList<Long> followerList = getFollower(userId);
		
		if (followerList.size() > followerLimit) {
			throw new FollowerLimitExceedException(userId);
		}
		
		// Get intersection
		followingList.retainAll(followerList);
		
		return followingList;
	}
	
	
	/** Resource sensitive follower retriever 
	 * 
	 * @param userId
	 * @return
	 */
	public ArrayList<Long> getFollower(long userId) {
		try {
			rq.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return _getFollower(userId);
	}
	
	
	/** Resource sensitive following retriever
	 * 
	 * @param userId
	 * @return
	 */
	public ArrayList<Long> getFollowing(long userId) {
		try {
			rq.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return _getFollowing(userId);		
	}
	
	
	protected ArrayList<Long> _getFollower(long userId) {
		ArrayList<Long> followerList = new ArrayList<Long>();
		
		TwitterFactory tf = new TwitterFactory(CustomConfig.getConfOnRandomlyDistributedKey());
        Twitter twitter = tf.getInstance();
        
        long cursor = -1;
        IDs ids;

        try {
			ids = twitter.getFollowersIDs(userId, cursor);
			
	        for (long id : ids.getIDs()) {
	        	followerList.add(id);
	        }
	        
		} catch (TwitterException e) {
			System.err.println("Failed to get followers' ids: " + e.getMessage());
		}
        
        return followerList;
	}
	
	
	protected ArrayList<Long> _getFollowing(long userId) {
		ArrayList<Long> followingList = new ArrayList<Long>();
		
		TwitterFactory tf = new TwitterFactory(CustomConfig.getConfOnRandomlyDistributedKey());
        Twitter twitter = tf.getInstance();;
        
        long cursor = -1;
        IDs ids;

        try {
			ids = twitter.getFriendsIDs(userId, cursor);

	        for (long id : ids.getIDs()) {
	        	followingList.add(id);
	        }
		} catch (TwitterException e) {
			System.err.println("Failed to get followings' ids: " + e.getMessage());
		}
        
        return followingList;
	}
	
}


/** Convenient class to create a blocking factory used to manage Twitter api call rate limits
 * 
 * @author yongjoo
 *
 */
class ResourceQueueFactory {
	
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	
	public ResourceQueue getResourceQueue() {
		final ResourceQueue q = new ResourceQueue(100);

		scheduler.scheduleAtFixedRate(new Runnable() {
			public void run() {
				q.offer(new ResourceToken());
			}
		}, 0, 1, SECONDS);
		
		return q;
	}
	
	public void stop() {
		scheduler.shutdown();
	}
}


/** Internally use a blocking queue to manage Twitter api call rate limit.
 * 
 * @author yongjoo
 *
 */
class ResourceQueue extends ArrayBlockingQueue<ResourceToken> {

	private static final long serialVersionUID = 1L;

	public ResourceQueue(int capacity) {
		super(capacity);
	}
}


class ResourceToken {}


/** Define a series of exception to use while crawling network data
 * 
 * @author yongjoo
 *
 */
class NetworkCrawlerException extends Exception {
	private static final long serialVersionUID = 1L;
}


class FollowingLimitExceedException extends NetworkCrawlerException {
	private static final long serialVersionUID = 1L;
	
	private long userId;
	
	public FollowingLimitExceedException(long userId) {
		this.userId = userId;
	}
	
	public long getUserId() {
		return userId;
	}
}


class FollowerLimitExceedException extends NetworkCrawlerException {
	private static final long serialVersionUID = 1L;
	
	private long userId;
	
	public FollowerLimitExceedException(long userId) {
		this.userId = userId;
	}
	
	public long getUserId() {
		return userId;
	}
}



