package website.javafiles;

public class TimezoneConvert {

	/**
	 * converts American timezones in +-hh:mm format to a readable format
	 * @param args
	 */
	public static void main(String[] args) {
	
		if(args[0].contains("-7")) {
			System.out.println("America/Los Angeles");
		} else if(args[0].contains("-6")) {
			System.out.println("America/Denver");
		} else if(args[0].contains("-5")) {
			System.out.println("America/Chicago");
		} else if(args[0].contains("-4")) {
			System.out.println("America/New York");
		} else {
			System.out.println("America/Los Angeles");
		}

	}

}
