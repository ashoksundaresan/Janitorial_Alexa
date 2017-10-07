/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agentbackend.responsegeneration;

import com.google.api.services.calendar.model.Event;

import agentbackend.responsegeneration.apiai.NLP_Connector;
import agentbackend.responsegeneration.calendar.help.Helper;
import agentbackend.responsegeneration.calendar.help.Plusday;
import agentbackend.responsegeneration.calendar.help.TimeConverter;
import agentbackend.responsegeneration.user.User;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author nihalmaunder
 */
public class SearchResp {

	/**
	 * Important Note: "timeframe" is general time frame ex. "a few days",
	 * "week" etc. While "day" is a specific day such as "tomorrow", "Monday",
	 * "today", etc.
	 * 
	 * @param ai.resp
	 * @param Calendar
	 *            service
	 * @return String msg
	 * @throws IOException
	 * @throws ParseException
	 *             ^^^^need to work on "until"
	 */
	public static String search(NLP_Connector airesp, User user) throws IOException, ParseException {
		String search = "today";

		String msg = "Hi " + user.getFirstName() + ",\n\nThe following is the list of appointments you requested";
		if (airesp.getParamMap().containsKey("timeframe")) {
			msg += " for " + airesp.getParamMap().get("timeframe").toString().replaceAll("\"", "");
			search = airesp.getParamMap().get("timeframe").toString();
		} else if (airesp.getParamMap().containsKey("day")) {
			search = airesp.getParamMap().get("day").toString();
			msg += " for " + airesp.getParamMap().get("day").toString().replaceAll("\"", "");
		} else if (airesp.getParamMap().containsKey("date")) {
			if (airesp.getParamMap().containsKey("month")) {
				search = TimeConverter.getDate(airesp.getParamMap().get("date").toString(),
						airesp.getParamMap().get("month").toString());
				msg += " for "
						+ TimeConverter.readableDate(TimeConverter.getDate(airesp.getParamMap().get("date").toString(),
								airesp.getParamMap().get("month").toString()));
			} else {
				search = TimeConverter.getDate(airesp.getParamMap().get("date").toString(), "");
				msg += " for " + TimeConverter
						.readableDate(TimeConverter.getDate(airesp.getParamMap().get("date").toString(), ""));
			}

		}
		if (airesp.getParamMap().containsKey("sttime")) {
			msg += " at " + airesp.getParamMap().get("sttime");
		}
		msg += ":\n";
		// System.out.println("searching for: " + search);
		// Get the list of the events for the timeframe you want to search
		Plusday listmaker = new Plusday(search, user);
		ArrayList<Event> list = listmaker.list;
		System.out.println("List (" + list.size() + "):");
		for (Event event : list) {
			System.out.println(event.getSummary());
		}
		// If you just want the event for a day instead of multiple days
		if (airesp.getParamMap().containsKey("timeframe")
				&& airesp.getParamMap().get("timeframe").toString().contains("next week")) {
			list = new Helper(user).newlist(list, user, "this week");
		} else if (!airesp.getParamMap().containsKey("timeframe")) {
			list = new Helper(user).newlist(list, user, search);
		}
		// System.out.println("list size for for loop of events: " +
		// list.size());
		if (list.size() == 0) {
			msg = "Hi " + user.getFirstName() + ",\n\nYou have no events in that timeframe. \n\nSincerely,\n\nJCI Agent\n";
			return msg;
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
		List<String> links = new ArrayList<String>();
		for (Event e : list) {
			// System.out.println("at for loop events");
			if (!links.contains(e.getHtmlLink())) {
				links.add(e.getHtmlLink());
				String time;

				if (e.getStart().getOrDefault("dateTime", 0).toString().length() < 3
						&& e.getStart().getDate() != null) {
					// System.out.println("converting just date for \"" +
					// e.getSummary()+"\": " +
					// e.getStart().getDate().toString());
					time = TimeConverter.convert(e.getStart().getDate().toString());
				} else {
					// System.out.println("converting whole time for \"" +
					// e.getSummary()+"\": " +
					// e.getStart().getOrDefault("dateTime", 0).toString());
					time = TimeConverter.convert(e.getStart().getOrDefault("dateTime", 0).toString());
				}
				msg += "\n" + e.getSummary() + " at " + time;
				msg += "\nClick " + stringifyLink(e.getHtmlLink()) + " to get more details\n\n";
			}
		}
		msg += "\nSincerely,\n\nJCI Agent\n";
		return msg;
	}

	private static String stringifyLink(String link) {
		return "<a href=\"" + link + "\">here</a>";
	}

}