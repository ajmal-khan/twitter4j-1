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

package twitter4j.examples.friendsandfollowers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.examples.pyongjoo.CustomConfig;

/**
 * Lists friends' ids
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class GetFriendsIDs {
    /**
     * Usage: java twitter4j.examples.friendsandfollowers.GetFriendsIDs [screen name]
     *
     * @param args message
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
    	if (args.length < 2) {
			System.err.println("Usuage: command [username] [outputfile]");
			System.exit(-1);
		}
    	
        try {
        	TwitterFactory tf = new TwitterFactory(CustomConfig.getConfOnRandomlyDistributedKey());
            Twitter twitter = tf.getInstance();
            long userid = Long.valueOf(args[0]);
            
            String filename = args[1];
    		FileWriter fstream = new FileWriter(filename, true);
    		BufferedWriter out = new BufferedWriter(fstream);
    		
            long cursor = -1;
            IDs ids;
            System.out.println("Listing following ids.");
//            out.write("Listing following ids.");
            do {
            	ids = twitter.getFriendsIDs(userid, cursor);
            	
            	System.out.println("#printing following list of: " + userid);
            	out.write("#printing following list of: " + userid + "\n");
            	for (long id : ids.getIDs()) {
            		System.out.println(id);
            		out.write(id + "\n");
            	}
            } while ((cursor = ids.getNextCursor()) != 0);
            
            out.close();
            System.exit(0);
        } catch (TwitterException te) {
        	te.printStackTrace();
            System.out.println("Failed to get friends' ids: " + te.getMessage());
            System.exit(-1);
        }
    }
}
