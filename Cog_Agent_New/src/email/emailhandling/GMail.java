package email.emailhandling;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;

import email.emailsending.ErrorEmail;

/**
 * This is our version of a GMail object. We take Google's Message object and
 * turn it into something more useful. It implements the Mail object because if
 * we want to move the agent to function on a different email service, the new
 * Email object for that service only has to implement the mail interface to be
 * used by our backend
 * 
 * @author nikhilchakravarthy
 *
 */
public class GMail implements Mail {

	private Message message;
	public String subject;
	public boolean fromAgent;
	public String from;
	public String body;
	private Gmail service;
	private String threadId;
	private String id;
	private long internalDate;
	private static final String tyco_Key = "A Tyco Innovation Garage© Creation";
	private static final String[] breakers = { "On Sun,", "On Mon,", "On Tue,", "On Wed,", "On Thu,", "On Fri,",
			"On Sat," };
	private static final String agentEmailAddress = "assistant.jci@gmail.com";

	/** <email_address, FirstName_LastName> */
	public Map<String, String> recipients;

	/** <email_address, FirstName_LastName */
	public Map<String, String> invitees;

	/**
	 * 
	 * @param m
	 *            - Message to be set to the Mail's message
	 * @param t
	 *            - Gmail to be used to get the Message m's thread to get all
	 *            recipients
	 * @throws IOException
	 */
	public GMail(Message m, Gmail t) throws IOException {
		message = m;
		service = t;
		for (int i = 0; i < message.getPayload().getHeaders().size(); i++) {
			if (message.getPayload().getHeaders().get(i).getName().equals("From")) {
				from = MailHelper.parseEmail(message.getPayload().getHeaders().get(i).getValue());
			}
		}
		id = message.getId();
		threadId = message.getThreadId();
		internalDate = message.getInternalDate();
		run();
		// new Thread(this).start();

	}

	public GMail() {

	}

	public GMail setInternalDate(long internalDate) {
		this.internalDate = internalDate;
		return this;
	}

	public GMail setId(String id) {
		this.id = id;
		return this;
	}

	public GMail setThreadId(String threadId) {
		this.threadId = threadId;
		return this;
	}

	public GMail setSubject(String subject) {
		this.subject = subject;
		return this;
	}

	public GMail setFromAgent(boolean fromAgent) {
		this.fromAgent = fromAgent;
		return this;
	}

	public GMail setFrom(String from) {
		this.from = from;
		return this;
	}

	public GMail setBody(String body) {
		this.body = body;
		return this;
	}

	public GMail setRecipients(Map<String, String> recipients) {
		this.recipients = recipients;
		return this;
	}

	public GMail setInvitees(Map<String, String> invitees) {
		this.invitees = invitees;
		return this;
	}

	/**
	 * Prints the recipients for the Mail
	 */
	public void printLabels() {
		System.out.println("Printing labels for: " + subject);
		System.out.println(message.getLabelIds());
	}

	/**
	 * Extracts all the recipients in a message from a Message
	 * 
	 * @NOTE: Only works with Gmail users currently
	 * 
	 * @param message
	 *            - Message to be analyzed
	 * @return Map<String,String> of email address to name
	 */
	public Map<String, String> getRecipients(Message message) {

		Map<String, String> map = new HashMap<String, String>();

		// Iterate through all the headers, and set from to the email address of
		// the sender. If the email is from tyco agent or from Google calendar,
		// set fromAgent to true
		for (int i = 0; i < message.getPayload().getHeaders().size(); i++) {
			if (message.getPayload().getHeaders().get(i).getName().equals("From")) {
				from = MailHelper.parseEmail(message.getPayload().getHeaders().get(i).getValue());
				if (message.getPayload().getHeaders().get(i).getValue().contains("assistant.jci@gmail.com")
						|| message.getPayload().getHeaders().get(i).getValue()
								.contains("calendar-notification@google.com")
						|| message.getPayload().getHeaders().get(i).getValue().contains("noreply@google.com")
						|| subject.contains("Decline") || subject.contains("Accept")) {
					fromAgent = true;
				}
			}

			// If the heder is From, To, Cc, or Reply-To, put the contents of
			// the header into a list of String sections of each person, then if
			// the section doesn't have tycoagent, put it in the map of
			// recipients
			if (message.getPayload().getHeaders().get(i).getName().equals("From")
					|| message.getPayload().getHeaders().get(i).getName().equals("To")
					|| message.getPayload().getHeaders().get(i).getName().equals("Cc")
					|| message.getPayload().getHeaders().get(i).getName().equals("Reply-To")) {

				for (String s : MailHelper.split(message.getPayload().getHeaders().get(i).getValue())) {
					if (!s.contains("assistant.jci@gmail.com")) {
						// System.out.println(s);
						try {
							map.put(MailHelper.parseEmail(s), MailHelper.parseName(s));
							if (map.get(MailHelper.parseEmail(s)).equals("no name")) {
								map.put(MailHelper.parseEmail(s), MailHelper.parseEmail(s));
							}
						} catch (StringIndexOutOfBoundsException e) {
							e.printStackTrace();
							ErrorEmail.errorEmail(e, this);
						}
					}

				}

			}
		}

		// As an added check, remove invalid emails
		Iterator<String> it = map.keySet().iterator();

		while (it.hasNext()) {
			String current = it.next();
			if (current.contains("no email")) {
				it.remove();
				map.remove(current);
				break;
			}
			if (!current.contains("@") || !current.contains(".")) {
				it.remove();
				map.remove(current);
				break;
			}
		}

		return map;
	}

	/**
	 * 
	 * @param m
	 *            - Message to identify the thread
	 * @param t
	 *            - Gmail service needed to get the thread
	 *            (assistant.jci@gmail.com)
	 * @return Map of email to name (String to String)
	 * @throws IOException
	 */
	public Map<String, String> getAllRecipients(Message m, Gmail t) throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		List<Message> msgs = t.users().threads().get("me", m.getThreadId()).execute().getMessages();
		// For all the messages in that message's thread, get all the recipients
		// and put them in the map
		for (Message msg : msgs) {
			map.putAll(getRecipients(msg));
		}
		// System.out.println(map);
		return map;
	}

	/**
	 * Returns text content of an input Gmail message in String form. As a
	 * backup, returns a snippet of the message
	 * 
	 * @param message
	 *            - Gmail message
	 * @return String of the message's body text content
	 */
	public static String getContent(GMail m) {
		StringBuilder stringBuilder = new StringBuilder();
		try {
			getPlainTextFromMessageParts(m.message.getPayload().getParts(), stringBuilder);
			byte[] bodyBytes = Base64.decodeBase64(stringBuilder.toString());
			String text = new String(bodyBytes, "UTF-8");
			return text;
		} catch (UnsupportedEncodingException e) {
			System.out.println("returning snippet, bad encoding");
			return m.message.getSnippet();
		}
	}

	/**
	 * Recursice helper method for getContent method
	 * 
	 * @param messageParts
	 *            - the message part to decode to plain text
	 * @param stringBuilder
	 *            - the plain text from messageParts is added to this
	 */
	private static void getPlainTextFromMessageParts(List<MessagePart> messageParts, StringBuilder stringBuilder) {
		// If there are no message parts, return the method
		if (messageParts == null) {
			return;
		}

		// For every message part, if it is plain text, add it to the
		// StringBuilder.Then, if the message part has more message parts, get
		// plain text from those too
		for (MessagePart messagePart : messageParts) {

			if (messagePart.getMimeType().equals("text/plain")) {
				stringBuilder.append(messagePart.getBody().getData());
			}

			if (messagePart.getParts() != null) {
				getPlainTextFromMessageParts(messagePart.getParts(), stringBuilder);
			}
		}
	}

	/**
	 * all the @Override methods are Mail interface methods
	 */
	@Override
	public Map<String, String> recipients() {

		return recipients;
	}

	@Override
	public Map<String, String> invitees() {
		// TODO Auto-generated method stub
		return invitees;
	}

	@Override
	public String from() {
		// TODO Auto-generated method stub
		return from;
	}

	@Override
	public boolean fromAgent() {
		// TODO Auto-generated method stub
		return fromAgent;
	}

	@Override
	public String subject() {
		// TODO Auto-generated method stub
		return subject;
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public String body() {
		return body;
	}

	@Override
	public String threadID() {
		return threadId;
	}

	@Override
	public long internalDate() {
		// TODO Auto-generated method stub
		return internalDate;
	}


	/**
	 * gets and sets the body, subject, recipients, invitees, and fromAgent from
	 * the Google Message object
	 */
	public void run() {
		body = cleanMessage(getContent(this));
		for (int i = 1; i < 32; i++) {
			body.replace((char) (i), ' ');
		}
		for (int i = 0; i < message.getPayload().getHeaders().size(); i++) {
			if (message.getPayload().getHeaders().get(i).getName().equals("Subject")) {
				subject = message.getPayload().getHeaders().get(i).getValue();
			}
		}
		recipients = getRecipients(message);
		try {
			invitees = getAllRecipients(message, service);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fromAgent = from.contains(agentEmailAddress);

	}

	/**
	 * cleans the body message to get rid of extraneous text
	 * @param message
	 * @return cleaned message String
	 */
	private String cleanMessage(String message) {
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
		message = message.replaceAll(">", "");
		message = message.replaceAll("<", "");
		message = message.replaceAll("\n", " ");

		for (int ind = 1; ind < message.length(); ind++) {
			if (message.charAt(ind) == 32 && message.charAt(ind - 1) == 32) {
				message = message.substring(0, ind - 1) + message.substring(ind + 1, message.length());
				ind--;
			}
		}

		message = message.replaceAll("\n", " ");

		for (int i = 0; i < 32; i++) {
			message = message.replaceAll((char) i + "", " ");
		}

		message = message.trim();
		message = message.replaceAll("  ", "\n");

		message = message.replaceAll("\n ", "\n");

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