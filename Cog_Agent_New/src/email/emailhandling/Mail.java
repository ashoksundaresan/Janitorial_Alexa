package email.emailhandling;

import java.util.Map;
/**
 * 
 * @author nikhilchakravarthy
 * simple interface for mail, should extend or trash
 * 
 */
public interface Mail {
	
	Map<String, String> recipients();

	Map<String, String> invitees();
	
	String from();
	
	boolean fromAgent();
	
	String subject();
	
	String id();
	
	String body();
	
	String threadID();
	
	long internalDate();
	

}
