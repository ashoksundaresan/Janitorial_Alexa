package agentbackend.responsegeneration.calendar;

import java.io.IOException;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

public class Calendar_Update {

	public Calendar_Update(String strttime, String endtime, String location, String timezone, String id,
			Calendar service) {

		update(service, strttime, endtime, location, timezone, id);

	}

	/**
	 * Method that receives new info from constructor and updates the event and
	 * sends invite of updated event to all recipients.
	 * 
	 * @param Credentials,
	 *            Start time of meeting, end time, location, timezone, id of
	 *            event you want to update.
	 * @return Nothing
	 */
	private void update(Calendar service, String strttime, String endtime, String location, String timezone,
			String id) {
		// TODO Auto-generated method stub
		try {
			Event event = service.events().get("primary", id).execute().setLocation(location)
					.setStart(new EventDateTime().setDateTime(new DateTime(strttime)).setTimeZone(timezone))
					.setEnd(new EventDateTime().setDateTime(new DateTime(endtime)).setTimeZone(timezone));

			// System.out.println("UPDATED");
			service.events().update("primary", event.getId(), event).setSendNotifications(true).execute();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
