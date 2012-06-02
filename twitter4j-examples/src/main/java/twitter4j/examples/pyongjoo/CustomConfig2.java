package twitter4j.examples.pyongjoo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class CustomConfig2 {

	/**
	 * OAuth tokens to access Twitter Rest API.
	 * <p>
	 * Refer to google doc, Twitter Account page. URL is
	 * <a>https://docs.google.com/document/d/1C8gXHxe9z9s235WfE8MKUFNUGYKTVG_ItPUxQCz5mVE/edit</a>.
	 */
	final private static String[][] tokens = {
		{
			"RyoOlgZJGrPaYfXitniKw",
			"UKnYPf6cObE44WvIodt41gQxtsdvElVifJ0Rw6ED0",
			"596977607-kTRdrBtUQuO8U9ngXcil54nzmFrmA8hWDHZZFuM0",
			"PNYjRWbtBMj8LxCQHL27YAtmYNWQTFqyaqSgeluw",
		},
		{
			"4rlo5MU8uPqtqjMjMe8Ogw",
			"OcSXzAA9GHLc7vfnCtRGHwNAjaP2r7VQyL1EWg4E",
			"596977964-tkoFYbzWqxdmJtjAhS00eh6RfYFBEF5HJEdpEiBN",
			"VtSGQBCP9xfqSAHXQScDykKMuRd262NpI2SgWkY4",
		},
		{
			"ONYlXGU0cFC3g1r1d9eHaA",
			"qj3LJVGANeGWHcp0Y5f7G0spBeon8fO0q1Yq95LCg",
			"596982982-GnSVgMKf5vrL4v4gix92rrqA3bDu0zd2Uk1vqTzH",
			"rzLCrxwwUeb80IiRyMxPCVP3eghd8H8hfO7lmtRZ4",
		},
		{
			"UlJoKt609TuXt8A6zAvqtQ",
			"GMPqcTKQvDaPSYmmlnXTQ3RWABu8nmN9WNWocQWRM",
			"597062181-GfdDqAJgH5IiVknDJhnwxIBQZwAQkHOS0l38kIkH",
			"Cy0jr0xd9Jat7SrMHzdJiwi0aKd7DYA9Qpb6YQoD9co",
		},
		{
			"uUBqT8NmRVWr2H0Rgu6ig",
			"A1utGuAoazUbQLubpCzexRmpeK0SkcTFZNNwY4mTDIY",
			"597062742-HUNmlFhWz1b5xkOTPRIFYGSmett2OQbATEkGvSAm",
			"hi4euhEDukLSgDmrSVblc8geqv4UezLRWP5lYvLqiZY",
		},
		{
			"9yMAWb7dynIqCUWVKVsF8w",
			"ae9LKmW5W59Jfm24ckZSQlxsisngpYDktTNxf7Pjm0",
			"597062772-6UcMpyb68DRGQf4ceUgxDG5ciKWr1bP4kNFBhj4P",
			"NEtUqXpCvJJDZzMDc5gUTjvKPkm3FHjm346PdEFJg",
		},
		{
			"X6eB3kEusPhWAIcjF4IDtw",
			"laXVcNI4zRBmnqoNMc8I1eSsf57sVy1KyLCo2oiTNY",
			"596980508-Ue2OYnu5TBhbJR5azss7txE2nRSdE9DDLcbWQ6qP",
			"C8yXii1hnPcufOwztjc5iI7x88d2ykz5qCosIDvqmpM",
		},
		{
			"PatwimZQruM4rbVA9dejQ",
			"reqAMUOgPgCVQFtealEG5W4F3OTYMMlr99HlICNEw",
			"596981261-Zhd4mkAcXLIHJNLq4074hzjAazkH7MSxUls2pf9f",
			"SGl1MypCeE4AvKetlH8EuhCiR4rCI4JFQXQEVy55Q",
		},
		{
			"0ZGXAbJY5ZmsfijrhTcbFg",
			"yZwR0TI2oI9GnHMMbmkyxuYQazpOK0KfRgeYfQeBU",
			"596985445-pDuXpYnw4qIJx23EK7qukJi4qv7emIvuxwQoOQzT",
			"TzhhbJyn5ce28kq2SV7zCCrYX6O6uTyooiVfgLHLI",
		},
		{
			"pjoCNIHGjzFWCQMjShM0w",
			"YC4HquMqrCXC64ps65nnaJxIr9lSdiYI9at5OyK2kcc",
			"597086529-Nh3sPPdAd9aoR62A1PsV3ESHb80DujvBA5q7TvQ",
			"cVMxkvDqUCGfsN9ZZCglrufp22V5raKn7HuqxOgQ",
		},
		{
			"knTI43vClQdN1Is5s4hfw",
			"SXpBa6Jaw7WSqxFH1lp241iRHP1vTlVESpQdtzaNs",
			"597008446-0dMEXfATSwmLji1hLAs0FATxqe4g7MGC8rJRYGrA",
			"Gjs4oTERGyQoKsNfEsQGmamN0tQpyCLGkVhKpNCHU",
		}
	};

	final private static String tempFile = "custom_auth_round_robin_key2";

	public static Configuration getConfOnRandomlyDistributedKey() {

		ConfigurationBuilder cb = new ConfigurationBuilder();
		
		int round_robin = 0;
		
		// Get the round robin key
		try {
			BufferedReader input =  new BufferedReader(new FileReader(tempFile));
			
			String line = null;
			
	        while (( line = input.readLine()) != null){
	        	round_robin = Integer.valueOf(line);
	        }
	        
		} catch (FileNotFoundException e) {
//			e.printStackTrace();
			// This happens at the first time
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String[] token = tokens[round_robin];
		
		
		// Write down the next round robin key
		Writer output;
		try {
			output = new BufferedWriter(new FileWriter(tempFile));
			
			output.write((round_robin + 1) % tokens.length + "\n");
			
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		cb.setDebugEnabled(true)
		  .setOAuthConsumerKey(token[0])
		  .setOAuthConsumerSecret(token[1])
		  .setOAuthAccessToken(token[2])
		  .setOAuthAccessTokenSecret(token[3]);
		
		return cb.build();
	}
	
	public static void test() {
		System.out.println(tempFile);
	}
}
