/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agentbackend.responsegeneration;

import email.emailhandling.Mail;
import email.emailsending.EmailCreater;
import external.google_api.gmail.GmailAuthorize;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import agentbackend.database.MySQLConnector;
import agentbackend.responsegeneration.apiai.NLP_Connector;
import agentbackend.responsegeneration.calendar.Calendar_Insert;
import agentbackend.responsegeneration.calendar.help.Helper;
import agentbackend.responsegeneration.calendar.help.TimeConverter;
import agentbackend.responsegeneration.user.User;

/**
 *
 * @author nihalmaunder
 */
public class InsertResp {

	/**
	 * 
	 * @param ai.response
	 * @param Calendar
	 *            service
	 * @param Messeage
	 *            m
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public static String insert(NLP_Connector airesp, User user, Mail m) throws ParseException, IOException {
		MySQLConnector sqlConnection = new MySQLConnector();
		String msg = "";
		if (sqlConnection.checkEvent(m.threadID())) {
			runUpdate(airesp, m.threadID(), user, sqlConnection);

		} else {
			sqlConnection.insertEvent(m.threadID());
			runUpdate(airesp, m.threadID(), user, sqlConnection);
		}
		Helper help = new Helper(user);
		// Checks if we have all the information necessary to
		if (checkForCompletion(m.threadID(), sqlConnection)
				&& (help.free(sqlConnection.getDate(m.threadID()) + "T" + sqlConnection.getStartTime(m.threadID()),
						user.getCalendar(), user.getTimezone()) || m.from().equals(user.getEmail())))

		{
			System.out.println("all information gathered");
			String sttime = sqlConnection.getDate(m.threadID()) + "T" + sqlConnection.getStartTime(m.threadID());
			System.out.println("start time: " + sttime);
			String endtime = help.addMin(user.getUserPreferences().getDefaultMeetingDuration(), sttime);
			System.out.println("end time: " + endtime);

			String location = sqlConnection.getLocation(m.threadID()).replaceAll("\"", "");

			String timeZone = "";
			if (user.getTimezone().contains("-7")) {
				timeZone = "America/Los_Angeles";
			} else if (user.getTimezone().contains("-6")) {
				timeZone = "America/Denver";
			} else if (user.getTimezone().contains("-5")) {
				timeZone = "America/Chicago";
			} else if (user.getTimezone().contains("-4")) {
				timeZone = "America/New_York";
			} else {
				timeZone = "America/Los_Angeles";
			}
			Map<String, String> names = m.invitees();
			String title = "Meeting at " + location + " with ";

			for (String s : names.keySet()) {
				if (sqlConnection.checkUser(s)) {
					title = title + sqlConnection.getFirstName(s) + ", ";
				} else if (names.get(s).equals("no name")) {

					title = title + s + ", ";
				} else {
					title = title + names.get(s) + ", ";
				}
			}
			title = title.substring(0, title.length() - 2);
			ArrayList<String> attendee = new ArrayList<String>();
			for (String s : names.keySet()) {
				attendee.add(s);
			}
			// AGAIN NEED TO GET TIMEZONE AUTOMATICALLY so "-07:00" can be
			// generated
			// and added automatically
			Calendar_Insert insertion = new Calendar_Insert(attendee.size(), sttime + user.getTimezone(),
					endtime + user.getTimezone(), location.replaceAll("\"", ""), title, timeZone, attendee,
					m.threadID(), user, sqlConnection);
			sqlConnection.updateEvent(m.threadID(), "created", "1");
			msg += "Sounds good. You should've recieved an invite. If there is any issue don't hesitate to shoot me an email.\n\nRegards,\n\nJCI Agent";
			try {

				String message = "The event: " + insertion.getEvent().getSummary() + " has been made. Click "
						+ stringifyLink(insertion.getEvent().getHtmlLink()) + " to go to the event in your calendar.";
				new EmailCreater("Event created: " + insertion.getEvent(), message, user.getEmail(), "new",
						GmailAuthorize.getGmailService());
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			if (sqlConnection.getDate(m.threadID()) != null && sqlConnection.getStartTime(m.threadID()) != null
					&& (!help.free(sqlConnection.getDate(m.threadID()) + "T" + sqlConnection.getStartTime(m.threadID()),
							user.getCalendar(), user.getTimezone())
							|| !help.free(
									help.addMin(user.getUserPreferences().getDefaultMeetingDuration(),
											sqlConnection.getDate(m.threadID())),
									user.getCalendar(), user.getTimezone()))) {

				msg += "Unfortunately, the meeting is set for "
						+ TimeConverter.convert(
								sqlConnection.getDate(m.threadID()) + "T" + sqlConnection.getStartTime(m.threadID()))
						+ ", which does not work for " + user.getFirstName()
						+ ". Please suggest another day that you are free. \n\nSincerely,\n\nJCI Agent";

			} else {
				List<String> arr;
				boolean nextWeek = false;
				if (airesp.getParamMap().containsKey("timeframe")
						&& airesp.getParamMap().get("timeframe").toString().contains("next week")) {
					nextWeek = true;

				}

				String addDate = "";
				String addLoc = "";
				String addTime = "";

				// Get arraylist of possible times meeting can take place.

				// Checks if there was a suggested time from email sender and
				// checks
				// according to that
				// prefrence as well
				// if (airesp.getParamMap().containsKey("day")) {
				// arr = help.findfreetime(user,
				// airesp.getParamMap().get("day").toString());
				// } else {
				// arr = help.findfreetime(user, "");
				// }
				if (sqlConnection.getDate(m.threadID()) == null) {
					arr = help.findfreedays(user.getCalendar(), nextWeek,
							user.getUserPreferences().getBusyHoursAmount(), user.getTimezone());
					addDate += user.getFirstName() + " is available ";
					addDate += TimeConverter.dateToDay(arr.get(0)) + " and " + TimeConverter.dateToDay(arr.get(1))
							+ ". ";
					addDate += "If those days don't work for you, " + user.getFirstName() + " is also available on "
							+ TimeConverter.dateToDay(arr.get(2) + ". \n");
				} else if (sqlConnection.getStartTime(m.threadID()) == null) {
					arr = help.findfreedaytime(user, sqlConnection.getDate(m.threadID()));
					String day = TimeConverter.dateToDay(sqlConnection.getDate(m.threadID()));
					System.out.println(arr);
					addTime += "On " + day + ", " + user.getFirstName() + " is available at "
							+ TimeConverter.timeToHour(arr.get(0).split("T")[1]) + ", at "
							+ TimeConverter.timeToHour(arr.get(1).split("T")[1]) + ", and at "
							+ TimeConverter.timeToHour(arr.get(2).split("T")[1])
							+ ". \nPlease let me know which time works for you. \n";
					System.out.println(arr.get(0) + "   " + arr.get(1) + "    " + arr.get(2) + "     ");
				}
				if (sqlConnection.getLocation(m.threadID()) == null) {
					addLoc += " Where would you prefer this event to be?\n";
				}

				msg += "Hi,\n\n";
				msg += "I have just checked " + user.getFirstName() + "'s calendar. \n";
				msg += addDate + addTime + addLoc;

				msg += "\nSincerely,\n\nJCI Agent\n";
			}
		}

		sqlConnection.close();
		return msg;

	}

	private static void runUpdate(NLP_Connector airesp, String thdid, User user, MySQLConnector sqlConnection) {
		System.out.println("updating mysql for : " + thdid);
		if (airesp.getParamMap().containsKey("location")) {
			sqlConnection.updateEvent(thdid, "location", airesp.getParamMap().get("location").toString());
		}
		if (airesp.getParamMap().containsKey("sttime")) {
			sqlConnection.updateEvent(thdid, "sttime",
					TimeConverter.convertTime(airesp.getParamMap().get("sttime").toString()));
		}
		if (airesp.getParamMap().containsKey("date")) {
			if (airesp.getParamMap().containsKey("month")) {
				sqlConnection.updateEvent(thdid, "date", TimeConverter.getDate(
						airesp.getParamMap().get("date").toString(), airesp.getParamMap().get("month").toString()));
			} else {
				sqlConnection.updateEvent(thdid, "date",
						TimeConverter.getDate(airesp.getParamMap().get("date").toString(), ""));
			}
		} else if (airesp.getParamMap().containsKey("day")) {
			sqlConnection.updateEvent(thdid, "date",
					TimeConverter.convertDay(airesp.getParamMap().get("day").toString()));
		}
	}

	public static String insertAlexa(NLP_Connector airesp, User user, String thdid, MySQLConnector sqlConnection)
			throws ParseException, IOException {
		String msg = "";
		if (sqlConnection.checkEvent(thdid)) {
			runUpdate(airesp, thdid, user, sqlConnection);

		} else {
			sqlConnection.insertEvent(thdid);
			runUpdate(airesp, thdid, user, sqlConnection);
		}
		Helper help = new Helper(user);
		// Checks if we have all the information necessary to
		if (checkForCompletion(thdid, sqlConnection)
				&& help.free(sqlConnection.getDate(thdid) + "T" + sqlConnection.getStartTime(thdid), user.getCalendar(),
						user.getTimezone()))

		{
			System.out.println("all information gathered");
			String sttime = sqlConnection.getDate(thdid) + "T" + sqlConnection.getStartTime(thdid);
			System.out.println("start time: " + sttime);
			String endtime = help.addMin(user.getUserPreferences().getDefaultMeetingDuration(), sttime);
			System.out.println("end time: " + endtime);

			String location = sqlConnection.getLocation(thdid);

			// NEED TO FIND OUT HOW TO AUTOMATICALLY GET TIMEZONE OF USER

			String timeZone = "";
			if (user.getTimezone().contains("-7")) {
				timeZone = "America/Los_Angeles";
			} else if (user.getTimezone().contains("-6")) {
				timeZone = "America/Denver";
			} else if (user.getTimezone().contains("-5")) {
				timeZone = "America/Chicago";
			} else if (user.getTimezone().contains("-4")) {
				timeZone = "America/New_York";
			} else {
				timeZone = "America/Los_Angeles";
			}

			String title = "Meeting with ";

			title = title.substring(0, title.length() - 2);
			ArrayList<String> attendee = new ArrayList<String>();

			// AGAIN NEED TO GET TIMEZONE AUTOMATICALLY so "-07:00" can be
			// generated
			// and added automatically
			new Calendar_Insert(attendee.size(), sttime + user.getTimezone(), endtime + user.getTimezone(),
					location.replaceAll("\"", ""), title, timeZone, attendee, thdid, user, sqlConnection);
			sqlConnection.updateEvent(thdid, "created", "1");
			msg += "Sounds good. You should've recieved an invite. If there is any issue don't hesitate to shoot me an email.\n\nRegards,\n\nJCI Agent";

		} else {
			List<String> arr;
			boolean nextWeek = false;
			if (airesp.getParamMap().containsKey("timeframe")
					&& airesp.getParamMap().get("timeframe").toString().contains("next week")) {
				nextWeek = true;

			}

			String addDate = "";
			String addLoc = "";
			String addTime = "";
			// Get arraylist of possible times meeting can take place.

			// Checks if there was a suggested time from email sender and checks
			// according to that
			// prefrence as well
			// if (airesp.getParamMap().containsKey("day")) {
			// arr = help.findfreetime(user,
			// airesp.getParamMap().get("day").toString());
			// } else {
			// arr = help.findfreetime(user, "");
			// }
			if (sqlConnection.getDate(thdid) == null) {
				arr = help.findfreedays(user.getCalendar(), nextWeek, user.getUserPreferences().getBusyHoursAmount(),
						user.getTimezone());
				addDate += user.getFirstName() + " is available ";
				addDate += TimeConverter.dateToDay(arr.get(0)) + " and " + TimeConverter.dateToDay(arr.get(1)) + ". ";
				addDate += "If those days don't work for you, " + user.getFirstName() + " is also available on "
						+ TimeConverter.dateToDay(arr.get(2) + ".\n");
			} else if (sqlConnection.getStartTime(thdid) == null) {
				arr = help.findfreedaytime(user, sqlConnection.getDate(thdid));
				String day = TimeConverter.dateToDay(sqlConnection.getDate(thdid));
				System.out.println(arr);
				addTime += "On " + day + ", " + user.getFirstName() + " is available at "
						+ TimeConverter.timeToHour(arr.get(0).split("T")[1]) + ", at "
						+ TimeConverter.timeToHour(arr.get(1).split("T")[1]) + ", and at "
						+ TimeConverter.timeToHour(arr.get(2).split("T")[1])
						+ ". \nPlease let me know which time works for you.\n";
				System.out.println(arr.get(0) + "   " + arr.get(1) + "    " + arr.get(2) + "     ");
			}
			if (sqlConnection.getLocation(thdid) == null) {
				addLoc += " Where would you prefer this event to be?\n";
			}

			msg += "Hi,\n\n";
			msg += "I have just checked " + user.getFirstName() + "'s calendar.\n";
			msg += addDate + addTime + addLoc;

			msg += "\nSincerely,\n\nJCI Agent\n";
		}

		return msg;

	}

	private static String stringifyLink(String link) {
		return "<a href=\"" + link + "\">here</a>";
	}

	private static boolean checkForCompletion(String thdid, MySQLConnector sqlConnection) {

		return (sqlConnection.getLocation(thdid) != null && sqlConnection.getStartTime(thdid) != null
				&& sqlConnection.getDate(thdid) != null);

	}
}