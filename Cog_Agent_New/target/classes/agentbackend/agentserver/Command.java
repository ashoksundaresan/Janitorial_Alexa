package agentbackend.agentserver;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import agentbackend.database.MySQLConnector;
import email.emailhandling.Mail;

/**
 * Command object that is the basis of the request and response. Can be turned
 * into a json string to send as bytes from client to server
 * 
 * @author nikhilchakravarthy
 *
 */
public class Command {

	private String message;

	private String userEmail;

	private String from;

	private String subject;

	private String id;

	private String threadId;

	private long internalDate;

	private int type;

	private String response;

	private boolean fromAgent;

	private Map<String, String> recipients, invitees;

	private String intent;

	/**
	 * takaes in a mail object and email address of the corresponding user of
	 * that email. EX: if the email was part of a chain setting up a meeting on
	 * Bob's calendar, Bob's email would be the String passed in
	 * 
	 * @param mail
	 * @param user
	 */
	public Command(Mail mail, String user) {

		userEmail = user;

		type = 0;
		message = mail.body();
		from = mail.from();
		subject = mail.subject();
		id = mail.id();
		threadId = mail.threadID();
		internalDate = mail.internalDate();
		fromAgent = mail.fromAgent();
		recipients = mail.recipients();
		invitees = mail.invitees();

	}

	/**
	 * turns this Command object into a JSON string with all the fields represented
	 * @return
	 * @throws JSONException
	 */
	public String jsonifyCommand() throws JSONException {
		JSONObject json = new JSONObject();
		json.accumulate("type", this.getCommandType()).accumulate("message", getMessage()).accumulate("from", getFrom())
				.accumulate("email", getUserEmail());
		json.accumulate("subject", getSubject()).accumulate("threadId", getThreadId()).accumulate("id", getId())
				.accumulate("internalDate", getInternalDate());
		json.accumulate("recipients", getRecipients()).accumulate("invitees", getInvitees())
				.accumulate("fromAgent", getFromAgent()).accumulate("response", getResponse())
				.accumulate("intent", getIntent());

		return json.toString();
	}

	/**
	 * recreates a Command object from a Command json string
	 * @param jsonString
	 */
	public Command(String jsonString) {
		System.out.println("json to read:\n" + jsonString);
		JSONObject json;
		try {
			json = new JSONObject(jsonString);
			message = json.getString("message");
			type = json.getInt("type");
			if (type == 0) {

				userEmail = json.getString("email");
				from = json.getString("from");
				subject = json.getString("subject");
				id = json.getString("id");
				threadId = json.getString("threadId");
				internalDate = json.getLong("internalDate");
				recipients = remap(json.get("recipients").toString());
				invitees = remap(json.get("invitees").toString());
				fromAgent = json.getBoolean("fromAgent");
			} else if (type == 1) {

				userEmail = MySQLConnector.getUserEmailFromAmazonEmail(json.getString("alexaemail"));

			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			json = null;
			message = "json exception";
			userEmail = null;
		}

	}

	public String getMessage() {
		return message;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public String getFrom() {
		return from;
	}

	public Command setResponse(String response) {
		this.response = response;
		return this;
	}

	public String getResponse() {
		return this.response;
	}

	public int getCommandType() {
		return type;
	}

	public long getInternalDate() {
		return internalDate;
	}

	public String getThreadId() {
		return threadId;
	}

	public String getId() {
		return id;
	}

	public String getSubject() {
		return subject;
	}

	public boolean getFromAgent() {
		return fromAgent;
	}

	public Map<String, String> getRecipients() {
		return recipients;
	}

	public Map<String, String> getInvitees() {
		return invitees;
	}

	/**
	 * takes the json representation of a Map object and turns it back into an actual map object
	 * @param map
	 * @return
	 */
	private Map<String, String> remap(String map) {
		map = map.substring(1, map.length() - 1);
		Map<String, String> list = new HashMap<String, String>();
		String[] people = map.split(",");
		for (String person : people) {
			String[] sections = person.split(":");
			list.put(sections[0].replaceAll("\"", ""), sections[1].replaceAll("\"", ""));
		}

		return list;
	}

	public String getIntent() {
		return intent;
	}

	public void setIntent(String intent) {
		this.intent = intent;
	}

}
