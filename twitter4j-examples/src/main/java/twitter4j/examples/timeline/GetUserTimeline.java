/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package twitter4j.examples.timeline;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.examples.pyongjoo.CustomConfig;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.7
 */
public class GetUserTimeline {
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
			System.err.println("Usuage: command [username] [outputfile]");
			System.exit(-1);
		}
		String filename = args[1];

		FileWriter fstream = new FileWriter(filename, true);
		BufferedWriter out = new BufferedWriter(fstream);
		
//		OutputStream file = new FileOutputStream(filename, true);
//		OutputStream buffer = new BufferedOutputStream( file );
//		ObjectOutput output = new ObjectOutputStream( buffer );

		String user = "";
		if (args.length >= 1) {
			user = args[0];
		}
//		out.write("#document starts with username: " + user + "\n");
		
		for (int i = 1; i <= 5; i++) {
			Paging pagingOption = new Paging(i, 200);

			try {
				List<Status> statuses;
				
				statuses = twitter.getUserTimeline(user, pagingOption);
				
//				System.out.println("My Custom Showing @" + user + "'s user timeline.");

				for (Status status : statuses) {
					out.write(status.toString() + '\n');
//					System.out.println(status.getUser().getScreenName() + "tweets written.");
				}
			} catch (TwitterException te) {
				te.printStackTrace();
//				System.out.println("Failed to get timeline: " + te.getMessage());
				
				// close the file
				out.close();
//				output.close();
				
				System.exit(-1);
			}
		}
		
		// close the file
		out.close();
//		output.close();
	}
}
