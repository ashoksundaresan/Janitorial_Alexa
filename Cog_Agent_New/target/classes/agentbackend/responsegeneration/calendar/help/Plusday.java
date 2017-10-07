/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agentbackend.responsegeneration.calendar.help;

import com.google.api.services.calendar.model.Event;

import agentbackend.responsegeneration.calendar.Calendar_Search;
import agentbackend.responsegeneration.user.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import com.google.api.services.calendar.Calendar;

/**
 *
 * @author nihalmaunder
 */
public class Plusday {
	int time;
	public ArrayList<Event> list;

	public Plusday(String searchFrame, User user) throws ParseException {
		System.out.println("starting plusday with searchFrame: " + searchFrame);
		time = new Helper(user).find_time(searchFrame);
		System.out.println("list of events \"time\" created: " + time);
		list = daylist(time, user.getCalendar());
		System.out.println("list of events \"list\" created: " + list.size());
	}

	/**
	 * 
	 * @param Int
	 *            x that descibes how many days from target is today
	 * @return list of events in that time span
	 * @throws ParseException
	 */
	public static ArrayList<Event> daylist(int numberOfDays, Calendar service) throws ParseException {
		// Gets the next 200 events
		System.out.println("starting daylist for: " + numberOfDays + " days");
		Calendar_Search meets = new Calendar_Search(100, service);
		ArrayList<Event> check = new ArrayList<>();
		if (meets.getAllEvents().size() == 0) {
			System.out.println("no events");
			return check;
		}
		// System.out.println("x: " + x);
		String daysfromnow = LocalDate.now().plusDays(numberOfDays + 1).toString();
		// System.out.println(daysfromnow);
		int counter = 0;

		System.out.println("daylist counter: " + counter);
		// System.out.println("meet .getAllEvents() size: " +
		// meets..getAllEvents().size());
		Event e = meets.getAllEvents().get(0);
		// System.out.println(e.getSummary());
		// Start time of given event
		// System.out.println("looking at: " + e.getSummary());
	
		String start_time = e.getStart().getOrDefault("dateTime", counter).toString();
		String start_date;
		// System.out.println("orig start time: " + start_time);

		if (start_time.length() < 3 && e.getStart().getDate() != null) {
			start_date = e.getStart().getDate().toString();
			// System.out.println("date: " + start_date);
			start_time = start_date;
		}
		System.out.println("final start time/date for \"" + e.getSummary() + "\" is: " + start_time);
		// String that can become comparable
		String comparble_dt = start_time.split("T")[0];
		// System.out.println(comparble_dt);
		// System.out.println("start time: " + start_time);
		// walks day by day on calendar and adds all events until we cross
		// target date
		System.out.println("next check: is " + comparble_dt +" before " + daysfromnow);
		
//		if(createDateTime(comparble_dt).before(createDateTime(daysfromnow))) {
//			check.add(e);
//		}
		
		while (createDateTime(comparble_dt).before(createDateTime(daysfromnow))
				&& counter < meets.getAllEvents().size()-1) {
			// Update to next event
			e = meets.getAllEvents().get(counter);
			check.add(e);
			counter++;
			if(counter == meets.getAllEvents().size()) {
				break;
			}
			Event temp = meets.getAllEvents().get(counter);
			System.out.println("daylist counter: " + counter);
			// System.out.println("looking at: " + e.getSummary());
			start_time = temp.getStart().getOrDefault("dateTime", counter).toString();

			// System.out.println("orig start time: " + start_time);

			if (start_time.length() < 3 && temp.getStart().getDate() != null) {
				start_date = temp.getStart().getDate().toString();
				// System.out.println("date: " + start_date);
				start_time = start_date;
			}

			
			comparble_dt = start_time.split("T")[0];
		}
		 System.out.println("number of events in search swath: " + check.size());
		return check;
	}

	/**
	 * Returns specific type of date format necessary to compare
	 * 
	 * @param String
	 *            dt
	 * @return Date format
	 * @throws ParseException
	 */
	private static Date createDateTime(String dateTimeString) throws ParseException {

		// System.out.println("input: " + inputString);
		return new SimpleDateFormat("yyyy-MM-dd").parse(dateTimeString);
	}

}