package agentbackend.responsegeneration.calendar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;

import agentbackend.database.MySQLConnector;
import agentbackend.responsegeneration.user.User;

public class Calendar_Insert {

	private String eventId;
	private Event event;
	public Calendar_Insert(int attendiesnum, String strttime, String endtime, String location, String title,
			String timezone, ArrayList<String> attendeenames, String thdid,User user, MySQLConnector sqlConnection) {

		eventId = add(attendiesnum, user.getCalendar(), strttime, endtime, location, title, timezone, attendeenames, thdid,
				sqlConnection);
	}

	/**
	 * Creates new event and sends invite to all applicable recipients.
	 * 
	 * @param Number
	 *            of attendees, Credentials, Start time of meeting, end time,
	 *            location, title of event, timezone, and list of attendee
	 *            emails.
	 * @return Nothing
	 */

	private String add(int attennum, Calendar service, String strttime, String endtime, String location, String title,
			String timezone, ArrayList<String> attendeenames, String thdid, MySQLConnector sqlConnection) {
		// TODO Auto-generated method stub
		Event event = new Event().setSummary(title).setLocation(location).setDescription(title);

	
		
		event.setStart(new EventDateTime().setDateTime(new DateTime(strttime)).setTimeZone(timezone));

		
		event.setEnd(new EventDateTime().setDateTime(new DateTime(endtime)).setTimeZone(timezone));

		EventAttendee[] attendees = new EventAttendee[attennum];
		for (int i = 0; i < attennum; i++) {
			attendees[i] = new EventAttendee().setEmail(attendeenames.get(i));
		}
		event.setAttendees(Arrays.asList(attendees));



		event.setReminders(new Event.Reminders().setUseDefault(false)
				.setOverrides(Arrays.asList(new EventReminder[] {
						new EventReminder().setMethod("email").setMinutes(24 * 60),
						new EventReminder().setMethod("popup").setMinutes(15), })));

		try {
			event = service.events().insert("primary", event).setSendNotifications(true).execute();
			sqlConnection.updateEvent(thdid, "eventid", event.getId());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			this.event = service.events().get("primary", event.getId()).execute();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return this.event.getId();

	}
	public String getEventId(){
		return eventId;
	}
	public Event getEvent() {
		return event;
	}

}