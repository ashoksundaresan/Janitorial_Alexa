/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
*/
package agentbackend.responsegeneration;

import email.emailhandling.Mail;

import java.util.*;

import agentbackend.database.MySQLConnector;
import agentbackend.responsegeneration.apiai.NLP_Connector;
import agentbackend.responsegeneration.calendar.Calendar_Update;
import agentbackend.responsegeneration.calendar.help.Helper;
import agentbackend.responsegeneration.calendar.help.TimeConverter;
import agentbackend.responsegeneration.user.User;

import java.io.IOException;
import java.text.ParseException;

/**
 *
 * @author nihalmaunder
 */
public class UpdateResp {

	/**
	 * Important Note: right now we are using a dummy database which is a map we
	 * need to create an actual database that I will be pulling the events from.
	 * Furthermore we need to store information of each event in individual
	 * dictionaries and keep updating those dictionaries. This method can be
	 * easily change to work once we have the database that stores dictionary
	 * and event according to unique key of thread ID of message
	 *
	 * @param ai.response
	 * @param message
	 *            m
	 * @return String of response
	 *
	 */

	public static String update(NLP_Connector airesp, User user, Mail m) throws IOException, ParseException {
		String msg = "";
		// String old_sttime = sqlConnection.getDate(m.threadID()) + "T" +
		// sqlConnection.getStartTime(m.threadID());
		MySQLConnector sqlConnection = new MySQLConnector();

		if (sqlConnection.checkEvent(m.threadID())) {
			runUpdate(airesp, m.threadID(), user, sqlConnection);

		} else {
			sqlConnection.insertEvent(m.threadID());
			runUpdate(airesp, m.threadID(), user, sqlConnection);
		}
		System.out.println("thread id: " + m.threadID());
		String timezone = user.getTimezone();
		String new_location = sqlConnection.getLocation(m.threadID());
		String new_date = sqlConnection.getDate(m.threadID());
		String sttime = sqlConnection.getDate(m.threadID()) + "T" + sqlConnection.getStartTime(m.threadID());
		Helper help = new Helper(user);
		System.out.println("start time: " + sttime);
		String endtime = help.addMin(user.getUserPreferences().getDefaultMeetingDuration(), sttime);
		if (help.free(sttime, user.getCalendar(), user.getTimezone())) {
			System.out.println("going to actual update");
			msg = "Hi,\n\nThank you so much for the notice. The event has been properly changed. You will soon recieve "
					+ "an invite of the new updated event.\n\n";
			msg += "\nSincerely,\n\nJCI Agent\n";
			new Calendar_Update(sttime + timezone, endtime + timezone, new_location,
					sqlConnection.getEventTimezone(m.threadID()), sqlConnection.getEventId(m.threadID()), user.getCalendar());
		} else {
			System.out.println("going to pushed update");
			List<String> arr = help.findfreedaytime(user, new_date);
			// ArrayList<String> arr = Helper.findfreedaytime(service, new_date,
			// old_time);
			System.out.println("recommended times: " + arr);
			msg = "Hi,\n\nUnfortunately that time does not work for " + user.getFirstName() + ". On "
					+ TimeConverter.dateToDay(new_date) + ", " + user.getFirstName() + " is free around "
					+ TimeConverter.timeToHour(arr.get(0).split("T")[1]) + ", "
					+ TimeConverter.timeToHour(arr.get(1).split("T")[1]) + ", "
					+ TimeConverter.timeToHour(arr.get(2).split("T")[1])
					+ ". Let me know if any of those times work for you. \n\n";
			msg += "\nSincerely,\n\nJCI Agent\n";
		}
		System.out.println("UPDATE MSG: " + msg);
		sqlConnection.close();
		return msg;
	}

	/**
	 * updates SQL database with parsed info from API.AI
	 * @param airesp - API.AI response
	 * @param thdid - thd id of event this email wants to update
	 * @param user - User object of the user associated with that thdid
	 */
	private static void runUpdate(NLP_Connector airesp, String thdid, User user, MySQLConnector sqlConnection) {
		
		if (airesp.getParamMap().containsKey("location")) {
			sqlConnection.updateEvent(thdid, "location", airesp.getParamMap().get("location").toString());
		}
		if (airesp.getParamMap().containsKey("sttime")) {
			sqlConnection.updateEvent(thdid, "sttime",
					TimeConverter.convertTime(airesp.getParamMap().get("sttime").toString()));
		}
		if (airesp.getParamMap().containsKey("date")) {
			if (airesp.getParamMap().containsKey("month")) {
				sqlConnection.updateEvent(thdid, "date", TimeConverter.getDate(airesp.getParamMap().get("date").toString(),
						airesp.getParamMap().get("month").toString()));
			} else {
				sqlConnection.updateEvent(thdid, "date",
						TimeConverter.getDate(airesp.getParamMap().get("date").toString(), ""));
			}
		} else if (airesp.getParamMap().containsKey("day")) {
			sqlConnection.updateEvent(thdid, "date", TimeConverter.convertDay(airesp.getParamMap().get("day").toString()));
		}
	}

}