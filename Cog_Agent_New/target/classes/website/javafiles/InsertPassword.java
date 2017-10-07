package website.javafiles;

/**
 *
 * @author nikhilchakravarthy
 *
 */
public class InsertPassword {

	
	/**
	 * arg[0] is email address, arg[1] is standard-text password
	 * @param args
	 */
	public static void main(String[] args) {

		SQL_Connection.insertUserPW(args[0], args[1]);
	}
	

}
