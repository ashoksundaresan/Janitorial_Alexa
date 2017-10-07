package agentbackend.responsegeneration;

import email.emailhandling.GMail;
import email.emailhandling.Mail;
import email.emailsending.ErrorEmail;

import java.io.IOException;
import java.text.ParseException;

import javax.mail.MessagingException;

import org.json.JSONException;

import agentbackend.agentserver.Command;
import agentbackend.database.MySQLConnector;
import agentbackend.responsegeneration.apiai.NLP_Connector;
import agentbackend.responsegeneration.apiai.Pre_NLP_Parser;
import agentbackend.responsegeneration.user.User;

/**
 *
 * @author nikhilchakravarthy
 */
public class ResponseCreater {

	/**
	 * type codes: 0 = mail, 1 = alexa
	 */
	private int type;
	private String response;
	private NLP_Connector nlpResult;

	private User user;
	private MySQLConnector sqlConn;
	private Command command;
	private Mail mail;
	private String intent;

	@Deprecated
	public ResponseCreater(NLP_Connector airesp, User user, MySQLConnector sqlConnection)
			throws ParseException, MessagingException, IOException {

		try {
			System.out.println("starting response generation");
			System.out.println("from: " + mail.from());
			System.out.println("calendar of: " + user.getEmail());
			System.out.println("thread: " + mail.threadID());
			System.out.println("thread user: " + sqlConnection.getThreadUser(mail.threadID()));

			System.out.println("getIntent() of \"" + mail.subject() + "\" is " + airesp.getIntent());
			// parse by getIntent() detected
			if (airesp.getIntent().equals("fallback")) {
				response = user.getFirstName() + ", \n\n " + airesp.getBadResponse() + " \n\nSincerely,\nJCI Agent\n";
			} else {
				if (airesp.getIntent().equals("delete_calendar") && mail.from().equals(user.getEmail())) {
					response = DeleteResp.delete(airesp, user);
				} else if (airesp.getIntent().equals("search_calendar") && mail.from().equals(user.getEmail())) {
					response = SearchResp.search(airesp, user);
				} else if (airesp.getIntent().equals("update_calendar")) {
					response = UpdateResp.update(airesp, user, mail);
				} else if (airesp.getIntent().equals("insert_calendar")) {
					response = InsertResp.insert(airesp, user, mail);
				} else {
					response = "I cannot do that action.";
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ErrorEmail.errorEmail(e, mail);
		}
	}

	/**
	 * gets the plain textual response of this ResponseCreater
	 * 
	 * @return String response
	 */
	public String getPlainTextResponse() {
		return response;
	}

	/**
	 * creates a ResponseCreater object from a command. If the command is an
	 * email type, set the Response mail object to a new GMail object and set
	 * the values from the Command object. This is because the backend for
	 * insert and update only works with a mail object right now. Otherwise, if
	 * the command is an alexa type, make an empty GMail object that won't be
	 * used, but is just there to prevent it from being null.
	 * 
	 * @param command
	 *            - Command object created from the json written to the backend
	 */
	public ResponseCreater(Command command) {
		try {
			System.out.println("responding to :\n" + command.jsonifyCommand());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.command = command;
		this.type = command.getCommandType();
		if (this.type == 0) {
			this.mail = new GMail().setBody(command.getMessage()).setFrom(command.getFrom())
					.setFromAgent(command.getFromAgent()).setId(command.getId())
					.setInternalDate(command.getInternalDate()).setInvitees(command.getInvitees())
					.setRecipients(command.getRecipients()).setThreadId(command.getThreadId().replaceAll("'", ""));
		} else {
			this.mail = new GMail();
		}

		sqlConn = new MySQLConnector();
		user = new User(command.getUserEmail(), sqlConn);
		sqlConn.close();

	}

	/**
	 * this method executes the NLP_Connector client to send a request to API.AI
	 * to parse the list of sentences, receives the nlpResult and sets it and
	 * the intent of the ResponseCreater
	 * 
	 * @return - this object but with more information
	 */
	public ResponseCreater executeNlpAnalysis() {
		nlpResult = new NLP_Connector(Pre_NLP_Parser.messageListToSend(command.getMessage()));
		intent = nlpResult.getIntent();
		return this;
	}

	/**
	 * this method calls the private method runResponseGeneration that sets this
	 * objects text response, then returns this object
	 * 
	 * @return
	 */
	public ResponseCreater executeResponseCreation() {
		runResponseGeneration();
		return this;
	}

	/**
	 * this object looks at the Command and intent, and based on the Command
	 * type and intent type, selects the type of response to to generate, and
	 * carries out the response generation and action for that intent/Command combo
	 */
	private void runResponseGeneration() {
		try {

			System.out.println("intent is " + nlpResult.getIntent());
			// parse by getIntent() detected
			if (nlpResult.getIntent().equals("failure")) {
				response = "There was a problem processing your request.";
			}
			if (nlpResult.getIntent().equals("fallback")) {
				response = user.getFirstName() + ", \n\n " + nlpResult.getBadResponse()
						+ " \n\nSincerely,\nJCI Agent\n";
			} else {

				if (type == 0) {

					if (nlpResult.getIntent().equals("delete_calendar") && mail.from().equals(user.getEmail())) {
						response = DeleteResp.delete(nlpResult, user);
					} else if (nlpResult.getIntent().equals("search_calendar") && mail.from().equals(user.getEmail())) {
						response = SearchResp.search(nlpResult, user);
					} else if (nlpResult.getIntent().equals("update_calendar")) {
						response = UpdateResp.update(nlpResult, user, mail);
					} else if (nlpResult.getIntent().equals("insert_calendar")) {
						response = InsertResp.insert(nlpResult, user, mail);
					} else {
						response = "I cannot do that action.";
					}
				} else {
					if (nlpResult.getIntent().equals("delete_calendar")) {
						response = DeleteResp.delete(nlpResult, user);
					} else if (nlpResult.getIntent().equals("search_calendar")) {
						response = SearchResp.search(nlpResult, user);

					} else {
						response = "I cannot do that action.";
					}
				}
			}

		} catch (

		Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if (type == 0) {
				ErrorEmail.errorEmail(true, mail);
			}
		}
	}

	/**
	 * gets the intent of this ResponseCreater (only do after executeNlpAnalysis has been run)
	 * @return - String intent
	 */
	public String getMessageIntent() {
		return intent;
	}

}
