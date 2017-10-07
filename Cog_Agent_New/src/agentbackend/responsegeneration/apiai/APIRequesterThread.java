package agentbackend.responsegeneration.apiai;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;

import external.apiai.api.AIConfiguration;
import external.apiai.api.AIDataService;
import external.apiai.api.AIServiceException;
import external.apiai.model.AIRequest;
import external.apiai.model.AIResponse;

/**
 * thread for executing request to API.AI server
 * 
 * @author nikhilchakravarthy
 *
 */
public class APIRequesterThread extends Thread {
	/**
	 * socket, API.AI objects, and API.AI configuration key
	 */
	private static final String configurationKey = "beff024a03a043728018f8668222ea46";
	private AIConfiguration configuration;
	private Socket socket;
	private AIDataService dataService;
	private AIRequest request;
	private AIResponse response;

	/**
	 * this is what gets run to start reading from the socket and do the
	 * request, then finally send the info back through the socket
	 */
	@Override
	public void run() {
		try {
			InputStream is = (socket.getInputStream());

			OutputStream os = (socket.getOutputStream());

			configuration = new AIConfiguration(configurationKey);
			dataService = new AIDataService(configuration);

			Basic_NLP_Response nlpResponse = null;

			int b = is.read();

			// System.out.println((char)b);
			String message = "";

			while (b > 0 && (b != 0x0a)) {
				// System.out.println("inside b loop where b = " +
				// (char) b);

				// System.out.println("here");
				message += (char) b;
				if ((char) b != '-') {

					b = is.read();
				} else {
					System.out.println(message);
					break;
				}

			}
			if (message.length() >= 1) {
				System.out.println("received and starting this: " + message);
				nlpResponse = new Basic_NLP_Response(message.substring(0, message.length() - 1), false);

			}

			System.out.println("got object with sentence: " + nlpResponse.getSentence());
			nlpResponse = requestAPI_AI(nlpResponse).setAnswered(true);
			// nlpResponse.setIntent(nlpResponse.getSentence()+"ppfpfp");
			System.out.println("writing response:\n" + nlpResponse.jsonify());
			os.write((nlpResponse.jsonify() + "-").getBytes());
			System.out.println("wrote json with intent: " + nlpResponse.getIntent());

			socket.close();
		} catch (IOException e) {
			e.printStackTrace();

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * constructor that sets socket
	 * @param socket
	 */
	public APIRequesterThread(Socket socket) {
		this.socket = socket;
	}

	/**
	 * makes the actual query to API.AI
	 * @param input Basic_NLP_Response object
	 * @return the same object, but set with the intent and parameters from the sentence
	 */
	private Basic_NLP_Response requestAPI_AI(Basic_NLP_Response input) {

		request = new AIRequest(input.getSentence());
		System.out.println("made request");
		try {
			response = dataService.request(request);
			System.out.println("got response");

			if (response.getStatus().getCode() != 200) {
				System.out.println("error code: " + response.getStatus().getCode());
				System.out.println("details:\n" + response.getStatus().getErrorDetails());
				input.failed();

			} else if (response != null) {
				System.out.println("good response");
				if (response.getResult().getMetadata().getIntentName().contains("fallback")) {
					input.setIntent("fallback");

				} else {
					input.setIntent(response.getResult().getMetadata().getIntentName());
					Map<String, String> params = new HashMap<String, String>();
					for (String paramName : response.getResult().getParameters().keySet()) {
						params.put(paramName, response.getResult().getParameters().get(paramName).toString());
					}
					input.setParameters(params);
				}
			}

		} catch (AIServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.out);
			input.failed();
		}
		System.out.println("returning out of apiai request");
		return input;

	}

}
