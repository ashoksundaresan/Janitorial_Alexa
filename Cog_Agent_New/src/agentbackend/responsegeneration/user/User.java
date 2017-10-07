package agentbackend.responsegeneration.user;

import com.google.api.services.calendar.Calendar;

import agentbackend.database.MySQLConnector;
import external.google_api.calendar.CalendarAuthorize;

/**
 * This User class is created from an email address of someone signed up for the
 * JCI Agent service. If the email address is not in the Database, most of the
 * fields will become null, causing this thread to be terminated down the line
 * in the workflow
 * 
 * @author nikhilchakravarthy
 *
 */
public class User {

	private String email;
	private String timezone;
	private String refreshToken;

	private String firstName;
	private String lastName;
	private Calendar calendar;

	private Preferences userPreferences;

	/**
	 * Constructor to get all the data from the MySQL Database
	 * @param emailAddress
	 * @param sqlConnection
	 */
	public User(String emailAddress, MySQLConnector sqlConnection) {
		MySQLConnector data = sqlConnection;
		email = emailAddress;
		timezone = (data.getUserTimezone(email));
		refreshToken = data.getUserRefreshToken(email);
		firstName = data.getFirstName(email);
		lastName = data.getLastName(email);
		userPreferences = (new Preferences(emailAddress, sqlConnection));

		calendar = (new CalendarAuthorize()).getCalendarService(refreshToken);
	}

	public String getEmail() {
		return email;
	}

	public Calendar getCalendar() {
		return calendar;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public Preferences getUserPreferences() {
		return userPreferences;
	}

	public String getTimezone() {
		return timezone;
	}

}
