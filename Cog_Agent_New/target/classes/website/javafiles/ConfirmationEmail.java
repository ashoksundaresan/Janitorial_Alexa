package website.javafiles;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import agentbackend.database.MySQLConnector;
import agentbackend.responsegeneration.user.User;
import email.emailsending.EmailCreater;
import external.google_api.gmail.GmailAuthorize;

public class ConfirmationEmail {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MySQLConnector sqlConnection = new MySQLConnector();
		try {
			System.setOut(new PrintStream(new File("initialemail.txt")));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		User user = new User(args[0], sqlConnection);
		System.out.println("user made: " + args[0]);
		String link = "<a href=\"http://ec2-34-208-176-12.us-west-2.compute.amazonaws.com/apache/index.html\">main website</a>";
		String msg = "Hi " + user.getFirstName()
				+ ",\n\nIf you received this email, that means you have been successfully signed up as a user of JCI Agent! Send emails to me when you want to do things on your calendar. Examples are \"What's on my schedule for the week?\" or \"Cancel all my events tomorrow\".";
		msg += " To set up meetings with someone else, just copy me on the chain. Then I will correspond with everyone on that email thread to set up the meeting. \n\nIf you have anymore questions, contact support from our "
				+ link + ".\n\nSincerely,\nJCI Agent";
		System.out.println("starting email sending");
		try {
			new EmailCreater("Confirmation", msg, user.getEmail(), "new", GmailAuthorize.getGmailService());

			System.out.println("ending email sending");
		} catch (Exception e) {
			System.out.println("email failure");
			e.printStackTrace(System.out);
		}finally{
			sqlConnection.close();
		}

	}

}
