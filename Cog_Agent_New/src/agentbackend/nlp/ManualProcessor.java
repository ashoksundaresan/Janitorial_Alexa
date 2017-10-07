package agentbackend.nlp;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;


public class ManualProcessor {

	static String [] retrieveWords = {"retrieve", "let me know", "give me", "past", "get me", "can i have", "show me", "query", "fetch", "know", "can we have", "tell me", "say to me", "obtain"};
	static String [] provideWords = {"bathroom", "restroom", "washroom", "toilet", "floor","sink"};
	String [] goodWords;
	String [] badWords;
	static String goodFileName ="/home/ubuntu/TycoAgent/good_words.txt", badFileName = "/home/ubuntu/TycoAgent/bad_words.txt";
	
	public static String patternMatchFeedback(String message) {
		
		for (String word : retrieveWords) {
			if (message.contains(word)) return "retrieve";
		}
		
		for (String word : provideWords) {
			if (message.contains(word)) return "provide";
		}
		
		return "unclear";
	}
	
	
	public static String patternMatchSentiment (String message){
		
		String [] goodWords = getWordsFromFile(goodFileName);
		String [] badWords = getWordsFromFile(badFileName);
		
		for (String word : goodWords) {
			System.out.println(word);
			if (message.contains(word)) {
				if (message.contains("not"))
					return "bad";
				else
					return "good";}
		}
		for (String word : badWords) {
			System.out.println(word);
			if (message.contains(word)) {
				if(message.contains("not"))
					return "good";
				else
					return "bad";
			}
		}
		
		return "neutral";
	
	}
	
	
	public static String[] getWordsFromFile(String filename) {
		Scanner sc=null;
		try {
			sc = new Scanner(new File(filename));
			} 
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}

		List<String> lines = new ArrayList<String>();
		while (sc.hasNextLine()) {
			lines.add(sc.nextLine());
			}

		String[] arr = lines.toArray(new String[0]);
		
		return arr;
		}


	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String retString = patternMatchSentiment("This requires");
		System.out.println(retString);

	}

}
