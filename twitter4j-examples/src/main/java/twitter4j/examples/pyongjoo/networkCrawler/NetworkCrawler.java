package twitter4j.examples.pyongjoo.networkCrawler;


import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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


/** Crawl Twitter network from a set of seed users.
 * 
 * Input Parameters when Running the main file
 * 
 * @param logFileName Collected Edge data are saved to this file.
 * @param seedFile Network exploration starts from the set of users contained in this file.
 * 
 * 
 * About Seed File
 * 
 * Seed file is supposed to include a single user_id in each line. Lines are separated
 * by new line characters.
 * 
 * @author yongjoo
 *
 */
public class NetworkCrawler {
	
	private String logFileName = null;
	
	private int depthLimit = 2;
	
	final private List<Long> seedList;

	public static void main(String[] args) throws NetworkCrawlerException, IOException  {
		if (args.length < 2) {
			System.err.println("Usage: java NetworkCrawler [log file name] [seed file]");
			System.exit(1);
		}
		
		NetworkCrawler crawler = new NetworkCrawler(args[0], 0);
		
		// Add seed users		
		BufferedReader in = new BufferedReader(new FileReader(args[1]));
		
		String line;
		
		while ((line = in.readLine()) != null) {
			Long userId = new Long(line);
			crawler.addToSeedList(userId);
		}
		
		// Run the crawler
		crawler.run();
	}
	
	
	public NetworkCrawler(String filename) {
		logFileName = filename;
		seedList = new ArrayList<Long>();
	}
	
	
	public NetworkCrawler(String filename, int depthLimit) {
		logFileName = filename;
		this.depthLimit = depthLimit;
		seedList = new ArrayList<Long>();
	}
	
	
	public void addToSeedList(long userId) {
		seedList.add(userId);
	}
	
	
	public void addToSeedList(Iterable<Long> list) {
		for (long user_id : list) {
			addToSeedList(user_id);
		}
	}
	
	
	public void run() throws NetworkCrawlerException, IOException {
		if (logFileName == null) {
			throw new NetworkCrawlerException("Log file is not set.");
		}
		
		Manager manager = new Manager();
		
		manager.setDepthLimit(depthLimit);
		manager.setInitialSeed(seedList);
		manager.setLogFileName(logFileName);
		
		manager.run();
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
			addToExploreList(userId, 0);
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
	
	
	/** Add to the list to explore in the future.
	 * 
	 * This method performs the check if the node to be added has been visited
	 * in the past, which is to prevent redundant explorations.
	 * 
	 * @param userId
	 * @param nextDepth
	 */
	protected void addToExploreList(long userId, int nextDepth) {
		if (!explored.contains(userId)) {
			Task nt = new Task(nextDepth, userId);
			
			taskList.offer(nt);
			explored.add(userId);
		}
		else {
//			System.out.println("Already explored: " + userId);
		}
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
				logger.logEdges(t.getUserId(), neighbor, t.getDepth());
				
				// generate the next depth of task
				// unless exceeded the depth limit.
				if (t.getDepth() < depthLimit) {
					int nextDepth = t.getDepth() + 1;
					
					for (long n : neighbor) {
						// create a new task only if the node is not yet scheduled
						// to explore.
						addToExploreList(n, nextDepth);
					}
				}
				
			} catch (FollowingLimitExceedException e) {
				System.out.println("Following Limit Exceed: " + e.getUserId());
			} catch (FollowerLimitExceedException e) {
				System.out.println("Follower Limit Exceed: " + e.getUserId());
			}
		}
		
		rqFactory.stop();
		
		logger.close();
	}
}


class Logger {
	
	final BufferedWriter out;
	
	public Logger(String filename) throws IOException {
		out = new BufferedWriter(new FileWriter(filename, false));
	}

	public void logEdges(long userId, Iterable<Long> neighbor, int depth) throws IOException {
		System.out.println("Retrieved the neighbor of user_id: " + userId + ", depth: " + depth);
		
		for (long n : neighbor) {
//			System.out.println(userId + ", " + n);
			out.write(userId + "," + n + "\n");
		}
	}
	
	public void close() throws IOException {
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
	final int followerLimit = 2000;
	
	/**
	 * We think a user who's following more than the limit is a sort of celebrity,
	 * and will discard those users in the final network constructed.
	 */
	final int followingLimit = 2000;
	
	
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



