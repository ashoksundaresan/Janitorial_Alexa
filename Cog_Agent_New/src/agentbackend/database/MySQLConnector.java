package agentbackend.database;

import java.util.List;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * the MySQL connection class that acts as the easiest way to do stuff in the
 * database for the agent
 * 
 * @author nikhilchakravarthy
 *
 */
public class MySQLConnector {
	private static final String url = "jdbc:mysql://127.0.0.1:3306/Tyco_Agent_Schema?autoReconnect=true&useSSL=false";
	private static final String user = "root";
	private static final String password = "CognitiveAgentDB" + "";
	private static final String comma = "', '";
	private static final String userTable = "Users";
	private static final String threadTable = "Threads";
	private static final String eventTable = "Events";

	private Connection connection;
	private Statement statement;
	private ResultSet resultSet;

	/**
	 * constructor that initializes SQL connection
	 */
	public MySQLConnector() {
		initialize();
	}

	/**
	 * closes the MySQL connection
	 */
	public void close() {
		try {
			if (!connection.isClosed()) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * gets last name of the user with that email address
	 * 
	 * @param user
	 *            - email address of user
	 * @return String
	 */
	public String getLastName(String user) {
		try {
			getUserEntry(user);
			resultSet.first();
			return resultSet.getString("last");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("first name fail");
			return "Boss";
		}
	}

	/**
	 * gets first name of the user with that email address
	 * 
	 * @param user
	 *            - email address of user
	 * @return String
	 */
	public String getFirstName(String user) {
		try {
			getUserEntry(user);
			resultSet.first();
			return resultSet.getString("first");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("last name fail");
			return "Boss";
		}
	}

	/**
	 * updates event in our database with that thdid associated with it --
	 * updates the value of the given column with the given value
	 * 
	 * @param thdid
	 * @param updateLabel
	 * @param updateValue
	 */
	public void updateEvent(String thdid, String updateLabel, String updateValue) {
		String query = "update " + eventTable + " set " + updateLabel + " = '" + updateValue + "' where thdid = '"
				+ thdid + "'";
		System.out.println("updating via this sql query:\n" + query);
		try {

			// String query = "update " + eventTable + " set " + updateLabel + "
			// = '" + updateValue + "' where thdid = '"
			// + thdid + "'";
			statement.executeUpdate(query);

		} catch (SQLException e) {
			System.out.println("error with:\n" + query);
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param thdid
	 * @return
	 */
	public String getEventTimezone(String thdid) {
		try {
			getEventEntry(thdid);
			resultSet.first();
			return resultSet.getString("timezone");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("failure, defaulting");
			return "default";
		}
	}

	/**
	 * 
	 * @param thdid
	 * @return
	 */
	public String getEventId(String thdid) {
		try {
			getEventEntry(thdid);
			resultSet.first();
			return resultSet.getString("eventid");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("failure, defaulting");
			return "default";
		}
	}

	/**
	 * 
	 * @param thdid
	 * @return
	 */
	public String getStartTime(String thdid) {
		try {
			getEventEntry(thdid);
			resultSet.first();
			return resultSet.getString("sttime");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("failure, defaulting");
			return "default";
		}
	}

	/**
	 * 
	 * @param thdid
	 * @return
	 */
	public String getLocation(String thdid) {
		try {
			getEventEntry(thdid);
			resultSet.first();
			return resultSet.getString("location");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("failure, defaulting");
			return "default";
		}
	}

	/**
	 * works
	 * 
	 * @param thdid
	 *            - thd id to be checked for existence in table
	 * @return true if exists, false if not
	 */
	public boolean checkEvent(String thdid) {
		try {

			String query = "select thdid from " + eventTable + " where exists (select thdid from " + eventTable
					+ " where thdid = '" + thdid + "')";
			resultSet = statement.executeQuery(query);
			return resultSet.first();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * works
	 * 
	 * @param thdid
	 *            - thd id to get event for
	 * @return result set of entry
	 */
	public ResultSet getEventEntry(String thdid) {
		try {
			if (checkEvent(thdid)) {
				String query = "select * from " + eventTable + " where thdid = '" + thdid + "'";
				resultSet = statement.executeQuery(query);
			} else {
				System.out.println("no event entry");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return resultSet;
	}

	/**
	 * works
	 * 
	 * @param thdid
	 *            - thd id of event to insert
	 * @param eventid
	 *            - event id
	 * @return true if works, false if didn't work
	 */
	public boolean insertEvent(String thdid) {
		try {
			String timezone = getUserTimezone(getThreadUser(thdid));
			String query = "insert into " + eventTable + " (thdid,timezone) values" + " ('" + thdid + comma + timezone
					+ "')";
			statement.executeUpdate(query);
			return checkEvent(thdid);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("failure to insert event");
			return false;
		}
	}

	/**
	 * works
	 * 
	 * @param email
	 *            - email id to get user timezone for
	 * @return timezone in string format
	 */
	public String getUserTimezone(String email) {
		try {
			getUserEntry(email);
			resultSet.first();
			return resultSet.getString("timezone");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("failure, defaulting to 00:00");
			return "00:00";
		}
	}

	/**
	 * initializes connection and statement variables
	 */
	private void initialize() {
		try {
			connection = DriverManager.getConnection(url, user, password);

			statement = connection.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * works
	 * 
	 * @param email
	 *            - email id to be checked for existence in database
	 * @return - true if exists, false if doesn't exist or error
	 */
	public boolean checkUser(String email) {
		try {

			String query = "select id from " + userTable + " where exists (select id from " + userTable
					+ " where id = '" + email + "')";
			resultSet = statement.executeQuery(query);
			return resultSet.first();

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * works
	 * 
	 * @param thdid
	 *            - thdid to be checked
	 * @return true if thdid is there, false otherwise or if error
	 */
	public boolean checkThreadId(String thdid) {
		try {

			resultSet = statement.executeQuery("select id from " + threadTable + " where exists (select id from "
					+ threadTable + " where id = '" + thdid + "')");
			return resultSet.first();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * works
	 * 
	 * @param email
	 *            - user id to search for
	 * @return ResultSet of that email (row)
	 */
	private ResultSet getUserEntry(String email) {
		try {
			if (checkUser(email)) {
				String query = "select * from " + userTable + " where id = '" + email + "'";
				resultSet = statement.executeQuery(query);
			}
			return resultSet;
		} catch (SQLException e) {
			e.printStackTrace();
			return resultSet;
		}
	}

	/**
	 * works
	 * 
	 * @param email
	 *            - user associated with thread
	 * @param thdid
	 *            - thd id to be stored
	 * @return true if successfully inserted entry, false if otherwise or
	 *         failure
	 */
	public boolean addThread(String email, String thdid) {
		try {

			if (!checkThreadId(thdid)) {
				String query = "insert into " + threadTable + " (id, user) values ('" + thdid + comma + email + "')";
				statement.executeUpdate(query);
			} else {
				System.out.println(thdid + " already there");
			}
			return checkThreadId(thdid);

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * works
	 * 
	 * @param thdid
	 *            - thd id to be removed
	 * @return the opposite result of checking the existence of the thd id
	 */
	public boolean removeThread(String thdid) {
		try {

			String query = "delete from " + threadTable + " where id = '" + thdid + "'";
			statement.executeUpdate(query);
			return !checkThreadId(thdid);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * works
	 * 
	 * @param thdid
	 *            - thd id to use to get result set
	 * @return ResultSet before first for that thd id
	 */
	private ResultSet getThreadEntry(String thdid) {
		try {
			if (checkThreadId(thdid)) {
				String query = "select * from " + threadTable + " where id = '" + thdid + "'";
				resultSet = statement.executeQuery(query);
			} else {
				System.out.println("thd id does not exist: " + thdid);
			}

		} catch (SQLException e) {
			e.printStackTrace();

		}
		return resultSet;
	}

	/**
	 * works
	 * 
	 * @param thdid
	 *            - thd id to find user for
	 * @return user in string form for that thd id
	 */
	public String getThreadUser(String thdid) {
		try {
			getThreadEntry(thdid);
			resultSet.first();
			return resultSet.getString("user");
		} catch (SQLException e) {
			e.printStackTrace();
			return "failure";
		}
	}

	/**
	 * works
	 * 
	 * @param email
	 *            - user to get all authorized threads from
	 * @return list of user's authorized strings
	 */
	public List<String> getAllThreads(String email) {
		ArrayList<String> allThdIds = new ArrayList<String>();
		try {

			String query = "select * from " + threadTable + " where user = '" + email + "'";
			resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				allThdIds.add(resultSet.getString("id"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return allThdIds;
	}

	/**
	 * works
	 * 
	 * @param email
	 *            - user to be deleted
	 * @return true if user no longer there, false otherwise or if failure
	 */
	public boolean deleteUser(String email) {
		try {

			String query = "delete from " + userTable + " where id = '" + email + "'";
			statement.executeUpdate(query);
			return !checkUser(email);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * gets date from the event associated with that thdid
	 * 
	 * @param thdid
	 * @return String
	 */
	public String getDate(String thdid) {
		try {
			getEventEntry(thdid);
			resultSet.first();
			return resultSet.getString("date");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("failure, defaulting");
			return "default";
		}
	}

	/**
	 * gets google calendar refresh token for that user
	 * 
	 * @param user
	 * @return String
	 */
	public String getUserRefreshToken(String user) {
		try {
			getUserEntry(user);
			resultSet.first();
			return resultSet.getString("refresh_token");

		} catch (SQLException e) {
			e.printStackTrace();
			return "none";
		}
	}

	/**
	 * gets work start time of that user
	 * 
	 * @param user
	 * @return String
	 */
	public String getPrefStart(String user) {
		try {
			getUserEntry(user);
			resultSet.first();
			return resultSet.getString("startwork");
		} catch (SQLException e) {
			e.printStackTrace();
			return "none";
		}
	}

	/**
	 * gets work end time of that user
	 * 
	 * @param user
	 * @return String
	 */
	public String getPrefEnd(String user) {
		try {
			getUserEntry(user);
			resultSet.first();
			return resultSet.getString("endwork");
		} catch (SQLException e) {
			e.printStackTrace();
			return "none";
		}
	}

	/**
	 * gets the associated user email of that thdid
	 * 
	 * @param thdid
	 * @return String
	 */
	public String getThreadUserEmail(String thdid) {

		try {
			initialize();
			String query = "select * from " + threadTable + " where id = '" + thdid + "'";
			ResultSet set = statement.executeQuery(query);
			set.first();
			return set.getString("user");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "none";
		}

	}

	/**
	 * returns true if the thdid is in our database
	 * 
	 * @param thdid
	 * @return
	 */
	public boolean threadIdExists(String thdid) {
		try {
			Statement state = tempInit();
			String query = "select id from " + threadTable + " where exists (select id from " + threadTable
					+ " where id = '" + thdid + "')";
			ResultSet set = state.executeQuery(query);
			return set.first();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * returns true if the user is in our database
	 * 
	 * @param email
	 * @return
	 */
	public boolean userExists(String email) {
		try {
			Statement state = tempInit();
			String query = "select id from " + userTable + " where exists (select id from " + userTable
					+ " where id = '" + email + "')";
			ResultSet set = state.executeQuery(query);
			return set.first();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * puts the thdid into our database, associating it with the given user
	 * email address
	 * 
	 * @param email
	 * @param thdid
	 */
	public void authThread(String email, String thdid) {
		try {
			Statement state = tempInit();
			if (!threadIdExists(email)) {
				String query = "insert into " + threadTable + " (id, user) values ('" + thdid + comma + email + "')";
				state.executeUpdate(query);
			} else {
				System.out.println(thdid + " already there");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * helper method for the static method initializations
	 * 
	 * @return Statement (sql object)
	 */
	private static Statement tempInit() {
		try {
			Connection tempConn = DriverManager.getConnection(url, user, password);
			Statement tempState = tempConn.createStatement();
			return tempState;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * gets how long that user's default duration is
	 * 
	 * @param user
	 * @return int
	 */
	public int getDefaultDuration(String user) {
		try {
			getUserEntry(user);
			resultSet.first();
			return resultSet.getInt("default_duration");

		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * gets how many hours that user needs free at a minimum in their workday to
	 * say that it is a viable day to schedule meetings on
	 * 
	 * @param user
	 * @return int
	 */
	public int getBusyAmt(String user) {
		try {
			getUserEntry(user);
			resultSet.first();
			return resultSet.getInt("busycutoff");
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * gets all the email addresses of signed up users in a list object
	 * 
	 * @return List
	 */
	public List<String> allEmailAddresses() {
		List<String> emails = new ArrayList<String>();
		try {
			initialize();
			String query = "Select * from " + userTable;
			resultSet = statement.executeQuery(query);

			while (resultSet.next()) {
				emails.add(resultSet.getString("id"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return emails;
	}

	/**
	 * gets the user's email given their amazon account email
	 * 
	 * @param amazon
	 * @return
	 */
	public static String getUserEmailFromAmazonEmail(String amazon) {
		try {
			System.out.println("started");
			Connection conn = DriverManager.getConnection(url, user, password);
			System.out.println("made conn");
			Statement state = conn.createStatement();
			System.out.println("init");
			String query = "Select * from " + userTable + " where amazon_email = '" + amazon + "'";
			ResultSet set = state.executeQuery(query);
			set.first();
			return set.getString("id");
		} catch (SQLException e) {
			e.printStackTrace();
			return "failure";
		}
	}

}