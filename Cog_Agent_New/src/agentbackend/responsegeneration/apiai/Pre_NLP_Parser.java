/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agentbackend.responsegeneration.apiai;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import opennlp.tools.sentdetect.SentenceDetector;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

/**
 * This class cleans the message and turns it into a list of sentences if the
 * length of the message after taking out all the unnecessary text is still over
 * 256 characters, other returning a list with one String object in it
 * 
 * @author nikhilchakravarthy
 */
public class Pre_NLP_Parser {

	/**
	 * the breakers are used in cleaning a gmail message of unnecessary text
	 */
	private static final String[] breakers = { "On Sun,", "On Mon,", "On Tue,", "On Wed,", "On Thu,", "On Fri,",
			"On Sat," };

	/**
	 * the tyco_Key is how we check if the agent has already been a part of this
	 * threadß
	 */
	private static final String tyco_Key = "A Tyco Innovation Garage© Creation";

	/**
	 * public method to turn string into list of sentences
	 * @param message - string to be parsed
	 * @return -ß list of strings of sentences
	 */
	public static List<String> messageListToSend(String message) {
		return sentenceSplitter(message);
	}

	/**
	 * private method called by messageListToSend that actually does the parsing and splitting
	 * @param message - string to be parsed
	 * @return - list of string of sentences
	 */
	private static List<String> sentenceSplitter(String message) {
		message = cleanMessage(message);
		byte[] messageBytes = message.getBytes();
		try {
			message = new String(messageBytes, "US-ASCII");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<String> sentenceList = new ArrayList<String>();
		if (message.length() >= 256) {
			InputStream modelIn = Pre_NLP_Parser.class.getResourceAsStream("training/en-sent.bin");
			try {
				final SentenceModel sentenceModel = new SentenceModel(modelIn);
				modelIn.close();
				SentenceDetector sentenceDetector = new SentenceDetectorME(sentenceModel);
				if (modelIn != null) {
					modelIn.close();
				}
				String sentences[] = (sentenceDetector.sentDetect(message));
				for (int i = 0; i < sentences.length; i++) {
					sentenceList.add(sentences[i]);
				}
				modelIn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			sentenceList.add(message);
		}
		return sentenceList;
	}

	/**
	 * before parsing for sentences, we clean our all the extraneous text from an email
	 * @param message - message to be cleans
	 * @return - the same message with extraneous text removed
	 */
	private static String cleanMessage(String message) {
		System.out.println("message pre clean:\n" + message);
		if (message.contains(tyco_Key) && message.contains(">")) {

			int carrot = 0;
			for (int i = 0; i < message.length(); i++) {
				if (message.charAt(i) == '>') {
					carrot = i;
				}
			}

			for (int i = carrot; i > 0; i--) {

				if (message.substring(i, i + 2).equals("On")) {

					message = message.substring(0, i + 2);
				}
			}
			message = message.substring(0, message.length() - 2);
		}

		// Else if the msg is a reply to someone else that Tyco agent was copied
		// on, ignore the reply email, and only substring the section of the
		// email that is a copy of the original email
		else if (message.contains("wrote:") && message.contains(">")) {
			for (int i = 0; i < message.length(); i++) {
				if (message.charAt(i) == '>') {
					message = message.substring(i + 9, message.length());
					break;
				}
			}
		}

		// Case for outlook when confidential
		if (message.contains("--------------")) {
			for (int i = 0; i < message.length(); i++) {
				if (message.charAt(i - 1) == '-' && message.charAt(i) == '-' && message.charAt(i + 1) == '-') {
					message = message.substring(0, i - 1);
				}
			}
		}

		// Cut out the extra characters that are left over
		// System.out.println("msg before deletions:\n" + msg);
		// msg = msg.replaceAll(":", " ");
		for (int ind = 1; ind < message.length(); ind++) {
			if (message.charAt(ind) == 32 && message.charAt(ind - 1) == 32) {
				message = message.substring(0, ind - 1) + message.substring(ind + 1, message.length());
				ind--;
			}
		}
		message = message.replaceAll(">", "");
		message = message.replaceAll("<", "");
		message = message.replaceAll("\n", " ");

		message = message.replaceAll("\n", " ");

		for (int i = 0; i < 32; i++) {
			message = message.replaceAll((char) i + "", " ");
		}

		message = message.trim();

		// System.out.println("parsed message before on: \n" + message);
		for (String on : breakers) {
			while (message.contains(on)) {
				int start = message.indexOf(on);
				int end = message.indexOf("wrote:") + 6;
				message = message.substring(0, start) + ". " + message.substring(end, message.length());
			}
		}
		System.out.println("message post clean:\n" + message);
		return message;
	}

}
