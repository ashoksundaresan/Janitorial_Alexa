package agentbackend.database;

import java.util.List;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.text.ParseException;

/**
 * the MySQL connection class that acts as the easiest way to do stuff in the
 * database for the agent
 * 
 * @author nikhilchakravarthy
 *
 */
public class MySQLConnector_new {
	private static final String url = "jdbc:mysql://127.0.0.1:3306/Janitorial_Service?autoReconnect=true&useSSL=false";
	private static final String user = "root";
	private static final String password = "CognitiveAgentDB" + ""; 
	/* private static final String password = "feedback_agent" + ""; */
	/* private static final String comma = "', '";*/
	private static final String feedbackTable = "Feedback";
	private static final int numMessageCounter = 10;
	
	/* private static final int deviceID = 46;
	private static final String updateLabel = "feedback";
	private static final String updateValue = "Try"; */

	private static Connection connection;
	private static Statement statement;
	private static ResultSet resultSet;

	/**
	 * constructor that initializes SQL connection
	 */
	public MySQLConnector_new() {
		initialize();
	}

	/*
	public static void main(String[] args) {
		 initialize();
		 System.out.println(getTimeStampEntries(45, 48));
		 close();
		
	} */
	
	public static long timeVal(int numHour) {
		
		long calcTime = numHour * 60*60;
		String currentTimeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
		DateFormat df = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
		Date d1=null;
		try {
			d1 = df.parse(currentTimeStamp);
		 	} 
		catch (ParseException e) {
		 // TODO Auto-generated catch block
		 		e.printStackTrace();
		 	}
		long timeVal = Math.abs(d1.getTime()/1000);
		System.out.println("current time:" + timeVal);
		calcTime = timeVal - calcTime;
		System.out.println("cut off time:" + calcTime);
		return calcTime;	
	}
	
	public static long timeStampToSec(String TimeStamp) {
		
		DateFormat df = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
		Date d1=null;
		try {
			d1 = df.parse(TimeStamp);
		 	} 
		catch (ParseException e) {
		 // TODO Auto-generated catch block
		 		e.printStackTrace();
		 	}
		long timeVal = Math.abs(d1.getTime()/1000);
		System.out.println("table entry time:" + timeVal);
		return timeVal;	
	}

	/**
	 * closes the MySQL connection
	 */
	public static void close() {
		try {
			if (!connection.isClosed()) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	

	
	public static void updateEntry(int deviceID, String updateLabel, String updateValue) {
		String entryValue = getEntry(deviceID);
		String newEntry = "";
		System.out.println(entryValue);
		if (entryValue==null) {
			System.out.println(entryValue);
			newEntry = updateValue;
			}
		else
			newEntry = entryValue + ". " + updateValue;
		
		String query = "update " + feedbackTable + " set " + updateLabel + " = '" + newEntry + "' where Row_ID = '"
				+ deviceID + "'";
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
	
	
	public static void insertMultipleEntries(int Restroom_ID, String Sentiment, String feedbackEntry) {
		int currentRowID = getRowID() + 1;
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
		feedbackEntry = feedbackEntry.replace("'", "\'");
		String query = "insert into " + feedbackTable + " (Row_ID,feedback,Type,Time_Stamp,Restroom_ID) Values (" + currentRowID + "," + "'" + feedbackEntry + "'" + "," + "'" + Sentiment + "'" + "," + "'" + timeStamp + "'"+ "," + Restroom_ID + ")";
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


	public static void clearEntry(int deviceID, String updateLabel) {
		
		String newEntry = "";
	
		String query = "update " + feedbackTable + " set " + updateLabel + " = '" + newEntry + "' where ID = '"
				+ deviceID + "'";
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
	
	public static void deleteEntries(int bathroomID) {
		
		String newEntry = "";

		String query = "DELETE FROM " + feedbackTable + " where Restroom_ID = " + bathroomID;
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
	 * works
	 * 
	 * @param deviceID
	 *            - deviceID to be checked for existence in table
	 * @return true if exists, false if not
	 */
	public static boolean checkEntry(int deviceID) {
		try {

			String query = "select Restroom_ID from " + feedbackTable + " where exists (select Restroom_ID from " + feedbackTable
					+ " where Restroom_ID = '" + deviceID + "')";
			resultSet = statement.executeQuery(query);
			return resultSet.first();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static int getRowID() {
		try {

			String query = "select * from " + feedbackTable + " order by Row_ID desc limit 1";
			resultSet = statement.executeQuery(query);
			boolean res= resultSet.first();
			if(!res) return 0;
			return resultSet.getInt("Row_ID");
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * works
	 * 
	 * @param thdid
	 *            - thd id to get event for
	 * @return result set of entry
	 * @throws SQLException 
	 */
	public static String getEntry(int deviceid) {
		String feedbackString = "Test";
		try {
			if (checkEntry(deviceid)) {
				String query = "select * from " + feedbackTable + " where Row_ID = " + deviceid;
				resultSet = statement.executeQuery(query);
				resultSet.first();
				feedbackString = resultSet.getString("Feedback");
			} else {
				System.out.println("no event entry");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return feedbackString;
	}
	
	public static String getMultipleEntries(int bathroomID) {
		String feedbackString = "";
		boolean isEntry = true;
		int counter = 0;
		try {
				String query = "select * from " + feedbackTable + " where Restroom_ID = " + bathroomID;
				resultSet = statement.executeQuery(query);
				isEntry = resultSet.last();
				while (isEntry && (counter < numMessageCounter)){
				feedbackString = feedbackString + resultSet.getString("Feedback") + ". ";
				isEntry = resultSet.previous();
				counter = counter + 1;
				}
			} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		return feedbackString;
	}
	
	public static String getTimeStampEntries(int bathroomID, int numHours) {
		String feedbackString = "";
		String timeStampString ="";
		boolean isEntry = true;
		long timeValue = timeVal(numHours);
		long timeStampVal = 0;
		try {
				String query = "select * from " + feedbackTable + " where Restroom_ID = " + bathroomID;
				resultSet = statement.executeQuery(query);
				isEntry = resultSet.last();
				timeStampString = resultSet.getString("Time_Stamp");
				timeStampVal = timeStampToSec(timeStampString);
				while (isEntry && (timeValue < timeStampVal)){
					feedbackString = feedbackString + resultSet.getString("Feedback") + ". ";
					isEntry = resultSet.previous();
					if(isEntry){
						timeStampString = resultSet.getString("Time_Stamp");
						timeStampVal = timeStampToSec(timeStampString);
					}
				}
			} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		return feedbackString;
	}
	
	public static String getTimeStampTypeEntries(int bathroomID, int numHours, String type) {
		String feedbackString = "";
		String timeStampString ="";
		boolean isEntry = true;
		long timeValue = timeVal(numHours);
		long timeStampVal = 0;
		try {
				String query = "select * from " + feedbackTable + " where Restroom_ID = " + bathroomID;
				resultSet = statement.executeQuery(query);
				isEntry = resultSet.last();
				timeStampString = resultSet.getString("Time_Stamp");
				timeStampVal = timeStampToSec(timeStampString);
				while (isEntry && (timeValue < timeStampVal)){
					if (resultSet.getString("Type").equals(type))
						feedbackString = feedbackString + resultSet.getString("Feedback") + ". ";
					isEntry = resultSet.previous();
					if(isEntry){
						timeStampString = resultSet.getString("Time_Stamp");
						timeStampVal = timeStampToSec(timeStampString);
					}
				}
			} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		return feedbackString;
	}
	
	public static String getGoodEntries(int bathroomID) {
		String feedbackString = "";
		boolean isEntry = true;
		int counter = 0;
		try {
				String query = "select * from " + feedbackTable + " where Restroom_ID = " + bathroomID;
				resultSet = statement.executeQuery(query);
				isEntry = resultSet.last();
				while (isEntry && (counter < numMessageCounter)){
					System.out.println("Type:" + resultSet.getString("Type") );
					if (resultSet.getString("Type").equals("good"))
						feedbackString = feedbackString + resultSet.getString("Feedback") + ". ";
					isEntry = resultSet.previous();
					counter = counter + 1;
					System.out.println("feedbackString" + feedbackString);
				}
			} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		return feedbackString;
	}
	
	public static String getBadEntries(int bathroomID) {
		String feedbackString = "";
		boolean isEntry = true;
		int counter = 0;
		try {
				String query = "select * from " + feedbackTable + " where Restroom_ID = " + bathroomID;
				resultSet = statement.executeQuery(query);
				isEntry = resultSet.last();
				while (isEntry && (counter < numMessageCounter)){
					System.out.println("Type:" + resultSet.getString("Type") );
					if (resultSet.getString("Type").equals("bad"))
						feedbackString = feedbackString + resultSet.getString("Feedback") + ". ";
					isEntry = resultSet.previous();
					counter = counter + 1;
					System.out.println("feedbackString" + feedbackString);
				}
			} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		return feedbackString;
	}
	
	public static boolean getSentiment(int bathroomID) {
		boolean isEntry = true;
		int counterGood = 0;
		int counterBad = 0;
		int counter = 0;
		try {
				String query = "select * from " + feedbackTable + " where Restroom_ID = " + bathroomID;
				resultSet = statement.executeQuery(query);
				isEntry = resultSet.last();
				while (isEntry && (counter < numMessageCounter)){
					System.out.println("Type:" + resultSet.getString("Type") );
					if (resultSet.getString("Type").equals("bad"))
						counterBad = counterBad + 1;
					if (resultSet.getString("Type").equals("good"))
						counterGood = counterGood + 1;
					isEntry = resultSet.previous();
					counter = counter + 1;
					System.out.println("counterGood:" + counterGood);
					System.out.println("counterBad:" + counterBad);
				}
			} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		if (counterGood >= counterBad)
			return true;
		else
			return false;
	}

	/**
	 * initializes connection and statement variables
	 */
	private static void initialize() {
		try {
			connection = DriverManager.getConnection(url, user, password);

			statement = connection.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}