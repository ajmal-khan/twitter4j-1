package twitter4j.examples.pyongjoo;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.examples.pyongjoo.CustomConfig;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CustomGetUserTimeline {
	/**
	 * Usage: java twitter4j.examples.timeline.GetUserTimeline
	 *
	 * @param args String[]
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		TwitterFactory tf = new TwitterFactory(CustomConfig.getConfOnRandomlyDistributedKey());
		
		// gets Twitter instance with default credentials
		Twitter twitter = tf.getInstance();

		if (args.length < 2) {
			System.err.println("Usuage: command [id_str] [outputfile]");
			System.exit(-1);
		}
		String filename = args[1];

		FileWriter fstream = new FileWriter(filename, true);
		BufferedWriter out = new BufferedWriter(fstream);

		long id_str = 000000;
		if (args.length >= 1) {
			id_str = Long.valueOf(args[0]);
		}
//		out.write("#document starts with id_str: " + id_str + "\n");
		
		for (int i = 1; i <= 1; i++) {
			Paging pagingOption = new Paging(i, 200);

			try {
				List<Status> statuses;
				
				statuses = twitter.getUserTimeline(id_str, pagingOption);
				
//				System.out.println("My Custom Showing @" + user + "'s user timeline.");

				out.write("#document starts with id_str: " + id_str + "\n");
				for (Status status : statuses) {
					out.write(status.getText() + "\n");
					System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
				}
			} catch (TwitterException te) {
				te.printStackTrace();
//				System.out.println("Failed to get timeline: " + te.getMessage());
				
				// close the file
				out.close();
				
				System.exit(-1);
			}
		}
		
		// close the file
		out.close();
	}
}
