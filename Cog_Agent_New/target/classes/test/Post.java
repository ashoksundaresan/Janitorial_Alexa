package test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Post {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		sendPOST("hellow there");
	}

	private static void sendPOST(String query) {
		try {
			HttpURLConnection con = (HttpURLConnection) new URL("http://localhost/apache/alexa/alexa_php.php")
					.openConnection();

			con.setRequestMethod("POST");

			// For POST only - START
			con.setDoOutput(true);
			OutputStream os = con.getOutputStream();

			query = "response=" + query;
			System.out.println("query: " + query);
			os.write(query.getBytes());
			os.flush();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// For POST only - END

	}
}
