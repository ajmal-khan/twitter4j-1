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

public class CustomConfig {
	final private static String[][] tokens = {
		{
			"9HwmRbVlPtFkit5ijoHw9A",
			"LJFcWnOxhdScxo9DieKIp4007YpSYOZaLfyNvXT6vRQ",
			"542891917-amZW3joJ0KnkHedwLjE4OOSCQiZ1ofIVXUXkNoWq",
			"IFXkAcvQ5oXOxYmoBq8GWN0ekmeebHLHCPTdgRN6F4"
		},
		{
			"Wd0QxAyd0fb9J3S1VPsQA",
			"S1QTuQ9rS0cDoyWNz5OBQQVxmNssHjbVJoVXaP5V2g",
			"542892448-Bc7o8hzhSxm6ljdUC6XsWOcSXNmLiU6CPJ0UdKx1",
			"xkedi702bVFJzdw3HLaDhiq6JRayPQGNaN6UKJIsPn0"
		},
		{
			"ojKAoxU7Q6gS4iwrYvnKww",
			"p8AbF8l0P1oQ4mypKw7po6TJPVcjyV1m5TFCk",
			"542891738-oUXVyOVkdHXXcVcdfo5Fx6kapUGzXEwiHvbw6GJx",
			"zDwJyAwzjShad54tUYLfA2S7bCBb1rVsWrjaZMkY"
		},
		{
			"AfwgulQxnB3tHfKXdifg",
			"798DTelCvpCiLMRt3ca15NlOEdqSwnPlh4dQY9iNY",
			"542892197-DE4VmIbASY81cREX03Jj2yOlABNvAEAEvSgTDV59",
			"KsIek6HOS0ckWGYrxOLwkOJkZZJGpuzQ8ipZL2MMUE"
		},
		{	// pyongjoo5
			"5CmGPwYlZ6xrGsCtqM44g",
			"sWcXruUlYfy9yDuBZ2Da652E0jqZCKOXvk0tyOyjQKk",
			"542893456-mEyhLLss1AIfukqoTvOh4lTkgsWXZkkMOEJ2P9Iu",
			"yIQC0SVwSeOjAHNTWySlgSaVXG8EjVfY8rWwPbtIo"
		},
		{	// windfeeler@gmail.com
			"sXAoxYjl03LYw0DpZnFA",
			"xfNYgD6GsKGnvbiQ9AhXuBee2TQNEsejlLDaFa6Vrs",
			"97911870-TppTiNFL4xIkOMKzjCpzYbIp1ObMxuaKUIL4xPso",
			"chSLThC7VD65AQOms0gcf0f9p86ECaActzSkS7vM"
		},
		{	// pyongjoo6
			"aWWnH6qBVYgvk2t7vIjYAg",
			"NFONc5zdoTzufAeGkHCcBWKviQCM717eKdepDbAGgQ",
			"543806860-TBk0kXw7YLwvS6cqgWWhAaGdr324XgUpzJqmGPWM",
			"mY0ALC31Z6mVUHaXGUxzseL6OEn0kLV7z26myJZPw"
		},
		{	// pyongjoo7
			"CXqH7qviDUZJrykO0ymEg",
			"yguPAsBje3CIiNZ5amK7gB5UV8qKQz4xeTPofRJc",
			"543807307-vBuJJRbtxa7hQIRRFDglwKFnUWsx5KOY6r8n9LtJ",
			"ZJYm51PlCsYMyQyuOwXrwQUBb4WsvLwuh9vPLpW0A"
		},
		{	// pyongjoo8
			"nYYiC0EI4ZhqemtbXCA",
			"KhBG7aUY28kKSNQs7LSfz4wNWXXW9E3m4sbMxSwJk",
			"543891717-jsx490dqkJ1utlqUsTdVYpbfjBCpP6ThvUXRyzSz",
			"Y1jP973jdZ5M4uEJ67AOn2mRIbRL55rL6N39ynJY"
		},
		{	// pyongjoo9
			"HA3wEeC8tixHhXHWBNvhkA",
			"4KQYIsgliA9Cai3boiulviJWbmljxebecVNvrqeY4",
			"543892125-DxA1vVa0NCqSMqZKz3gjOsAfz0qFJqk1ODFrMZhg",
			"H70IYesPytWEZvWoN5qzXapO9M12lWlK7sneagFj9o"
		},
		{	// pyongjoo10
			"ELQP7MjdGA68QM4zsG6Emg",
			"7QOrolVOGKHaWNGVjoNM5hfnV3If8IvEXD7vzdRbEI",
			"543892161-0joeLw6YaFQ2fRnWpKHJahbOsDY7GwfwJRmWeMMs",
			"40Oal8PACwOm23jLypObfuo8KBYFseQS5wv7cgZl8"
		}
	};
	
	final private static String tempFile = "custom_auth_round_robin_key";

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