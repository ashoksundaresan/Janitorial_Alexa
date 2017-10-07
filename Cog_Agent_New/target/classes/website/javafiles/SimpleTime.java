package website.javafiles;

import agentbackend.responsegeneration.calendar.help.TimeConverter;

public class SimpleTime {
	
	public static void main(String[] args) {
		System.out.println(TimeConverter.timeToHour(args[0]));
	}
	
}
