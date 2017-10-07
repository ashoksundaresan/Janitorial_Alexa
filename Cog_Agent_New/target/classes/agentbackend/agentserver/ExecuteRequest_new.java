package agentbackend.agentserver;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import agentbackend.database.MySQLConnector_new;
import agentbackend.database.MySQLConnector;
import agentbackend.responsegeneration.user.User;
import agentbackend.nlp.ManualProcessor;
import agentbackend.responsegeneration.ResponseCreater;
import agentbackend.responsegeneration.calendar.Calendar_Insert_New;
import email.emailsending.EmailCreater;

/**
 * Class that takes in a Command object (the request to the server), and carries
 * it out
 * 
 * @author nikhilchakravarthy
 *
 */
public class ExecuteRequest_new {

	/**
	 * Command and Socket objects passed into the Constructor
	 */
	private Command command;
	private Socket socket;
	private String responseString;
	private static final String provideOption = "provide";
	private static final String retrieveOption = "retrieve";
	private static final String stopOption = "stop";
	private static final String resetOption = "reset";
	

	/**
	 * This is the method that runs the command through the backend. If the
	 * command is an email command, the backend sends the email and the socket
	 * is closed. If the command is an alexa command, the backend writes the
	 * response to the socket then closes it. Either way, the backend sees the
	 * command and acts on it and generates a response
	 */
	public void run() {

		System.out.println("starting responseGeneration with apiai");
		command.setResponse(generateResponseAndExecuteRequest().getPlainTextResponse());
		System.out.println("response generated");
		if (command.getCommandType() == 1) {
			System.out.println("sending post");
			OutputStream outputStream;
			try {
				outputStream = socket.getOutputStream();
				outputStream.write(command.getResponse().getBytes());
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.println("sent post");
		} else if (command.getCommandType() == 0) {

			new EmailCreater(command);
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
	
	public void run2() {

		System.out.println("starting responseGeneration with apiai");
		responseString = executeRequestAndGenerateResponse();
		command.setResponse(responseString);
		System.out.println("response generated");
		if (command.getCommandType() == 1) {
			System.out.println("sending post");
			OutputStream outputStream;
			try {
				outputStream = socket.getOutputStream();
				outputStream.write(command.getResponse().getBytes());
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.println("sent post");
		} 

	}
	
	public String executeRequestAndGenerateResponse() {
		String resp1,resp2,respThree, respOne, respTwentyFour, retDB;
		int deviceID = 45;
		final String updateLabel = "feedback";
		resp1="Ok. Please go ahead. ";
		resp2="Ok. Here are the most recent entries. ";
		respThree="Ok. Here are the entries in the last three hours. ";
		respOne="Ok. Here are the entries in the last one hour. ";
		respTwentyFour="Ok. Here are the entries in the last twenty four hours. ";
		String notFollowResp ="Please give your feedback or ask for past feedback. Go ahead. Say stop to exit the session. ";
		String stopResp="Good Bye";
		String recResp="Your feedback has been recorded. ";
		String goodResp = "Thank you for your nice comments. ";
		String badResp = "I am sorry you had this experience. ";
		String clearResp="Feedback entries have been cleared. ";
		String postResp ="The overall sentiment has been positive";
		String negResp ="The overall sentiment has been negative";
	
		MySQLConnector_new testA = new MySQLConnector_new();
		String message = command.getMessage().toLowerCase();
		
		if (message.contains(stopOption)) return stopResp;
		
		if (message.contains("schedule cleaning")) 
			{
			postCalendar();
			return "Cleaning has been scheduled";
			}
		
		if (message.contains(resetOption)){
			testA.deleteEntries(deviceID);
			testA.close();
			return clearResp;
		}
		
		if (message.contains("sentiment")){
			if (testA.getSentiment(deviceID))
				{
				testA.close();
				return postResp;
				}
			else
				{
				testA.close();
				return negResp;
				}
		}
		
		String manualCheckFeedback = ManualProcessor.patternMatchFeedback(message);
		String manualCheckSentiment = ManualProcessor.patternMatchSentiment(message);
		
		System.out.println("manualCheckFeedback:" + manualCheckFeedback);
		System.out.println("manualCheckSentiment:" + manualCheckSentiment);
				
		if ((manualCheckFeedback.equals("provide")) || (manualCheckSentiment.equals("good") && !manualCheckFeedback.equals("retrieve")) || (manualCheckSentiment.equals("bad") && !manualCheckFeedback.equals("retrieve"))){
			if (manualCheckSentiment == "good"){
				System.out.println("deviceID " + deviceID);
				System.out.println("updatelabel " + updateLabel);
				System.out.println("message:" + message);
				testA.insertMultipleEntries(deviceID, "good", message);
				testA.close();
				return goodResp + recResp;
			}
			if (manualCheckSentiment == "bad"){
				System.out.println("deviceID " + deviceID);
				System.out.println("updatelabel " + updateLabel);
				System.out.println("message:" + message);
				testA.insertMultipleEntries(deviceID, "bad", message);
				testA.close();
				return badResp + recResp;
			}
			if (manualCheckSentiment == "neutral") {
				System.out.println("deviceID " + deviceID);
				System.out.println("updatelabel " + updateLabel);
				System.out.println("message:" + message);
				testA.insertMultipleEntries(deviceID, "neutral", message);
				testA.close();
				return recResp;
			}
		}
		if(manualCheckFeedback.equals("retrieve"))
		{ 
			System.out.println("retrieving feedback");
			if ((message.contains("positive")) || (message.contains("good"))){
				
				if (message.contains("last") && message.contains("hour")){
					System.out.println("message contains last and hour");
					if (message.contains("one"))
						{retDB = testA.getTimeStampTypeEntries(deviceID, 1, "good");
						System.out.println("one" + retDB);
						if (retDB == "") retDB = "There is currently no feedback available.";
						System.out.println(respOne + retDB);
						return respOne + retDB;}
					if (message.contains("three"))
						{retDB = testA.getTimeStampTypeEntries(deviceID, 3, "good");
						System.out.println("three" + retDB);
						if (retDB == "") retDB = "There is currently no feedback available.";
						System.out.println(respThree+ retDB);
						return respThree + retDB;}
						
					if (message.contains("twenty four")){
						retDB = testA.getTimeStampTypeEntries(deviceID, 24, "good");
						if (retDB == "") retDB = "There is currently no feedback available.";
						System.out.println(respTwentyFour + retDB);
						return respTwentyFour + retDB;}
					else
						retDB = testA.getGoodEntries(deviceID);
						if (retDB == "") retDB = "There is currently no feedback available.";
						System.out.println(resp2 + retDB);
						return resp2 + retDB;	
				}
				
				retDB = testA.getGoodEntries(deviceID);
				if (retDB == "") retDB = "There is currently no feedback available.";
				System.out.println(resp2 + retDB);
				return resp2 + retDB;
			}
			
			if ((message.contains("negative")) || (message.contains("bad"))) {
				
				if (message.contains("last") && message.contains("hour")){
					System.out.println("message contains last and hour");
					if (message.contains("one"))
						{retDB = testA.getTimeStampTypeEntries(deviceID, 1, "bad");
						System.out.println("one" + retDB);
						if (retDB == "") retDB = "There is currently no feedback available.";
						System.out.println(respOne + retDB);
						return respOne + retDB;}
					if (message.contains("three"))
						{retDB = testA.getTimeStampTypeEntries(deviceID, 3, "bad");
						System.out.println("three" + retDB);
						if (retDB == "") retDB = "There is currently no feedback available.";
						System.out.println(respThree+ retDB);
						return respThree + retDB;}
						
					if (message.contains("twenty four")){
						retDB = testA.getTimeStampTypeEntries(deviceID, 24, "bad");
						if (retDB == "") retDB = "There is currently no feedback available.";
						System.out.println(respTwentyFour + retDB);
						return respTwentyFour + retDB;}
					else
						retDB = testA.getBadEntries(deviceID);
						if (retDB == "") retDB = "There is currently no feedback available.";
						System.out.println(resp2 + retDB);
						return resp2 + retDB;	
				}
				retDB = testA.getBadEntries(deviceID);
				if (retDB == "") retDB = "There is currently no feedback available.";
				System.out.println(resp2 + retDB);
				return resp2 + retDB;
				}
		
			if (message.contains("last") && message.contains("hour")){
				System.out.println("message contains last and hour");
				if (message.contains("one"))
					{retDB = testA.getTimeStampEntries(deviceID, 1);
					System.out.println("one" + retDB);
					if (retDB == "") retDB = "There is currently no feedback available.";
					System.out.println(respOne + retDB);
					return respOne + retDB;}
				if (message.contains("three"))
					{retDB = testA.getTimeStampEntries(deviceID, 3);
					System.out.println("three" + retDB);
					if (retDB == "") retDB = "There is currently no feedback available.";
					System.out.println(respThree+ retDB);
					return respThree + retDB;}
					
				if (message.contains("twenty four")){
					retDB = testA.getTimeStampEntries(deviceID, 24);
					if (retDB == "") retDB = "There is currently no feedback available.";
					System.out.println(respTwentyFour + retDB);
					return respTwentyFour + retDB;}
				else
					retDB = testA.getMultipleEntries(deviceID);
					if (retDB == "") retDB = "There is currently no feedback available.";
					System.out.println(resp2 + retDB);
					return resp2 + retDB;	
			}
			
			retDB = testA.getMultipleEntries(deviceID);
			if (retDB == "") retDB = "There is currently no feedback available.";
			System.out.println(resp2 + retDB);
			return resp2 + retDB;
		
			
			
			/* if more than 10 read out only 10};*/
		}
		
		if (message.contains("help")) return notFollowResp;
		
		if (manualCheckFeedback.equals("unclear")){
			System.out.println("deviceID " + deviceID);
			System.out.println("updatelabel " + updateLabel);
			System.out.println("message:" + message);
			testA.insertMultipleEntries(deviceID, "neutral", message);
			testA.close();
			return recResp;
			}
		
		
		return stopResp;
	
	}

	private void postCalendar()
	{
		String userEmailAddress = "vramamurti@gmail.com";
		String startTime = "2017-10-06T08:00:00-08:00";
		String endTime = "2017-10-06T09:00:00-08:00";
		String timeZone = "America/Los_Angeles";
		String location = "Bathroom A";
		String title = "Cleaning Scheduled";
		MySQLConnector sqlConnection = new MySQLConnector();
		User someUser = new User(userEmailAddress,sqlConnection);
		System.out.println("After User, before Calendar_Insert_New");
		Calendar_Insert_New insertion = new Calendar_Insert_New(startTime,
				endTime, location, title, timeZone, someUser);
	}

	/**
	 * 
	 * @param command - request to the server - Command object format
	 * @param socket
	 */
	public ExecuteRequest_new(Command command, Socket socket) {
		this.command = command;
		this.socket = socket;

	}

	/**
	 * runs the entire backed and returns a Response object
	 * @return
	 */
	private ResponseCreater generateResponseAndExecuteRequest() {
		ResponseCreater response = new ResponseCreater(command).executeNlpAnalysis().executeResponseCreation();
		command.setIntent(response.getMessageIntent());
		return response;
	}

}
