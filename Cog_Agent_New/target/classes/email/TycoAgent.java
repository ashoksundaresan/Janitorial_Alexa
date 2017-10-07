package email;

import com.google.api.services.gmail.model.*;

import email.emailhandling.GMail;
import email.emailhandling.Mail;
import external.google_api.gmail.GmailAuthorize;

import com.google.api.services.gmail.Gmail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TycoAgent {

	/** Instance of Gmail for TycoAgent */
	private Gmail jciEmail;

	/** Map for new Mails */
	private Map<String, Mail> recentMailMap;

	/** Constructor to get the Gmail service via GmailAuthorize */
	public TycoAgent() {

		// Initialize the Gmail, clear its emails via initialize(), initialize
		// the whole map with the init message, then initialize the new map,
		// dummy databse, auth users, and auth threads
		jciEmail = GmailAuthorize.getGmailService();
		// initialize();
		recentMailMap = new HashMap<String, Mail>();

	}

	public Map<String, Mail> getNewEmails() {
		return recentMailMap;
	}

	public Gmail getAgentGmail() {
		return jciEmail;
	}

	/**
	 * Maps every message to its extracted body text in String format
	 * 
	 * @param msgs
	 *            - Usable List of messages
	 * @return Map of key: Thread ID, value: Message
	 */
	private static Map<String, Mail> ContentExtract(List<Mail> msgs) {
		Map<String, Mail> Map = new HashMap<String, Mail>();

		for (Mail m : msgs) {
			Map.put(m.threadID(), m);
		}
		return Map;
	}

	/**
	 * Gets new emails since the last update time
	 * 
	 * @param time
	 *            - the Long time (UTC) since last update
	 * @return List of usable messages
	 */
	private List<Mail> getNewEmails(long time) throws IOException {
		List<Message> allMsgList = (jciEmail.users().messages().list("me").execute().getMessages());

		List<Mail> newMsgList = new ArrayList<Mail>();

		// Get all the messages in the inbox, and run through them. Check if
		// they are after the last email receiver. If they are, ensure that they
		// are valid, and they have not already been added to the old emails. If
		// that is all true, add them to the list of new messages. If they are
		// the same time or before the last email, break the loop so it doesn't
		// go through every single message

		for (Message m : allMsgList) {
			Message use = usable(m);
			if (use.getInternalDate().compareTo(time) == 1) {

				if (isValid(use) == true) {
					System.out.println("\n\n\n\nemail at: " + use.getInternalDate());
					newMsgList.add(convert(use));

				}
			} else {

				break;
			}

		}
		return newMsgList;
	}

	/**
	 * 
	 * @param m
	 *            - Message input for Mail constructor
	 * @return new Mail object
	 * @throws IOException
	 */
	private Mail convert(Message m) throws IOException {
		return new GMail(usable(m), jciEmail);
	}

	/**
	 * 
	 * @param m
	 *            - empty Message from the message list function (only has ID)
	 * @return usable Message with all fields
	 * @throws IOException
	 */
	private Message usable(Message m) throws IOException {
		return jciEmail.users().messages().get("me", m.getId()).execute();
	}

	/**
	 * At time of update, puts the messages gotten since the last email, puts
	 * them into the wholeMap, then gets new emails and puts them into the
	 * newMap, and returns the long time of the latest email. If no email is
	 * receiver, leave return the input oldTime
	 * 
	 * @param time
	 *            - long UTC time of update
	 * @return Long value of last email time
	 * @throws IOException
	 */
	public long updateMap(long oldTime) throws IOException {

		recentMailMap.clear();
		recentMailMap = ContentExtract(getNewEmails(oldTime));
		long ret = oldTime;
		for (String threadId : recentMailMap.keySet()) {
			ret = recentMailMap.get(threadId).internalDate();
			break;
		}
		return ret;
	}

	/**
	 * Checks if the message is good to be parsed into API.AI, with requirements
	 * being that it is not a calendar notification, and not a reply of
	 * tycoagent it self. As I find more things that should be marked invalid, I
	 * will add them to this method.
	 * 
	 * @param message
	 *            - message to be checked
	 * @return
	 */
	private boolean isValid(Message message) {
		boolean isValid = true;

		if (message.getLabelIds().contains("SENT")) {

			return false;
		}
		for (int i = 0; i < message.getPayload().getHeaders().size(); i++) {

			// If the Message is a Calendar invite response (Accept, Decline,
			// Tentative Accept), invalid Message

			if (message.getPayload().getHeaders().get(i).getName().equals("Subject")) {
				System.out.println("isValid for: " + message.getPayload().getHeaders().get(i).getValue());
				if (message.getPayload().getHeaders().get(i).getValue().contains("Decline")
						|| message.getPayload().getHeaders().get(i).getValue().contains("Accept")) {

					isValid = false;
					break;

				}
			}

			// If the Message is from tycoagent itself or is an event invite,
			// invalid Message

			if (message.getPayload().getHeaders().get(i).getName().equals("From")) {
				if (message.getPayload().getHeaders().get(i).getValue().contains("calendar-notification@google.com")
						|| message.getPayload().getHeaders().get(i).getValue().contains("assistant.jci@gmail.com")) {

					isValid = false;
					break;
				}
			}

		}

		// Else, the Message is valid
		return isValid;

	}

	// /**
	// * Deletes all but the init message in tycoagent's account
	// *
	// * @throws IOException
	// */
	// @Deprecated
	// private void initialize() throws IOException {
	// List<Message> msgs =
	// jciEmail.users().messages().list("me").execute().getMessages();
	// for (int i = 0; i < msgs.size() - 1; i++) {
	// jciEmail.users().messages().delete("me", msgs.get(i).getId()).execute();
	// }
	// System.out.println("initialized");
	// }

}