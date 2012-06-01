package twitter4j.examples.pyongjoo.networkCrawler;


import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.examples.pyongjoo.CustomConfig;


public class NetworkCrawler {
	
	private String logFileName = null;

	public static void main(String[] args) throws NetworkCrawlerException  {
		if (args.length < 1) {
			System.err.println("Usage: java NetworkCrawler [log file name]");
			System.exit(1);
		}
		
		NetworkCrawler crawler = new NetworkCrawler(args[0]);
		
		crawler.run();
	}
	
	
	public NetworkCrawler(String filename) {
		logFileName = filename;
	}
	
	
	public void run() throws NetworkCrawlerException {
		if (logFileName == null) {
			throw new NetworkCrawlerException("Log file is not set.");
		}
		
		ArrayList<Long> initialGuys = new ArrayList<Long>();
		initialGuys.add(new Long(24642133));
		
		Manager manager = new Manager();
		
		manager.setDepthLimit(1);
		
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
	/**
	 * Manager has its own task list that he manages.
	 */
	final Queue<Task> taskList;
	
	/**
	 * Remember user_id that's already explored.
	 * 
	 * Nodes that are scheduled to explore in the future (i.e., once they have been added
	 * to the taskList), those nodes are also added to this set.
	 */
	final Set<Long> explored;
	
	/** Specifies the limit to explore.
	 * 
	 * The depth of the root is 0, and the depth increases by one as we explore one more.
	 * 
	 * For example, depthLimit being 0 means that we only explore the neighbors of the roots. 
	 */
	private int depthLimit = 2;
	
	private String logFileName; 
	
	public Manager() {
		taskList = new LinkedList<Task>();
		explored = new HashSet<Long>();
	}
	
	/**
	 * This method is required to be called to start the work.
	 * @param userIdList
	 */
	public void setInitialSeed(Iterable<Long> userIdList) {
		for (long userId : userIdList) {
			taskList.offer(new Task(0, userId));
			explored.add(new Long(userId));
		}
	}
	
	/** Required to call before calling the 'run' method.
	 * 
	 * The absolute path is preferred to avoid the confusion.
	 * 
	 * @param filename
	 */
	public void setLogFileName(String filename) {
		logFileName = filename;
	}
	
	/** Optional
	 * @param limit
	 */
	public void setDepthLimit(int limit) {
		depthLimit = limit;
	}
	
	/** Start to manage the job, and stops if it reaches the depthLimit.
	 * @throws IOException 
	 */
	public void run() throws IOException {
		ResourceQueueFactory rqFactory = new ResourceQueueFactory();
		ResourceQueue rq = rqFactory.getResourceQueue();	// resource queue is periodically added a resource token.
		
		Logger logger = new Logger(logFileName);
		
		while (taskList.size() > 0) {
			Task t = taskList.remove();
			
			Worker worker = new Worker(rq);
			
			try {
				ArrayList<Long> neighbor = worker.getNeighbor(t);
				
				// log the retrieved neighbor
				logger.logEdges(t.getUserId(), neighbor);
				
				// generate the next depth of task
				// unless exceeded the depth limit.
				if (t.getDepth() < depthLimit) {
					int nextDepth = t.getDepth() + 1;
					
					for (long n : neighbor) {
						// create a new task only if the node is not yet scheduled
						// to explore.
						if (!explored.contains(n)) {
							Task nt = new Task(nextDepth, n);
							
							taskList.offer(nt);
							explored.add(n);
						}
					}
				}
				
			} catch (FollowingLimitExceedException e) {
				e.printStackTrace();
			} catch (FollowerLimitExceedException e) {
				e.printStackTrace();
			}
		}
		
		rqFactory.stop();
	}
}


class Logger {
	
	final BufferedWriter out;
	
	public Logger(String filename) throws IOException {
		out = new BufferedWriter(new FileWriter(filename, false));
	}

	public void logEdges(long userId, Iterable<Long> neighbor) throws IOException {
		// TODO
		for (long n : neighbor) {
			out.write(userId + "," + n + "\n");
		}
	}
	
	public void clsoe() throws IOException {
		out.close();
	}
}


/** A unit to define an entity to work on.
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
	
	
	public ArrayList<Long> getNeighbor(Task task) throws FollowingLimitExceedException, FollowerLimitExceedException {
		return getNeighbor(task.getUserId());
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
 * Resource queue is periodically added a resource token by the factory.
 * 
 * @author yongjoo
 *
 */
class ResourceQueueFactory {
	
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	
	private long period = 1;
	
	public ResourceQueue getResourceQueue() {
		final ResourceQueue q = new ResourceQueue(100);

		scheduler.scheduleAtFixedRate(new Runnable() {
			public void run() {
				q.offer(new ResourceToken());
			}
		}, 0, period, SECONDS);
		
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
	
	public NetworkCrawlerException() {}
	
	public NetworkCrawlerException(String message) {
		super(message);
	}
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



