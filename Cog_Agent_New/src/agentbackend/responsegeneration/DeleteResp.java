
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agentbackend.responsegeneration;

import com.google.api.services.calendar.model.Event;

import agentbackend.responsegeneration.apiai.NLP_Connector;
import agentbackend.responsegeneration.calendar.Calendar_Delete;
import agentbackend.responsegeneration.calendar.help.Helper;
import agentbackend.responsegeneration.calendar.help.Plusday;
import agentbackend.responsegeneration.calendar.help.TimeConverter;
import agentbackend.responsegeneration.user.User;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;

import javax.mail.MessagingException;

/**
 *
 * @author nihalmaunder
 */
public class DeleteResp {

	/**
	 * Important Note: Because out API.AI is very week right now we need a lot
	 * of if and else statements to get the info from the sentences. Once heavy
	 * training is done we can cut down the amount of if and else statements.
	 * 
	 * @param ai.response
	 * @return String msg
	 * @throws ParseException
	 * @throws IOException
	 */

	public static String delete(NLP_Connector airesp, User user)
			throws ParseException, IOException, MessagingException {
		// Checks if we are deleting a specific event or many
		String eventsdel = "false";
		if (airesp.getParamMap().containsKey("plural")) {
			eventsdel = airesp.getParamMap().get("plural").toString();
		}
		String msg = "";
		// search default is today ex. Delete my event with Mark (assume today
		// unless told otherwise)
		String search = "today";
		// if you want to delete an event next week
		if (airesp.getParamMap().containsKey("timeframe")) {
			search = airesp.getParamMap().get("timeframe").toString();
		}
		// if you want to delete an event by a certain amount of days ex. Monday
		// to Wednesday
		if (airesp.getParamMap().containsKey("day")) {
			search = airesp.getParamMap().get("day").toString();
		}

		if (airesp.getParamMap().containsKey("date")) {
			if (airesp.getParamMap().containsKey("month")) {
				search = TimeConverter.getDate(airesp.getParamMap().get("date").toString(),
						airesp.getParamMap().get("month").toString());
			} else {
				search = TimeConverter.getDate(airesp.getParamMap().get("date").toString(), "");
			}
		}
		// Get the list of the events for the timeframe you want to search
		Plusday listmaker = new Plusday(search, user);
		ArrayList<Event> list = listmaker.list;
		// If you just want the event for a day instead of multiple days
		if (!airesp.getParamMap().containsKey("timeframe")) {
			list = new Helper(user).newlist(list, user, search);
		}

		if (airesp.getParamMap().containsKey("sttime")) {
			String specific_time = "";
			if (airesp.getParamMap().containsKey("date")) {
				if (airesp.getParamMap().containsKey("month")) {
					specific_time = TimeConverter.getDate(airesp.getParamMap().get("date").toString(),
							airesp.getParamMap().get("month").toString());
				} else {
					specific_time = TimeConverter.getDate(airesp.getParamMap().get("date").toString(), "");
				}

			} else if (airesp.getParamMap().containsKey("day")) {

				specific_time = TimeConverter.convertDay(airesp.getParamMap().get("day").toString());
			} else {
				specific_time = LocalDate.now().toString();
			}
			specific_time += "T" + TimeConverter.convertTime(airesp.getParamMap().get("sttime").toString());

			if (new Helper(user).free(specific_time, user.getCalendar(), user.getTimezone())) {
				msg = "Hi " + user.getFirstName()
						+ ",\n\nYou have no event at this time. \n\nSincerely,\n\nJCI Agent\n";
				return msg;
			} else {
				ArrayList<Event> templist = new ArrayList<Event>();
				for (Event e : list) {
					String startTime, endTime;

					if (e.getStart().getOrDefault("dateTime", 0).toString().length() < 3
							&& e.getStart().getDate() != null) {
						startTime = (e.getStart().getDate().toString());
						endTime = startTime;
						templist.add(e);
					} else {
						startTime = (e.getStart().getOrDefault("dateTime", 0).toString());
						endTime = e.getEnd().getOrDefault("dateTime", 0).toString();
						if (TimeConverter.isEventSearchedByTime(startTime, endTime, specific_time)) {
							templist.add(e);

						}
					}

				}
				list = templist;
			}
		}

		// checks if your deleting singular event and if you are looking for
		// target so it can identify
		// what to delete

		
		// WE NEED TO START USING THIS (only goes to else because we have not even made a "target" entity)
		msg += "Hi " + user.getFirstName() + ",\n\n";
		if (!eventsdel.contains("true") && airesp.getParamMap().containsKey("target")) {
			String target = airesp.getParamMap().get("target").toString();
			for (Event e : list) {
				for (int i = 0; i < e.getAttendees().size(); i++) {
					if (e.getAttendees().get(i).getDisplayName().contains(target)) {
						msg += "The event: " + e.getSummary() + " has been successfully deleted.\n";
						new Calendar_Delete(e.getId(), user.getCalendar());
					}
				}
			}
		} else {
			if (list.size() > 0) {
				msg += "The following events have been deleted:\n\n";
				for (Event e : list) {
					msg += e.getSummary() + "\n\n";
					new Calendar_Delete(e.getId(), user.getCalendar());

				}
			} else {
				msg += "Hi " + user.getFirstName() + ",\n\nYou have no events to delete.\n";
			}
		}
		msg += "\nSincerely,\n\nJCI Agent\n";
		return msg;
	}

}