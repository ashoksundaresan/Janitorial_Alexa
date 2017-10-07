package email.emailsending;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

import agentbackend.agentserver.Command;
import external.google_api.gmail.GmailAuthorize;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.Address;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * 
 * @author nikhilchakravarthy Class used to send emails from the agents account
 *         by creating a Java MimeMessage object, then writing that to an empty
 *         Google Message object and sending it from the agents email account
 */
public class EmailCreater {

	/** Gmail account object for TycoAgent */
	private Gmail tyco;

	private static final String agentEmail = "assistant.jci@gmail.com";

	/**
	 * for sending to everyone on the
	 * 
	 * @param subject
	 * @param body
	 * @throws IOException
	 * @throws MessagingException
	 */
	public EmailCreater(String subject, String body, List<String> emails, boolean mass)
			throws MessagingException, IOException {
		tyco = GmailAuthorize.getGmailService();
		Set<String> recipients = new HashSet<>(emails);

		sendEmail(subject, body, recipients, "new", mass);
	}

	/**
	 * creates an email from a Command object. If the command was a delete or
	 * search, only send the email to the recipients of the email that requested
	 * the delete or search. If it was an insert or update, send the email to
	 * every one who was ever on the thread with the agent so that everyone can
	 * be notified
	 * 
	 * @param command
	 */
	public EmailCreater(Command command) {
		try {
			tyco = GmailAuthorize.getGmailService();

			if (command.getIntent().contains("delete") || command.getIntent().contains("search")) {
				new EmailCreater(command.getSubject(), command.getResponse(), command.getRecipients(),
						command.getThreadId(), tyco);
			} else {
				new EmailCreater(command.getSubject(), command.getResponse(), command.getInvitees(),
						command.getThreadId(), tyco);
			}
		} catch (IOException | MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param subject
	 *            subject
	 * @param body
	 *            text body
	 * @param receiver
	 *            recipient
	 * @param thdid
	 *            thdid
	 * @throws IOException
	 * @throws MessagingException
	 */
	public EmailCreater(String subject, String body, String receiver, String thdid, Gmail gmail)
			throws IOException, MessagingException {

		tyco = gmail;

		List<String> temp = new ArrayList<String>();
		temp.add(receiver);
		Set<String> recipients = new HashSet<>(temp);

		sendEmail(subject, body, recipients, thdid, false);
	}

	/**
	 * Constructor that sends an email to the receiver with the given subject
	 * and text body
	 * 
	 * @param receiver
	 *            - email address to be sent to
	 * @param subj
	 *            - subject of the email
	 * @param body
	 *            - body of text to be sent
	 */
	public EmailCreater(String subj, String body, Map<String, String> receiver, String thdID, Gmail gmail)
			throws IOException, MessagingException {
		tyco = gmail;

		sendEmail(subj, body, receiver.keySet(), thdID, false);
		System.out.println("sent email to " + receiver);
	}

	public EmailCreater(String subj, String body, List<String> receiver, String thdID, Gmail gmail)
			throws IOException, MessagingException {
		tyco = gmail;

		Set<String> recipients = new HashSet<>(receiver);
		sendEmail(subj, body, recipients, thdID, false);
		System.out.println("sent email to " + receiver);
	}

	/**
	 * Sends email to the receiver with given subject and text body. Adds the
	 * unique tag to help identify a chain of emails
	 * 
	 * @param receiver
	 *            - email address to be sent to
	 * @param msgSubject
	 *            - subject of the email
	 * @param msgBody
	 *            - body of text to be sent
	 * @param mass
	 */
	public void sendEmail(String msgSubject, String msgBody, Set<String> msgRecipients, String thdID, boolean mass)
			throws MessagingException, IOException {

		// Set the properties, make a Session from the properties, then make a
		// MimeMessage from the Session. Set the MimeMessage to be from the
		// tycoagent email. Then add recipients based on the String set of
		// receivers input. Then set the ReplyTo to the tycoagent email. Then
		// set the text body to the input plus the tyco innovation tag. Then
		// create the Gmail Message
		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", "localhost");
		Session session = Session.getDefaultInstance(properties);
		MimeMessage mimeMessage = new MimeMessage(session);

		InternetAddress t = new InternetAddress(agentEmail);
		mimeMessage.setFrom(t);
		for (String person : msgRecipients) {
			if (person.equals("no email")) {
				System.out.println("error with recipient, no email");
			} else {
				if (mass == false) {
					mimeMessage.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(person));
				} else {
					mimeMessage.addRecipient(javax.mail.Message.RecipientType.BCC, new InternetAddress(person));
				}

			}
		}
		mimeMessage.setSubject(msgSubject);
		mimeMessage.setReplyTo(new Address[] { t });
		if (msgBody == null) {
			msgBody = "Authorization was not given.";
			System.out.println("Null body, not sending: " + msgSubject + " to " + msgRecipients);
			return;
		}
		msgBody += "\n\nA Tyco Innovation Garage© Creation";
		msgBody = HTMLize(msgBody);
		// m.setText(body + "\n\nA Tyco Innovation Garage© Creation", "utf-8");
		mimeMessage.setContent(msgBody, "text/html; charset=utf-8");
		Message msg;
		if (thdID.equals("new")) {
			msg = createMsg(mimeMessage);
		} else {
			msg = createMsg(mimeMessage, thdID);
		}
		try {
			tyco.users().messages().send("me", msg).execute();
		} catch (Exception e) {
			System.out.println("mail not sent officially");
			e.printStackTrace(System.out);
		}
	}

	/**
	 * Creates a Gmail message from a MimeMessage via ByteArrayOutputStream when
	 * it needs to join a thread
	 * 
	 * @param mimemsg
	 *            MimeMessage to be converted to Gmail message
	 * @param thdID
	 *            - thread ID of thread this message needs to join
	 * @return Message to be sent
	 * @throws MessagingException
	 * @throws IOException
	 */
	private Message createMsg(MimeMessage mimemsg, String thdID) throws MessagingException, IOException {

		// Use a BAOS to take the MimeMessage and output to the encoded string
		// version of the Gmail Message. Then set the thread ID, and put the
		// Gmail Message in the tyco agent's correct thread
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		mimemsg.writeTo(baos);
		String encodedEmail = Base64.encodeBase64URLSafeString(baos.toByteArray());
		Message message = new Message();
		message.setRaw(encodedEmail);
		message.setThreadId(thdID);
		tyco.users().threads().get("me", thdID).execute().put(message.getId(), message);
		return message;
	}

	/**
	 * 
	 * @param mimemsg
	 *            - MimeMessage to be converted to Gmail message
	 * @return Message to be sent
	 * @throws MessagingException
	 * @throws IOException
	 */
	private Message createMsg(MimeMessage mimemsg) throws MessagingException, IOException {

		// Use a BAOS to take the MimeMessage and output to the encoded string
		// version of the Gmail Message.
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		mimemsg.writeTo(baos);
		String encodedEmail = Base64.encodeBase64URLSafeString(baos.toByteArray());
		Message message = new Message();
		message.setRaw(encodedEmail);
		return message;
	}

	/**
	 * simply puts the text received into a more html-friendly form -- this is
	 * needed because in order for SearchResp to properly display links to the
	 * events, the email needs to be in html form
	 * 
	 * @param body
	 *            - text to be html'd
	 * @return
	 */
	private String HTMLize(String body) {
		body = body.replaceAll("\n", "<br>");
		return "<p>" + body + "</p>";
	}

}