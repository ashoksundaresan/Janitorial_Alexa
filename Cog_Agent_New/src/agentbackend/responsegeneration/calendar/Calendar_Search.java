package agentbackend.responsegeneration.calendar;

import java.io.IOException;
import java.util.List;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import com.google.api.services.calendar.Calendar;

public class Calendar_Search {
	private List<Event> allEvents;


	public Calendar_Search(int num, Calendar service) {
		// creates instance of Google Calendar authorizer

		search(num, service);

	}

	/**
	 * Given the number of meetings from now that you want to search it will
	 * return List of events.
	 * 
	 * @param Credentials,
	 *            Number of events you want to search.
	 * @return Nothing
	 */
	private void search(int num, Calendar service) {
		// TODO Auto-generated method stub
		DateTime now = new DateTime(System.currentTimeMillis());
		Events events = new Events();
		try {
			events = service.events().list("primary").setMaxResults(num).setTimeMin(now).setOrderBy("startTime")
					.setSingleEvents(true).execute();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		allEvents = events.getItems();
		if (allEvents.size() == 0) {
			// System.out.println("No upcoming events found.");
		} else {

			for (Event event : allEvents) {
				DateTime start = event.getStart().getDateTime();
				if (start == null) {
					start = event.getStart().getDate();
				}

			}
		}

	}

	public List<Event> getAllEvents(){
		return allEvents;
	}
}
