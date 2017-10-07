package test.serverclienttest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import agentbackend.responsegeneration.apiai.Basic_NLP_Response;

public class API_AI_Client {

	private static final int port = 3009;
	private static Socket socket;

	public static void main(String[] args) throws IOException {

		System.out.println("start client");
		try {
			socket = new Socket("localhost", port);

			OutputStream os = (socket.getOutputStream());
			ObjectOutputStream o = new ObjectOutputStream(os);
			InputStreamReader type = new InputStreamReader(System.in);
			BufferedReader read = new BufferedReader(type);
			InputStream is = (socket.getInputStream());
			ObjectInputStream i = new ObjectInputStream(is);
			System.out.println("loop start");
			while (true) {
				Basic_NLP_Response resp = new Basic_NLP_Response(read.readLine());
				o.writeObject(resp);
				resp = (Basic_NLP_Response) i.readObject();
				System.out.println("intent: " + resp.getIntent());
				System.out.println("params: " + resp.getParameters());
			}

			
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			socket.close();
			
			e.printStackTrace();
		}

	}

}
