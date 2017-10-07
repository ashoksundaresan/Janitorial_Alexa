package external.google_api.calendar;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialRefreshListener;
import com.google.api.client.auth.oauth2.TokenErrorResponse;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;

/**
 * This is based on the Google API Calendar Quickstart code found here:
 * https://developers.google.com/google-apps/calendar/quickstart/java
 * 
 * It has been changed a lot. It now simply creates a credential from a refresh
 * token (from MySQL Database) and the calendar_secret_web.json. It's main use
 * is to get the Google Calendar service of a User. Unlike the GmailAuthorize
 * class, which uses a StoredCredential file, this class only needs the refresh
 * token
 * 
 * @author nikhilchakravarthy
 *
 */
public class CalendarAuthorize {
	/**
	 * Build and return an authorized Calendar client service.
	 * 
	 * @return an authorized Calendar client service
	 * @throws IOException
	 */
	public Calendar getCalendarService(String refreshToken) {

		try {
			return new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(),
					JacksonFactory.getDefaultInstance(), makeCredential(refreshToken))
							.setApplicationName("TycoAgentCalendar").build();
		} catch (GeneralSecurityException | IOException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
			return null;
		}
	}

	/**
	 * creates the calendar credential object via the calendar secret .json file
	 * and the refresh token from the SQL database
	 * 
	 * @return Credential object
	 */
	private Credential makeCredential(String refreshToken) {
		// TODO Auto-generated method stub
		try {
			InputStream in = CalendarAuthorize.class.getResourceAsStream("calendar_secret_web.json");
			System.out.println("refresh: " + refreshToken);
			
			GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(),
					new InputStreamReader(in));
			
			/*GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
			clientSecrets.set("client_id", "497755334803-ijnnggsvvp8ukk9kcab733sfvu19r4uj.apps.googleusercontent.com");
			clientSecrets.set("project_id", "gmailcontroller-168518");
			clientSecrets.set("auth_uri", "https://accounts.google.com/o/oauth2/auth");
			clientSecrets.set("token_uri", "https://accounts.google.com/o/oauth2/token");
			clientSecrets.set("auth_provider_x509_cert_url", "https://www.googleapis.com/oauth2/v1/certs");
			clientSecrets.set("client_secret", "Rzy12pRmH2wlLGeFCaRcQ8X5");
			clientSecrets.set("redirect_uris", new ArrayList().add("http://ec2-34-208-176-12.us-west-2.compute.amazonaws.com/apache/secondaryauth.php"));*/

			System.out.println("After reading client secrets");

			GoogleCredential credential = new GoogleCredential.Builder()
					.setJsonFactory(JacksonFactory.getDefaultInstance())
					.setTransport(GoogleNetHttpTransport.newTrustedTransport())
					.addRefreshListener(new MyCredentialRefreshListener()).setClientSecrets(clientSecrets).build();
			credential.setRefreshToken(refreshToken);
			if (!credential.refreshToken()) {
				throw new RuntimeException("Failed OAuth to refresh the token");

			}

			return credential;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * quick listener for Credential creation
	 * 
	 * @author nikhilchakravarthy
	 *
	 */
	class MyCredentialRefreshListener implements CredentialRefreshListener {

		@Override
		public void onTokenErrorResponse(Credential arg0, TokenErrorResponse arg1) throws IOException {
			System.out.println(arg1);

		}

		@Override
		public void onTokenResponse(Credential arg0, TokenResponse arg1) throws IOException {
			System.out.println("Credential was refreshed successfully!");

		}
	}
}
