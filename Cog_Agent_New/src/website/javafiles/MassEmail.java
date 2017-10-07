package website.javafiles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.mail.MessagingException;

import agentbackend.database.MySQLConnector;
import email.emailsending.EmailCreater;

public class MassEmail {

	/**
	 * args[0] is subject Sends email to everyone signed up on the SQL database
	 * as BCC (others can't see the list of emails of ppl signed up)
	 * 
	 * @param args
	 * @throws MessagingException
	 */
	public static void main(String[] args) throws MessagingException {
		String subj = "";
		for (String word : args) {
			subj += word + " ";
		}
		subj = subj.substring(0, subj.length() - 1);

		try {
			File body = new File("/home/ubuntu/TycoAgent/MassEmail/mass_email.txt");
			BufferedReader read = new BufferedReader(new FileReader(body));
			String emailBody = "";
			String line = read.readLine();
			while (line != null) {
				emailBody += line + "\n";
				line = read.readLine();
			}
			System.out.println(emailBody);
			read.close();
			new EmailCreater(subj, emailBody, new MySQLConnector().allEmailAddresses(), true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}