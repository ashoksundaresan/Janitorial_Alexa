package agentbackend.responsegeneration.calendar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;

import agentbackend.responsegeneration.calendar.help.TimeConverter;
import email.emailsending.EmailCreater;
import external.google_api.gmail.GmailAuthorize;

public class Calendar_Delete {

	public Calendar_Delete(String calendarId, Calendar service) throws MessagingException {

		try {
			delete(calendarId, service);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Method that takes in id from constructor and deletes event scheduled.
	 * 
	 * @param Credentials
	 *            and id of event you want to delete.
	 * @return Nothing
	 * @throws MessagingException
	 */
	private void delete(String id, Calendar service) throws IOException, MessagingException {
		// TODO Auto-generated method stub
		Event currentEvent = service.events().get("primary", id).execute();
		service.events().delete("primary", id).execute();

		if (currentEvent.getAttendees() != null) {
			List<String> emails = new ArrayList<String>();
			for (EventAttendee person : currentEvent.getAttendees()) {
				emails.add(person.getEmail());
			}

			String[] dateTime = currentEvent.getStart().toString()
					.substring(13, currentEvent.getStart().toString().length() - 2).split("T");

			String body = "Hello,\n\nMy boss " + currentEvent.getOrganizer().getDisplayName()
					+ " has canceled the event: \"" + currentEvent.getSummary() + "\", which would have started at "
					+ TimeConverter.timeToHour(dateTime[1].substring(0, 8)) + " on "
					+ TimeConverter.dateToDay(dateTime[0]) + ".\n\nSincerely,\nTycoAgent";
			String subj = "Event Cancellation Notification for: " + currentEvent.getSummary();
			new EmailCreater(subj, body, emails, "new", GmailAuthorize.getGmailService());
		}

	}

}
