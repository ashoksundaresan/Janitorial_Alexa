package agentbackend.responsegeneration.calendar.help;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.FreeBusyRequest;
import com.google.api.services.calendar.model.FreeBusyRequestItem;
import com.google.api.services.calendar.model.FreeBusyResponse;

import agentbackend.responsegeneration.user.User;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author nihalmaunder
 */
public class Helper {

	private String[] morning;

	private String[] afternoon;

	private String[] evening;

	public Helper(User user) {
		String[][] times = user.getUserPreferences().generateGoodTimes();
		morning = times[0];
		afternoon = times[1];
		evening = times[2];

	}

	/**
	 * Adds minutes to time in RFC33339 time format
	 * 
	 * @param int
	 *            how many minutes you want to add
	 * @param String
	 *            Start time
	 * @return String time with added minutes
	 */
	public String addMin(int i, String start) {
		// System.out.println("JUJ: " + endtime);
		return ("" + LocalDateTime.parse(start, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")).plusMinutes(i)
				+ ":00").substring(0, 19);
	}

	/**
	 * returns list of a specific day
	 * 
	 * @param ArrayList
	 *            of events till target
	 * @return list of events just on target day
	 * @throws ParseException
	 */
	public ArrayList<Event> newlist(ArrayList<Event> eventList, User user, String search) throws ParseException {
		int y = find_time(search) - 1;
		ArrayList<Event> temp = Plusday.daylist(y, user.getCalendar());

		// System.out.println("temp size: " + temp.size());
		for (int i = 0; i < temp.size(); i++) {
			eventList.remove(0);
		}
		return eventList;
	}

	/**
	 * 
	 * @param Calendar
	 *            service
	 * @param String
	 *            suggested_timeframe
	 * @return List of possible choices to have meeting on
	 * @throws IOException
	 * @throws ParseException
	 */
	public ArrayList<String> findfreetime(User user, String suggested_timeframe) throws IOException, ParseException {
		// String[] suggestions = new String[3];
		LocalDate start = LocalDate.now();
		start = start.plusDays(1);
		if (!suggested_timeframe.equals("")) {
			start = start.plusDays(find_time(suggested_timeframe));
		}
		ArrayList<String> suggestions = new ArrayList<>();
		int counter = 0;
		while (suggestions.size() < 3) {

			if (suggestions.size() < 3 && !freebusyhelper(morning, user, start).contains("busy")) {
				suggestions.add(freebusyhelper(morning, user, start));
			}

			if (suggestions.size() < 3 && !freebusyhelper(afternoon, user, start).contains("busy")) {
				suggestions.add(freebusyhelper(afternoon, user, start));
			}
			// Don't want 3 choices of same day so checks if new day or if
			// either Morning or Afternoon was busy.
			if (suggestions.size() < 2 || counter > 0) {
				if (suggestions.size() < 3 && !freebusyhelper(evening, user, start).contains("busy")) {
					suggestions.add(freebusyhelper(evening, user, start));
				}
			}

			start = start.plusDays(1);
			counter++;

		}

		return suggestions;
	}

	/**
	 * Returns first time available in this section of the day that is free on
	 * Boss's calendar
	 * 
	 * @param String[]
	 *            x
	 * @param Calendarservice
	 * @param LocalDate
	 *            start
	 * @return String of available time in RFC time
	 * @throws ParseException
	 * @throws IOException
	 */
	public String freebusyhelper(String[] x, User user, LocalDate start) throws ParseException, IOException {

		for (int i = 0; i < x.length; i++) {
			try {
				String full = start.toString() + "T" + x[i];
				System.out.println("IN: " + full);
				List<FreeBusyRequestItem> freeBusyRequestItemList = new ArrayList<FreeBusyRequestItem>();
				freeBusyRequestItemList.add(new FreeBusyRequestItem().setId("primary"));
				Calendar.Freebusy.Query calendarFreeBusyQuery = user.getCalendar().freebusy()
						.query(new FreeBusyRequest().setTimeMax(getDateTime(addMin(60, full),user.getTimezone()))
								.setTimeMin(getDateTime(full,user.getTimezone())).setItems(freeBusyRequestItemList));
				FreeBusyResponse end = calendarFreeBusyQuery.execute();
				@SuppressWarnings("unchecked")
				List<String> eventList = (List<String>) end.getCalendars().get("primary").get("busy");
				System.out.println(end.getCalendars().get("primary").get("busy"));

				if (eventList.isEmpty()) {
					System.out.println("eventList empty");
					return full;
				}
				Thread.sleep(100);
			} catch (Exception e) {
				System.out.println("exception at: " + i);
				e.printStackTrace();
			}
		}
		return "busy";

	}

	/**
	 * change form
	 * 
	 * @param String
	 *            args
	 * @return Date form of String
	 * @throws ParseException
	 */
	public DateTime getDateTime(String args, String timezone) throws ParseException {
		return new DateTime(args + timezone);

	}

	/**
	 * Important for search delete and finding free time
	 * 
	 * @param String
	 *            search
	 * @return int of how many days target is from now
	 */
	public int find_time(String search) {
		search = search.replaceAll("\"", "");
		System.out.println("search is now: " + search);
		int dayAmount = 0;
		if (search.contains("tomorrow")) {
			dayAmount = 1;
			System.out.println("dayAmount: " + dayAmount + "  search: " + search);
		} else if (search.contains("this week")) {
			dayAmount = wrapweek("SUNDAY");
			System.out.println("dayAmount: " + dayAmount + "  search: " + search);
		} else if (search.contains("a few days")) {
			dayAmount = 3;
			System.out.println("dayAmount: " + dayAmount + "  search: " + search);
		} else if (search.contains("-")) {
			dayAmount = TimeConverter.calculateDaysTo(search);
			System.out.println("dayAmount: " + dayAmount + "  search: " + search);
		} else if (!search.contains("today") && search.contains("day")) {
			dayAmount = wrapweek(search);
			System.out.println("dayAmount: " + dayAmount + "  search: " + search);
		} else if (search.contains("today")) {
			dayAmount = 0;
			System.out.println("dayAmount: " + dayAmount + "  search: " + search);
		} else if (search.contains("next week")) {
			dayAmount = wrapweek("SUNDAY") + 7;
			System.out.println("dayAmount: " + dayAmount + "  search: " + search);
		}
		System.out.println("found dayAmount: " + dayAmount);
		return dayAmount;
	}

	/**
	 * wraps a week ex. if target is Wed. and its Mon. it will return 2 if it
	 * was Tues. it would return 1
	 * 
	 * @param String
	 *            search
	 * @return int of how many days from today is the target
	 */
	public int wrapweek(String search) {
		LocalDate today = LocalDate.now();
		int dayCount = 0;
		while (!today.getDayOfWeek().toString().equals(search.toUpperCase())) {
			today = today.plusDays(1);
			dayCount++;
		}
		return dayCount;
	}

	/**
	 * 
	 * @param service
	 *            - Calendar to search
	 * @param nextWeek
	 *            - is next week or not
	 * @param busyAmt
	 *            - how many hours can be busy before a day is considered "too
	 *            busy"
	 * @return list of
	 * @throws IOException
	 * @throws ParseException
	 */
	public List<String> findfreedays(Calendar service, boolean nextWeek, int busyAmt, String timezone)
			throws IOException, ParseException {
		// String[] suggestions = new String[3];
		LocalDate start;
		if (nextWeek) {
			start = LocalDate.now().plusDays(7);
		} else {
			start = LocalDate.now().plusDays(1);
		}

		List<String> suggestions = new ArrayList<>();
		while (suggestions.size() < 3) {
			List<String> mor = freebusydayhelper(morning, service, start,timezone);
			List<String> aft = freebusydayhelper(afternoon, service, start, timezone);
			List<String> eve = freebusydayhelper(evening, service, start,timezone);

			if ((mor.size() + aft.size() + eve.size()) > busyAmt - 1) {
				suggestions.add(start.toString());
			}
			start = start.plusDays(1);
		}
		System.out.println("suggested days: " + suggestions);
		return suggestions;
	}

	/**
	 * 
	 * @param timesArray
	 * @param service
	 * @param start
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public List<String> freebusydayhelper(String[] timesArray, Calendar service, LocalDate start, String timezone)
			throws ParseException, IOException {
		List<String> totalTimeList = new ArrayList<>();
		for (int i = 0; i < timesArray.length; i++) {
			String full = start.toString() + "T" + timesArray[i];
			// System.out.println("IN: " + full);
			List<FreeBusyRequestItem> itemList = new ArrayList<FreeBusyRequestItem>();
			itemList.add(new FreeBusyRequestItem().setId("primary"));

			Calendar.Freebusy.Query fbq = service.freebusy().query(new FreeBusyRequest()
					.setTimeMax(getDateTime(addMin(60, full),timezone)).setTimeMin(getDateTime(full,timezone)).setItems(itemList));
			FreeBusyResponse end = fbq.execute();
			@SuppressWarnings("unchecked")
			List<String> timeList = (List<String>) end.getCalendars().get("primary").get("busy");
			// System.out.println(end.getCalendars().get("primary").get("busy"));
			// System.out.println(l.isEmpty());
			if (timeList.isEmpty()) {
				totalTimeList.add(full);
			}
		}
		return totalTimeList;
	}

	/**
	 * 
	 * @param user
	 *            - User object
	 * @param date
	 *            - date to check for free times
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	public List<String> findfreedaytime(User user, String date) throws IOException, ParseException {
		// String[] suggestions = new String[3];
		LocalDate start = LocalDate.parse(date);
		List<String> suggestions = new ArrayList<>();
		while (suggestions.size() < 3) {
			if (suggestions.size() < 3 && !freebusyhelper(morning, user, start).contains("busy")) {
				suggestions.add(freebusyhelper(morning, user, start));
			}

			if (suggestions.size() < 3 && !freebusyhelper(afternoon, user, start).contains("busy")) {
				suggestions.add(freebusyhelper(afternoon, user, start));
			}
			// Don't want 3 choices of same day so checks if new day or if
			// either Morning
			// or Afternoon was busy.
			if (suggestions.size() < 3 && !freebusyhelper(evening, user, start).contains("busy")) {
				suggestions.add(freebusyhelper(evening, user, start));
			}
		}

		return suggestions;
	}

	/**
	 * 
	 * @param sttime
	 *            - start time of the event to check if the calendar is free at
	 *            that time
	 * @param service
	 *            - calendar to check
	 * @return true if
	 * @throws ParseException
	 * @throws IOException
	 */
	public boolean free(String sttime, Calendar service, String timezone) throws ParseException, IOException {
		System.out.println("STTIME: " + sttime);

		List<FreeBusyRequestItem> test = new ArrayList<FreeBusyRequestItem>();
		test.add(new FreeBusyRequestItem().setId("primary"));

		Calendar.Freebusy.Query fbq = service.freebusy().query(new FreeBusyRequest()
				.setTimeMax(getDateTime(addMin(60, sttime),timezone)).setTimeMin(getDateTime(sttime,timezone)).setItems(test));
		FreeBusyResponse end = fbq.execute();
		@SuppressWarnings("unchecked")
		List<String> eventList = (List<String>) end.getCalendars().get("primary").get("busy");
		// System.out.println("# events: " + l.size());
		// System.out.println(end.getCalendars().get("primary").get("busy"));
		// System.out.println(l.isEmpty());
		if (eventList.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}
}
