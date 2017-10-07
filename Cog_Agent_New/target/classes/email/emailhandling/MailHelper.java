package email.emailhandling;

import java.util.ArrayList;
import java.util.List;

public class MailHelper {

	/**
	 * Extracts the name of a recipient from the string
	 * 
	 * @param recipient
	 * @return String of the name of recipient, or 'no name' if no name is found
	 */

	// take care of this case <nikhil.chakravarthy@case.edu>
	public static String parseName(String recipient) {

		if (recipient.contains("<")) {
			for (int i = 0; i < recipient.length(); i++) {
				if (recipient.charAt(i) == '<') {

					return recipient.substring(0, i - 1);
				}
			}
		}
		return "no name";
	}

	// Has to read format:
	// "First Last <email@gmail.com>, First Last <email@gmail.com>, ...,
	// tycoagent@gmail.com

	/**
	 * Splits the string of recipients into a list of individual recipients
	 * 
	 * @param recipients
	 *            - String of all recipients from a single header
	 * @return List<String> of individual recipients
	 */
	public static List<String> split(String recipients) {
		List<String> tokens = new ArrayList<String>();
		int start = 0;

		// If the string to be split doesn't have a comma, it is just one
		// person, so add it to the list and return it
		if (!recipients.contains(",")) {
			tokens.add(recipients);
		}

		// Otherwise, split the string by every comma and add the entries to the
		// list, then add the last entry
		else {

			for (int i = 0; i < recipients.length(); i++) {
				if (recipients.charAt(i) == ',') {
					tokens.add(recipients.substring(start, i));
					i++;
					start = i;
				}
			}
			for (int i = recipients.length() - 1; i > 0; i--) {
				if (recipients.charAt(i) == ',') {
					tokens.add(recipients.substring(i + 2, recipients.length()));
					break;
				}
			}
		}

		return tokens;
	}

	/**
	 * 
	 * @param email
	 *            - string to parse email from using the '@' char as the
	 *            identifier
	 * @return email address
	 */
	public static String parseEmail(String email) {
		if (!email.contains("@")) {
			return "invalid_email";
		} else {
			StringBuilder front = new StringBuilder(), back = new StringBuilder();
			int at = email.indexOf('@');
			for (int i = at + 1; i < email.length(); i++) {
				if (stopChar(email.charAt(i))) {
					break;
				} else {
					back.append(email.charAt(i));
				}

			}
			for (int j = at - 1; j >= 0; j--) {
				if (stopChar(email.charAt(j))) {
					break;
				} else {
					front.append(email.charAt(j));
				}

			}

			return front.reverse().toString() + '@' + back.toString();
		}
	}

	/**
	 * returns true if the char is a char that should stop the building of the
	 * email address outwards from the '@' char
	 * 
	 * @param in - char input
	 * @return true or false
	 */
	private static boolean stopChar(char in) {
		if (in == '>' || in == '<' || in == ' ' || in == ',') {
			return true;
		}
		return false;
	}

}
