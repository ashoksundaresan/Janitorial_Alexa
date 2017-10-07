package agentbackend.agentserver;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import agentbackend.database.MySQLConnector_new;
import agentbackend.nlp.ManualProcessor;
import agentbackend.responsegeneration.ResponseCreater;
import email.emailsending.EmailCreater;

/**
 * Class that takes in a Command object (the request to the server), and carries
 * it out
 * 
 * @author nikhilchakravarthy
 *
 */
public class ExecuteRequest {

	/**
	 * Command and Socket objects passed into the Constructor
	 */
	private Command command;
	private Socket socket;
	private String responseString;
	private static final String option1 = "provide";
	private static final String option2 = "retrieve";
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
		String resp1,resp2,retDB;
		int deviceID = 45;
		final String updateLabel = "feedback";
		resp1="Ok. Please go ahead.";
		resp2="Ok. Here is what we have. ";
		String notFollowResp ="I did not follow. Say provide or retrieve";
		String stopResp="Good Bye";
		String recResp="Your feedback has been recorded.";
		String clearResp="Feedback entries have been cleared.";
	
		MySQLConnector_new testA = new MySQLConnector_new();
		String message = command.getMessage();
		
		String manualCheckFeedback = ManualProcessor.patternMatchFeedback(message);
		String manualSentiment = ManualProcessor.patternMatchFeedback(message);
				
		if (command.getMessage().toLowerCase().contains(option1)) {
			TycoAgentServer.setGiveFeedbackFlag(true);
			return resp1;
			}
		
		else if (command.getMessage().toLowerCase().contains(resetOption)){
			testA.deleteEntries(deviceID);
			testA.close();
			return clearResp;
		}
		else if (command.getMessage().toLowerCase().contains(option2)){
			retDB = testA.getMultipleEntries(deviceID);
			if (retDB == "") retDB = "There is currently no feedback available.";
			return resp2 + retDB;
		}
		else if (command.getMessage().toLowerCase().contains(stopOption)){
			TycoAgentServer.setGiveFeedbackFlag(false);
			return stopResp;}
		else
			if (TycoAgentServer.getGiveFeedbackFlag()){
				System.out.println("deviceID " + deviceID);
				System.out.println("updatelabel " + updateLabel);
				System.out.println("message:" + command.getMessage());
				testA.insertMultipleEntries(deviceID,"neutral", command.getMessage());
				testA.close();
				return recResp;
			}
			else
				return notFollowResp;
	}


	/**
	 * 
	 * @param command - request to the server - Command object format
	 * @param socket
	 */
	public ExecuteRequest(Command command, Socket socket) {
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
