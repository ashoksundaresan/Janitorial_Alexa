package email.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.text.ParseException;

import javax.mail.MessagingException;

import org.json.JSONException;

import agentbackend.agentserver.Command;
import agentbackend.database.MySQLConnector;
import email.TycoAgent;
import email.emailsending.EmailCreater;

/**
 * this class acts as a server to get emails and as a client to the backend
 * server
 * 
 * @author nikhilchakravarthy
 *
 */
public class EmailServer {

	private static long lastTime = 0;
	private static final int outputPort = 2003;
	private static final int checkerInterval = 10000;

	private static Socket clientSocket = null;

	private static final String unauthMessage = "Hello,\nThis email is not from an authorized"
			+ " user of JCI Agent or from one of a JCI Agent user's correspondence. Click <a href=\"http://ec2-34-208-176-12.us-west-2.compute.amazonaws.com/apache/index.html\">here</a> to sign up."
			+ "\n\nSincerely,\nJCI Agent";

	/**
	 * grabs emails from assistand.jci@gmail.com acct and sends them to the
	 * tyco_server
	 * 
	 * @param args
	 */

	public static void main(String[] args) {
		// try {
		if (args.length < 1) {
			System.out.println("defaulting to now");
			lastTime = System.currentTimeMillis();

		} else {
			lastTime = new Long(args[0]);
		}
		System.out.println("version 2.0");

		runEmailGatherer(lastTime);

	}

	/**
	 * Create a TycoAgent object, create a MySQLConnection, and gets the new
	 * valid emails using the TycoAgent object every 10 seconds (can be set).
	 * With every batch on new emails, the are put into a Command object, then
	 * the command is sent as a json to the backend server via the client socket
	 * 
	 * @param updateTime
	 *            - time to start checking for emails after in long utc form
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws MessagingException
	 * @throws IOException
	 * @throws MessagingException
	 * @throws ParseException
	 */
	public static void runEmailGatherer(Long updateTime) {

		TycoAgent tyco = new TycoAgent();

		// initialize old time long variable
		long oldTime = updateTime;

		synchronized (tyco) {

			while (true) {
				try {

					// System.out.println("\n\n\nStarting update for emails
					// after: " + oldTime);

					// update the tyco maps, and set old time to the time of the
					// latest emailnew
					oldTime = tyco.updateMap(oldTime);
					lastTime = oldTime;
					// System.out.println("#new emails: " +
					// tyco.getNewEmails().size());
					OutputStream requestStream = null;
					if (tyco.getNewEmails().size() > 0) {
						MySQLConnector sql = new MySQLConnector();
						clientSocket = new Socket("localhost", outputPort);
						requestStream = (clientSocket.getOutputStream());

						Command command;
						for (String threadId : tyco.getNewEmails().keySet()) {
							System.out.println("\n\n");
							boolean userPresent = sql.checkUser(tyco.getNewEmails().get(threadId).from());
							System.out.println(
									"user: " + tyco.getNewEmails().get(threadId).from() + " is in db: " + userPresent);
							boolean threadPresent = sql.checkThreadId(threadId);
							System.out.println("threadId: " + threadId + " is in db: " + threadPresent);
							if (userPresent || threadPresent) {
								System.out.println("authorized");
								if (userPresent && !threadPresent) {
									sql.authThread(tyco.getNewEmails().get(threadId).from(), threadId);

								}

								command = new Command(tyco.getNewEmails().get(threadId), sql.getThreadUser(threadId));
								String json = command.jsonifyCommand() + "-";

								System.out.println("json version of command:\n" + json);

								requestStream.write(json.getBytes());
								System.out.println("written");

							} else {
								try {
									new EmailCreater(tyco.getNewEmails().get(threadId).subject(), unauthMessage,
											tyco.getNewEmails().get(threadId).from(), threadId, tyco.getAgentGmail());
								} catch (MessagingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

						}
						sql.close();
						clientSocket.close();
					}

					// wait 10 seconds b/w checks
					// System.out.println("Now waiting");
					tyco.wait(checkerInterval);
				} catch (IOException e) {

					System.out.println("while loop error, printing exception");
					System.out.println("caught: ");
					e.printStackTrace();

					tyco = null;

					runEmailGatherer(lastTime);

				} catch (InterruptedException e) {
					System.out.println("interrupted");
					e.printStackTrace();
					runEmailGatherer(lastTime);

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					System.out.println("incorrect json format");
					e.printStackTrace();
					runEmailGatherer(lastTime);
				}

			}
		}

	}

}