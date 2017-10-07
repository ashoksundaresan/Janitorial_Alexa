package test;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import agentbackend.agentserver.Command;
import email.emailhandling.GMail;
import email.emailhandling.Mail;

public class CommandTesting {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Map<String,String> recip,inv;
		recip = new HashMap<String,String>();
		inv = new HashMap<String,String>();
		recip.put("nihalmaunder@gmail.com", "Nihal Maunder");
		recip.put("nkchakra97@gmail.com", "Nikhil Chakravarthy");
		inv.putAll(recip);
		inv.put("divyajain@gmail.com", "Divya Jain");
		Mail mail = new GMail()
				.setBody("this is the body with lots of unnecessary text and shit and who knows what else.")
				.setFromAgent(false).setFrom("nsc27@case.edu").setId("sdgsdhjkglkdgjdsljk3480786903498364")
				.setInternalDate(75798409).setSubject("this is the subj").setRecipients(recip).setInvitees(inv).setThreadId("jhwer789rewkj4ghbjk234");
		Command com = new Command(mail, "nsc27@gmail.com,");
		try {
			JSONObject json = new JSONObject(jsonifyCommand(com));
			System.out.println(json.toString());
			System.out.println(json.getString("response"));
//			String invite = json.get("invitees").toString();
//			
//			Map<String,String> invs = remap(invite);
//			for(String s : invs.keySet()) {
//				System.out.println("email: " + s);
//				System.out.println("name: "+ invs.get(s)+"\n");
//			}
//			System.out.println((json.get("invitees")));
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
		
		
	}

	private static String jsonifyCommand(Command command) throws JSONException {
		JSONObject json = new JSONObject();
		json.accumulate("type", 0).accumulate("message", command.getMessage()).accumulate("from", command.getFrom()).accumulate("email", command.getUserEmail());
		json.accumulate("subject", command.getSubject()).accumulate("threadId", command.getThreadId()).accumulate("id", command.getId()).accumulate("internalDate", command.getInternalDate());
		json.accumulate("recipients", command.getRecipients()).accumulate("invitees", command.getInvitees()).accumulate("fromAgent", command.getFromAgent()).accumulate("response", command.getResponse());
		
		return json.toString();
	}
	
//	private static Map<String,String> remap(String map) {
//		map = map.substring(1, map.length()-1);
//		Map<String,String> list = new HashMap<String,String>();
//		String[] people = map.split(",");
//		for(String person : people) {
//			String[] sections = person.split(":");
//			list.put(sections[0].replaceAll("\"", ""), sections[1].replaceAll("\"", ""));
//		}
//		
//		return list;
//	}
}
