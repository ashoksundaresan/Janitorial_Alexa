package agentbackend.responsegeneration.apiai;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class acts as a server, sending client socket connections to separate
 * threads to be executed and written back
 * 
 * @author nikhilchakravarthy
 *
 */
public class API_AI_Requester {

	/**
	 * port number and serversocket
	 */
	private static final int port = 3009;
	private static ServerSocket serverSock = null;

	/**
	 * runs the server, called in main method
	 */
	public static void runRequesterServer() {
		try {
			System.out.println("started server");
			serverSock = new ServerSocket(port);
			while (true) {
				try {
					Socket socket = serverSock.accept();
					Thread clientThread = new APIRequesterThread(socket);
					clientThread.start();
				} catch (IOException e) {
					e.printStackTrace(System.out);
				}

			}

		} catch (IOException e) {
			e.printStackTrace(System.out);

		}

	}

	public static void main(String[] args) {

		System.out.println("started at: " + System.currentTimeMillis());
		runRequesterServer();
		System.out.println("terminated at " + System.currentTimeMillis());

	}

}
