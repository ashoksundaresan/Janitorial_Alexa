package website.javafiles;

/**
 * 
 * @author nikhilchakravarthy
 * should return int 0 or 1
 */
public class LoginFunction {

	/**
	 * args[0] email, args[1] standard-text password
	 * @param args
	 */
	public static void main(String[] args) {

		if(SQL_Connection.checkPassword(args[0], args[1])){
			System.out.println(1);
		} else {
			System.out.println(0);
		}
		
	}

}
