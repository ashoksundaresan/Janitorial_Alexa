package agentbackend.responsegeneration.apiai;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

/**
 * @author nikhilchakravarthy Text client reads requests line by line from
 *         standard input. This class acts as a client to the API_AI_Requester
 *         server
 */

public class NLP_Connector {

	private Map<String, String> parameterMap;
	private String intent;
	private static final String badResp = "I'm sorry, I couldn't figure out what you wanted me to do from that email. Could you be more precise?";

	private boolean tooLong = false;

	private static final int port = 3009;
	private static Socket socket;

	public Map<String, String> getParamMap() {
		return parameterMap;
	}

	public String getIntent() {
		return intent;
	}

	public String getBadResponse() {
		return badResp;
	}

	public boolean isTooLong() {
		return tooLong;
	}

	/**
	 * Connects to the API_AI_Requester server and runs the API_AI call
	 * 
	 * @param message
	 *            - String to be parsed by API.AI
	 */
	public NLP_Connector(List<String> message) {

		try {
			socket = new Socket("localhost", port);
			getResponse(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			intent = "failure";
			e.printStackTrace();
		}

		// System.out.println("intent: " + intent);
	}

	/**
	 * Inputs a message to API.AI, which puts the message into a NLP black box
	 * turn it into useful info. Also takes care of the carroting issue
	 * 
	 * @param message
	 *            - String message to be parsed
	 * @return Map of entity to its value
	 */
	private void getResponse(List<String> message) {

		// If the message is a direct reply to a tycoagent email, substring the
		// message to ignore the sxction that is a copy of Tyco Agent's email

		// System.out.println("message to api.ai:\n" + message);

		parameterMap = sentenceBreakdown(message);

	}

	/**
	 * this method creates a new Basic_NLP_Response object without intent or
	 * parameters for each sentence to be analyzed, turns it into a json string,
	 * and writes that as bytes to the API_AI_Requester server to be sent to
	 * API.AI. Then, once it gets a json response from the server (json form of
	 * a complete Basic_NLP_Response object), it turns that json back into an
	 * object and sets the intent of this NLP_Connector instance and returns the
	 * parameter map
	 * 
	 * @param sentences - list of sentences to be parsed
	 * @return Map<String,String> parameters
	 */
	private Map<String, String> sentenceBreakdown(List<String> sentences) {
		// TODO Auto-generated method stub

		Map<String, Map<String, String>> allParameters = new HashMap<String, Map<String, String>>();

		System.out.println("sentence list:\n" + sentences);
		try {

			OutputStream os = (socket.getOutputStream());
			InputStream is = (socket.getInputStream());

			int i = 0;
			for (String sentence : sentences) {

				Basic_NLP_Response response = new Basic_NLP_Response(sentence);
				os.write((response.jsonify().toString() + "-").getBytes());
				int b = is.read();
				String message = "";
				while (b > 0 && (b != 0x0a)) {

					message += (char) b;
					if ((char) b != '-') {

						b = is.read();
					} else {
						System.out.println(message);
						break;
					}

				}
				if (message.length() >= 1) {
					System.out.println("received: " + message);
					response = new Basic_NLP_Response(message.substring(0, message.length() - 1), true);
					if (!response.isFailed()) {
						allParameters.put(response.getIntent() + i, response.getParameters());
						i++;
					}

				}

			}
			socket.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("ALL PARAMETERS (" + allParameters.size() + "): " + allParameters);

		return compressAllParameters(allParameters);

	}

	/**
	 * compresses the map of parameter maps into a single parameter map and sets
	 * the correct intent NOTE: note well tested
	 * 
	 * @param allParameters
	 * @return Map<String,JsonElement>, param name to value
	 */
	private Map<String, String> compressAllParameters(Map<String, Map<String, String>> allParameters) {
		// TODO Auto-generated method stub

		if (allParameters.size() <= 1) {
			for (String intent_i : allParameters.keySet()) {
				if (!intent_i.contains("fallback")) {
					intent = intent_i.substring(0, intent_i.length() - 1);
					return allParameters.get(intent_i);
				} else {
					intent = "fallback";
				}
			}
			return new HashMap<String, String>();
		} else {
			Map<String, String> compressedParameters = new HashMap<String, String>();
			boolean update = false;
			for (String intent_i : allParameters.keySet()) {
				if (intent_i.contains("update")) {
					update = true;
				}
				if (allParameters.get(intent_i).size() > 0) {
					compressedParameters.putAll(allParameters.get(intent_i));
				}
			}
			if (update) {
				intent = "update_calendar";
			} else {
				intent = "insert_calendar";
			}
			System.out.println("intent: " + intent);
			System.out.println("compressed parameters: " + compressedParameters);
			return compressedParameters;
		}

	}

}