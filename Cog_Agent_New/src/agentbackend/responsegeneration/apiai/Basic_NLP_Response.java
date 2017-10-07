package agentbackend.responsegeneration.apiai;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * nlp response object with sentence to analyze. once it is analyzed, the intent
 * and parameter map should be set, as well as failure and answered. Can be
 * turned into a json representation with all the fields represented to be sent
 * as bytes from client to server and back
 * 
 * @author nikhilchakravarthy
 *
 */
public class Basic_NLP_Response {

	private Map<String, String> parameters;
	private String intent;
	private String sentence;
	private boolean failure;
	private boolean answered;

	/**
	 * Constructor to make the object with only the sentence to be parsed
	 * 
	 * @param sentence
	 */
	public Basic_NLP_Response(String sentence) {
		this.sentence = sentence;
		failure = false;
		answered = false;

	}

	/**
	 * answered getter
	 * 
	 * @return
	 */
	public boolean getAnswered() {
		return answered;
	}

	/**
	 * answered setter, returns this object
	 * 
	 * @param set
	 * @return
	 */
	public Basic_NLP_Response setAnswered(boolean set) {
		this.answered = set;
		return this;
	}

	/**
	 * constructor from json
	 * 
	 * @param jsonString
	 * @param answered
	 *            - unused, just here to separate form other constructor
	 */
	public Basic_NLP_Response(String jsonString, boolean answered) {
		JSONObject json;

		try {
			json = new JSONObject(jsonString);
			this.answered = json.getBoolean("answered");
			sentence = json.getString("sentence");
			if (this.answered) {
				intent = json.getString("intent");
				failure = json.getBoolean("failure");
				parameters = remap(json.get("parameters").toString());
			}
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	/**
	 * set the parameter map of name to value
	 * 
	 * @param parameters
	 *            - map to set
	 * @return this object
	 */
	public Basic_NLP_Response setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
		return this;

	}

	/**
	 * turn this object into a json string
	 * 
	 * @return string in json form
	 * @throws JSONException
	 */
	public String jsonify() throws JSONException {
		JSONObject json = new JSONObject();
		json.accumulate("sentence", sentence).accumulate("intent", intent).accumulate("failure", failure)
				.accumulate("parameters", parameters).accumulate("answered", answered);
		return json.toString();
	}

	/**
	 * set intent
	 * 
	 * @param intent
	 * @return this object
	 */
	public Basic_NLP_Response setIntent(String intent) {
		this.intent = intent;
		return this;
	}

	/**
	 * gets intent
	 * 
	 * @return String intent
	 */
	public String getIntent() {
		return intent;
	}

	/**
	 * gets parameters
	 * 
	 * @return Map of String to String (name to value)
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}

	/**
	 * gets sentence to parse
	 * 
	 * @return String sentence
	 */
	public String getSentence() {
		return sentence;
	}

	/**
	 * set this object's failed flag to true
	 */
	public void failed() {
		failure = true;
	}

	/**
	 * see if the object has failed
	 * 
	 * @return failed boolean
	 */
	public boolean isFailed() {
		return failure;
	}

	/**
	 * takes the string representation of the parameter map and turns it into an
	 * actual Map object
	 * 
	 * @param map
	 *            - String representation of the map of parameters
	 * @return Map object of parameters
	 */
	private Map<String, String> remap(String map) {

		map = map.substring(1, map.length() - 1);
		Map<String, String> list = new HashMap<String, String>();
		String[] parameters = map.split(",");
		if (map.contains(":")) {
			for (String value : parameters) {

				String[] sections = value.split(":", 2);
				list.put(sections[0].replaceAll("\\\\", "").replaceAll("\"", ""),
						sections[1].replaceAll("\\\\", "").replaceAll("\"", ""));
			}
		}
		return list;
	}

}
