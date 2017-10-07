package agentbackend.responsegeneration.calendar.help;

import java.time.*;
import java.time.format.DateTimeFormatter;


/**
 * 
 * @author nikhilchakravarthy Class used for time conversion
 */
public class TimeConverter {

	/**
	 * 
	 * @param time
	 *            - time in "YYYY-MM-DDTHH:MM:SS:mmm" as a String
	 * @return String of hour, day, month, dayofmonth ex: 9AM, TUESDAY, AUGUST
	 *         21
	 */
	public static String convert(String time) {
		int year = Integer.parseInt(time.substring(0, 4));

		int month = Integer.parseInt(time.substring(5, 7));

		int dayM = Integer.parseInt(time.substring(8, 10));
		// Make ints to make it clear how I am making the LocalDateTime
		if (time.contains("T")) {

			int hour = Integer.parseInt(time.substring(11, 13));

			int min = Integer.parseInt(time.substring(14, 16));

			LocalDateTime n = LocalDateTime.of(year, month, dayM, hour, min);

			// Set the hr String to either the n hour + AM or the n hour-12 + PM
			String hr = "";
			if (n.getHour() < 12) {
				hr += n.getHour();
				if (n.getMinute() != 0) {
					hr += ":" + n.getMinute();
				}
				hr += "AM";
			} else {
				if (n.getHour() == 12) {
					hr += 12;
				} else {
					hr += n.getHour() - 12;
				}
				if (n.getMinute() != 0) {
					hr += ":" + n.getMinute();
				}
				hr += "PM";
			}

			// Simply get the day of the week
			String day = n.getDayOfWeek().toString();
			day = day.charAt(0) + day.substring(1).toLowerCase();
			String monthT = n.getMonth().toString();
			monthT = monthT.charAt(0) + monthT.substring(1).toLowerCase();

			return (hr + " on " + day + ", " + monthT + " " + n.getDayOfMonth());
		} else {
			LocalDate n = LocalDate.of(year, month, dayM);

			// Simply get the day of the week
			String day = n.getDayOfWeek().toString();
			day = day.charAt(0) + day.substring(1).toLowerCase();
			String monthT = n.getMonth().toString();
			monthT = monthT.charAt(0) + monthT.substring(1).toLowerCase();

			return (" is an all-day event on " + day + ", " + monthT + " " + n.getDayOfMonth());
		}
	}

	/**
	 * 
	 * @param day
	 *            - String for day name in quotes. ex: "\"Monday\""
	 * @param time
	 *            - String for time with AM/PM in quotes. ex: "\"11PM\""
	 * @return date-time string in "YYYY-MM-DDTHH:MM:SS" form at the next
	 *         instance of that day and time
	 */
	public static String convert(String day, String time) {

		// Initialize with current time
		LocalDateTime n = LocalDateTime.now();

		if (day.contains("tomorrow")) {
			n = n.plusDays(1);
			// n = n.minusNanos(n.getNano());
			n = n.minusMinutes(n.getMinute());
			// n = n.minusSeconds(n.getSecond());
			return n.toString().substring(0, 19);
		}

		// Iterate by adding days until the day of n is the desired day from the
		// day variable
		while (!day.toUpperCase().contains((n.getDayOfWeek().toString()))) {
			n = n.plusDays(1);
		}

		// If the time is AM, get the integer from the set substring, and
		// iterate the n hour forwards or backwards until it equals the hour. If
		// the time is PM, add 12 to the integer from the substring, then
		// iterate the hours forwards or backwards. If there is just a number,
		// get the number, then iterate the hours until they match the number
		if (time == null) {
			n.minusHours(n.getHour());

		} else {
			if (time.toUpperCase().contains("AM")) {
				int t = Integer.parseInt(time.substring(1, time.length() - 3));
				if (n.getHour() > t) {
					while (n.getHour() > t) {
						n = n.minusHours(1);
					}
				} else {
					while (n.getHour() < t) {
						n = n.plusHours(1);
					}
				}
			} else {
				int t;
				if (time.length() > 4) {
					t = Integer.parseInt(time.substring(1, time.length() - 3));
				} else {
					t = Integer.parseInt(time.substring(1, time.length() - 1));
				}
				if (n.getHour() > 12 + t) {
					while (n.getHour() > 12 + t) {
						n = n.minusHours(1);
					}
				} else {
					while (n.getHour() < 12 + t) {
						n = n.plusHours(1);
					}
				}
			}
		}
		// set the minutes, seconds, nanoseconds to 0
		n = n.minusMinutes(n.getMinute());
		n = n.minusSeconds(n.getSecond());
		n = n.minusNanos(n.getNano());

		// add a :00 to the end since the seconds are 0
		return (n.toString() + ":00").substring(0, 19);
	}

	/**
	 * converts day like "tomorrow" or "Tuesday" to a string form yyyy-mm-dd
	 * 
	 * @param day
	 * @return
	 */
	public static String convertDay(String day) {

		// Initialize with current time
		LocalDateTime n = LocalDateTime.now();

		if (day.contains("tomorrow")) {
			n = n.plusDays(1);
			// n = n.minusNanos(n.getNano());
			n = n.minusMinutes(n.getMinute());
			// n = n.minusSeconds(n.getSecond());
			return n.toString().substring(0, 10);
		}

		// Iterate by adding days until the day of n is the desired day from the
		// day variable
		while (!day.toUpperCase().contains((n.getDayOfWeek().toString()))) {
			n = n.plusDays(1);
		}

		return n.toString().substring(0, 10);
	}

	/**
	 * converts times with :30 in them to String of hh:mm:ss.lll
	 * 
	 * @param time
	 * @return
	 */
	private static String convert30Time(String time) {
		LocalTime n = fixToHour(time);

		// System.out.println("here: " + n.toString());
		// fix minutes to 30
		while (n.getMinute() < 30) {
			// System.out.println("plus at " + n.getMinute());
			n = n.plusMinutes(1);
		}
		while (n.getMinute() > 30) {
			// System.out.println("minus at " + n.getMinute());
			n = n.minusMinutes(1);
		}
		n = n.minusSeconds(n.getSecond());
		n = n.minusNanos(n.getNano());
		return n.toString() + ":00";

	}

	/**
	 * get the localtime to the correct hour of the time
	 * 
	 * @param time
	 * @return
	 */
	private static LocalTime fixToHour(String time) {
		time = time.replace(":30", "");
		return fromStandard(convertTime(time));
	}

	/**
	 * converts time from 7PM to hh:mm:ss.lll
	 * 
	 * @param time
	 * @return
	 */
	public static String convertTime(String time) {
		LocalDateTime n = LocalDateTime.now();
		if(!time.contains("\"")) {
			time = "\"" + time+"\"";
		}
		if (time.contains(":30")) {
			time = time.replace("[", "").replace("]", "");
			return convert30Time(time);
		}
		if (time.contains("[")) {
			if (time.toUpperCase().contains("AM")) {
				int t = Integer.parseInt(time.substring(2, time.length() - 4));
				if (n.getHour() > t) {
					while (n.getHour() > t) {
						n = n.minusHours(1);

					}
				} else {
					while (n.getHour() < t) {
						n = n.plusHours(1);

					}
				}
			} else {
				int t;
				if (time.length() >= 4) {
					t = Integer.parseInt(time.substring(2, time.length() - 4));
				} else {
					t = Integer.parseInt(time.substring(2, time.length() - 2));
				}
				if (n.getHour() > 12 + t) {
					while (n.getHour() > 12 + t) {
						n = n.minusHours(1);

					}
				} else {
					while (n.getHour() < 12 + t) {
						n = n.plusHours(1);

					}
				}
			}
		} else {
			if (time.toUpperCase().contains("AM")) {
				int t = Integer.parseInt(time.substring(1, time.length() - 3));

				if (t == 12) {
					t = 0;
				}
				if (n.getHour() > t) {
					while (n.getHour() > t) {

						n = n.minusHours(1);

					}
				} else {
					while (n.getHour() < t) {
						n = n.plusHours(1);

					}
				}
			} else {
				int t;
				if (time.length() >= 4) {
					t = Integer.parseInt(time.substring(1, time.length() - 3));
				} else {
					t = Integer.parseInt(time.substring(1, time.length() - 1));
				}
				int check;
				// fix 24:00 error (never reach)
				if (12 + t == 24) {
					check = 12;
				} else {
					check = t + 12;
				}
				if (n.getHour() > check) {
					while (n.getHour() > check) {
						n = n.minusHours(1);
						// System.out.println(n.getHour());

					}
				} else {
					while (n.getHour() < check) {
						n = n.plusHours(1);
						// System.out.println(n.getHour());

					}
				}
			}
		}
		n = n.minusMinutes(n.getMinute());
		n = n.minusSeconds(n.getSecond());
		n = n.minusNanos(n.getNano());
		return n.toString().substring(11, n.toString().length()) + ":00";
	}

	/**
	 * converts date to the corresponding day
	 * 
	 * @param dateForm
	 *            yyyy:mm:dd
	 * @return
	 */
	public static String dateToDay(String dateForm) {

		int year = Integer.parseInt(dateForm.substring(0, 4));

		int month = Integer.parseInt(dateForm.substring(5, 7));

		int day = Integer.parseInt(dateForm.substring(8, 10));

		LocalDate ld = LocalDate.of(year, month, day);
		String blankday = ld.getDayOfWeek().toString().toLowerCase();
		blankday = blankday.substring(0, 1).toUpperCase() + blankday.substring(1);
		return blankday;
	}

	/**
	 * converts time to the correct 12-hour base hour
	 * 
	 * @param timeForm
	 *            hh:mm:ss
	 * @return
	 */
	public static String timeToHour(String timeForm) {

		int hr = Integer.parseInt(timeForm.substring(0, 2));
		int min = Integer.parseInt(timeForm.substring(3, 5));
		String hour = "";
		String minute = min + "";
		if (hr >= 12) {
			if (hr - 12 > 0) {

				hour += (hr - 12);
			} else {
				hour = "12";
			}
			if (min == 0) {
				return hour + "PM";
			} else {
				return hour + ":" + minute + "PM";
			}
		} else {
			if (hr > 0) {

				hour += hr;
			} else {
				hour = "12";
			}
			if (min == 0) {
				return hour + "AM";
			} else {
				return hour + ":" + minute + "AM";
			}
		}

	}

	/**
	 * returns just the hour and minute in hh:mm:ss.lll
	 * 
	 * @param time
	 * @return
	 */
	private static LocalTime fromStandard(String time) {
		// Make ints to make it clear how I am making the LocalDateTime
		// System.out.println("this: " + time);
		int hour = Integer.parseInt(time.substring(0, 2));
		int minute = Integer.parseInt(time.substring(3, 5));

		return LocalTime.of(hour, minute);

	}

	/**
	 * 
	 * @param date
	 *            in dd
	 * @return date in yyyy-mm-dd
	 */
	public static String getDate(String date, String month) {
		date = date.replace("\"", "");
		month = month.replace("\"", "");
		if (month == null || month.length() < 1) {

			return getDateNoMonth(date);
		} else if (date.length() > 0 && month.length() > 0) {

			return getDateWithMonth(date, month);
		} else
			return "input_error";
	}

	/**
	 * returns the date in yyyy-mm-dd format when month is given
	 * 
	 * @param date
	 * @param month
	 * @return
	 */
	private static String getDateWithMonth(String date, String month) {
		int dateNum = Integer.parseInt(date);
		int monthNum = Integer.parseInt(month);
		int year;
		if (monthNum < LocalDate.now().getMonthValue()) {
			year = LocalDate.now().getYear() + 1;
		} else {
			year = LocalDate.now().getYear();
		}
		return LocalDate.of(year, monthNum, dateNum).toString();

	}

	/**
	 * returns the date in yyyy-mm-dd format when no month is given assumes the
	 * very next instance of that date
	 * 
	 * @param date
	 * @param month
	 * @return
	 */
	private static String getDateNoMonth(String date) {
		int dateNum = Integer.parseInt(date);
		LocalDate n = LocalDate.now();
		while (n.getDayOfMonth() != dateNum) {
			n = n.plusDays(1);
		}
		return n.toString();
	}

	/**
	 * not used yet
	 * 
	 * @param date
	 * @return
	 */
	public static boolean isNextWeek(String date) {
		return false;
	}

	/**
	 * finds # of days to the date given from the current date
	 * 
	 * @param targetDate
	 * @return
	 */
	public static int calculateDaysTo(String targetDate) {
		LocalDate n = LocalDate.now();
		int counter = 0;
		int year = Integer.parseInt(targetDate.substring(0, 4));
		int month = Integer.parseInt(targetDate.substring(5, 7));
		int day = Integer.parseInt(targetDate.substring(8, 10));
		while (n.getYear() != year || n.getMonthValue() != month || n.getDayOfMonth() != day) {
			n = n.plusDays(1);
			counter++;
		}
		return counter;
	}
	
	public static boolean isEventSearchedByTime(String eventStart, String eventEnd, String searchTime) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		String start = eventStart.substring(0,19);
		String end = eventEnd.substring(0,19);
		System.out.println("start: " +start);
		System.out.println("end: "+ end);
		System.out.println("searchTime: " + searchTime);
		LocalDateTime eventStartTime = LocalDateTime.parse(start, formatter).minusMinutes(1);
		LocalDateTime eventEndTime = LocalDateTime.parse(end, formatter).plusMinutes(1);
		LocalDateTime queryTime = LocalDateTime.parse(searchTime, formatter);
		if(eventStartTime.isBefore(queryTime) && eventEndTime.isAfter(queryTime)) {
			return true;
		}
		else return false;
		
	}
	
	public static String readableDate(String date) {
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		
		LocalDate ld = LocalDate.parse(date, formatter);
		
		String day = ld.getDayOfWeek().toString();
		day = day.charAt(0) + day.substring(1).toLowerCase();
		String month = ld.getMonth().toString();
		month = month.charAt(0) + month.substring(1).toLowerCase();
		return day + ", " + month + " " + ld.getDayOfMonth();
	}

//	public static void main(String[] args) {
//		System.out.println(readableDate(getDate("23","")));
//	}
}