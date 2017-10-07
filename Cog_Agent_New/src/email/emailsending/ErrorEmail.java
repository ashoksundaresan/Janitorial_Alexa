package email.emailsending;

import java.io.IOException;

import javax.mail.MessagingException;

import com.google.api.services.gmail.Gmail;

import email.emailhandling.Mail;
import external.google_api.gmail.GmailAuthorize;
/**
 * This class creates error emails.
 * @NOTE: it is unfinised, add as errors come
 * @author nikhilchakravarthy
 *
 */
public class ErrorEmail {
	private static final String link = "<a href=\"ec2-34-208-176-12.us-west-2.compute.amazonaws.com/apache/index.html\">support</a>";
	private static final String support = "Please contact "+link
			+" with the message this is in reply to for further assistance.";
	private static final String end = "\nI'm very sorry for the inconvenience.\n\nSincerely,\nJCI Agent";
	private static final Gmail gmail = GmailAuthorize.getGmailService();
	
	/**
	 * in progress, should take in an uncaught exception and send an error email
	 * @param exception - Exception thrown
	 * @param mail - mail object that caused the exception
	 * @throws IOException
	 * @throws MessagingException
	 * these exceptions will not be hit, have to through these anyway
	 */
	public static void errorEmail(Exception exception, Mail mail)  {
		String errorMessage = "";
		String error, type = "default";
		error = exception.toString();
		if (error.contains("StringIndexOutOfBoundsException")) {
			type = "emailParse";
			errorMessage = responseCreater(type);
			try {
				new EmailCreater(mail.subject() + ": caused an error", errorMessage, mail.from(), mail.threadID(),gmail);
			} catch (IOException | MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if(error.contains("NumberFormatException")) {
			type = "timeParse";
			errorMessage = responseCreater(type);
			try {
				new EmailCreater(mail.subject() + ": caused an error", errorMessage, mail.from(), mail.threadID(),gmail);
			} catch (IOException | MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			errorEmail(true,mail);
		}
		

		
		
	}

	/**
	 * makes the text for the error email
	 * @param type - type of error email
	 * @return - msg + support and end strings
	 */
	private static String responseCreater(String type) {
		String message = "Hi Boss,\n\n";
		if (type.equals("emailParse")) {
			message += "I had trouble reading the email addresses on this email. " + support + end;
		} else if (type.equals("timeParse")) {
			message += "I ran into a bug in reading the times in this email. " + support + end;
		} else if(type.equals("default")) {
			message += "I ran into an uncommon error while processing this email. " + support + end;
		}
		return message;
	}

	/**
	 * errorEmail for when I am not sure what went wrong
	 * @param unknown - differentiating variable from the other constructor
	 * @param mail - mail object that caused error
	 * @param msg  - msg that I create elsewhere (it is probably referring to things this class can't access)
	 * @throws IOException
	 * @throws MessagingException
	 */
	public static void errorEmail(boolean unknown, Mail mail)  {

		String errorMessage = "Hi Boss,\n\nI'm very sorry but something went very wrong. "+support+end;
		try {
			new EmailCreater(mail.subject(), errorMessage, mail.from(), mail.threadID(),gmail);
			new EmailCreater("api error for" + mail.from(), "sent to api.ai: " + mail.body(), "nikhilchakravarthy@tycoint.com","new",gmail);
		} catch (IOException | MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
