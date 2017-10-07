package website.javafiles;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PHP_Java_SQL {
	private static final String url = "jdbc:mysql://127.0.0.1:3306/Tyco_Agent_Schema?autoReconnect=true&useSSL=false";
	private static final String user = "root";
	private static final String password = "CognitiveAgentDB";

	private static final String userTable = "Users";

	private static Connection connection;
	private static Statement statement;
	
	/**
	 * 
	 * @param args [0] = email; [1] = real password input
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("start");
		List<String> input = new ArrayList<String>(Arrays.asList(args));
		doPrefUpdate(input);
		System.out.println("done");
	}
	
	private static void initialize() {
		try {
			connection = DriverManager.getConnection(url, user, password);
			statement = connection.createStatement();
			System.out.println("initialized");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @param input list of Strings in form {pref0 name, pref0 value, pref1 name, pref1 value,...., email}
	 */
	private static void doPrefUpdate(List<String> input) {
		initialize();
		for(int i = 0; i < input.size()-1;i+=2) {
			System.out.println("update#: " + i);
			doUpdate(input.get(i),input.get(i+1), input.get(input.size()-1));
		}
		System.out.println("all updated");
	}

	private static void doUpdate(String pref, String value, String email) {
		try {
			String query = "update " + userTable + " set " + pref + " = '" + value + "' where id = '" + email + "'";
			statement.executeUpdate(query);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	

}
