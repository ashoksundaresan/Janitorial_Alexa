package agentbackend.responsegeneration.user;

import java.util.ArrayList;
import java.util.List;

import agentbackend.database.MySQLConnector;

/**
 * Preferences object that stores a user's preferences from the MySQL Database
 * @author nikhilchakravarthy
 *
 */
public class Preferences {

	private int busyHoursAmount;
	private String workStartTime;
	private String workEndTime;
	private int defaultMeetingDuration;
	private static final String morning_afternoon = "12:00:00";
	private static final String afternoon_evening = "17:00:00";

	public Preferences(String user, MySQLConnector sqlConnection) {
		busyHoursAmount = (sqlConnection.getBusyAmt(user));
		workStartTime = sqlConnection.getPrefStart(user).substring(0, 8);
		workEndTime = sqlConnection.getPrefEnd(user).substring(0, 8);
		defaultMeetingDuration = (sqlConnection.getDefaultDuration(user));

	}

	/**
	 * generates 3 arrays of Strings, put in an array the arrays represent
	 * morning, afternoon, evening times respectively, split by 12 pm and 5 pm
	 * 
	 * @return
	 */
	public String[][] generateGoodTimes() {


		String[] goodTimes = generateTimes();

		int firstDiv = -1;
		int secDiv = goodTimes.length;
		for (int i = 0; i < goodTimes.length; i++) {
			if (goodTimes[i].equals(morning_afternoon)) {
				firstDiv = i;
			}
			if (goodTimes[i].equals(afternoon_evening)) {
				secDiv = i;
			}
		}
		String[] morning = new String[0];
		String[] afternoon = new String[0];
		String[] evening = new String[0];

		if (firstDiv != -1) {
			morning = putTimesIn(goodTimes, 0, firstDiv);
		}
		if (secDiv != goodTimes.length) {
			afternoon = putTimesIn(goodTimes, firstDiv, secDiv);
			evening = putTimesIn(goodTimes, secDiv, goodTimes.length);
		} else {
			afternoon = putTimesIn(goodTimes, firstDiv, secDiv);
		}

		return new String[][] { morning, afternoon, evening };
	}

	/**
	 * helper method
	 * 
	 * @param goodTimes
	 *            - times to put in the String[]
	 * @param start
	 *            - start index
	 * @param end
	 *            - end index
	 * @return String []
	 */
	private String[] putTimesIn(String[] goodTimes, int start, int end) {
		// System.out.println("start: " + start);
		// System.out.println("end: " + end);
		String[] temp = new String[end - start];
		int i = 0;
		for (int j = start; j < end; j++) {
			temp[i] = goodTimes[j];
			i++;
		}
		return temp;

	}

	/**
	 * Generates String[] of all good times in the day (30 min increments of
	 * times in the workday)
	 * 
	 * @return String []
	 */
	private String[] generateTimes() {

		int startHr = Integer.parseInt(workStartTime.substring(0, 2));
		int startMin = Integer.parseInt(workStartTime.substring(3, 5));
		int endHr = Integer.parseInt(workEndTime.substring(0, 2));
		int endMin = Integer.parseInt(workEndTime.substring(3, 5));
		List<String> goodTimes = new ArrayList<String>();

		int tempHr = startHr;
		int tempMin = startMin;
		while (tempHr != endHr || tempMin != endMin) {
			goodTimes.add(createStringTime(tempHr, tempMin));
			if (tempMin == 0) {
				tempMin = 30;
			} else {
				tempMin = 0;
				tempHr++;
			}
		}
		goodTimes.add(createStringTime(tempHr, tempMin));

		return goodTimes.toArray(new String[0]);
	}

	/**
	 * makes a time in hh:mm:ss format from int hour and int min
	 * 
	 * @param tempHr
	 * @param tempMin
	 * @return String timeform
	 */
	private String createStringTime(int tempHr, int tempMin) {
		String hr = "";
		String min = "";
		if (tempHr < 10) {
			hr += "0" + tempHr;
		} else {
			hr += tempHr;
		}
		if (tempMin == 0) {
			min += "0" + tempMin;
		} else {
			min += tempMin;
		}
		return hr + ":" + min + ":00";
	}

	public int getBusyHoursAmount() {
		return busyHoursAmount;
	}

	public int getDefaultMeetingDuration() {
		return defaultMeetingDuration;
	}

	public String getWorkStartTime() {
		return workStartTime;
	}

	public String getWorkEndTime() {
		return workEndTime;
	}
}
