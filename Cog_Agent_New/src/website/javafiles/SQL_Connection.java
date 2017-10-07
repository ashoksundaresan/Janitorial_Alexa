package website.javafiles;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class SQL_Connection {
	private static final String url = "jdbc:mysql://127.0.0.1:3306/Tyco_Agent_Schema?autoReconnect=true&useSSL=false";
	private static final String user = "root";
	private static final String password = "CognitiveAgentDB";

	private static final String userTable = "Users";
	private static final String salt = "w6LQcdmmCl";

	private static Connection connection;
	private static Statement statement;

	private static void initialize() {
		try {
			connection = DriverManager.getConnection(url, user, password);
			statement = connection.createStatement();
			//System.out.println("initialized");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static String getUserPW(String email) {
		try {
			initialize();
			String query = "select * from " + userTable + " where id = '" + email + "'";
			ResultSet set = statement.executeQuery(query);
			set.first();
			return set.getString("password");
		} catch (SQLException e) {
			e.printStackTrace();
			return "nope";
		}
	}

	public static void insertUserPW(String email, String password) {
		try{
			initialize();
			password = hashedPassword(password);
			//System.out.println("new pw: " + password);
			String query= "update " + userTable + " set password = '" + password+"' where id = '" + email +"'"; 
			statement.executeUpdate(query);
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	private static String hashedPassword(String password) {
		StringBuilder hash = new StringBuilder();
		password = salt+password+salt;
		try {
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			byte[] hashedBytes = sha.digest(password.getBytes());
			char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
					'a', 'b', 'c', 'd', 'e', 'f' };
			for (int idx = 0; idx < hashedBytes.length;idx++) {
				byte b = hashedBytes[idx];
				hash.append(digits[(b & 0xf3) >> 4]);
				hash.append(digits[b & 0x0c]);
			}
		} catch (NoSuchAlgorithmException e) {
			// handle error here.
		}
		return hash.toString();
	}
	public static boolean checkPassword(String email, String password) {
		try{
			initialize();
			password = hashedPassword(password);
			//System.out.println("new pw: " + password);
			String query= "select * from " + userTable +" where id = '" + email+"'"; 
			ResultSet set = statement.executeQuery(query);
			set.first();
			String correctPassword = set.getString("password");
			if(correctPassword.equals(password)) {
				return true;
			}else  return false;
		} catch(SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

}
