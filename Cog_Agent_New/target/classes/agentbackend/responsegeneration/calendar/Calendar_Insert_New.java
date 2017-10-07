package agentbackend.responsegeneration.calendar;

import java.io.IOException;

import java.util.Arrays;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;

import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;


import agentbackend.responsegeneration.user.User;

public class Calendar_Insert_New {

	private String eventId;
	private Event event;
	
	
	public Calendar_Insert_New(String strttime, String endtime, String location, String title,
			String timezone, User user) {

		add(user.getCalendar(), strttime, endtime, location, title, timezone);
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

	private void add(Calendar service, String strttime, String endtime, String location, String title,
			String timezone) {
		// TODO Auto-generated method stub
		Event event = new Event().setSummary(title).setLocation(location).setDescription(title);

	
		
		event.setStart(new EventDateTime().setDateTime(new DateTime(strttime)).setTimeZone(timezone));

		
		event.setEnd(new EventDateTime().setDateTime(new DateTime(endtime)).setTimeZone(timezone));

		
		event.setAttendeesOmitted(true);


		try {
			event = service.events().insert("primary", event).setSendNotifications(false).execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
		

	}
	public String getEventId(){
		return eventId;
	}
	public Event getEvent() {
		return event;
	}

}