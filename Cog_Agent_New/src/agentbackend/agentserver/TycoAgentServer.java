package agentbackend.agentserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * try implementing this on servers:
 * https://stackoverflow.com/questions/5419328/multiple-client-to-server-communication-program-in-java
 * 
 * @author nikhilchakravarthy
 *
 */
public class TycoAgentServer {

	private static final int inputPort = 2005;
	private static ServerSocket serverSocket = null;
	private static  boolean giveFeedbackFlag = false;
	
	
	public static void setGiveFeedbackFlag(Boolean value)
	{ giveFeedbackFlag = value;}
	
	public static Boolean getGiveFeedbackFlag()
	{ return giveFeedbackFlag;}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		System.out.println("started at: " + System.currentTimeMillis());
		runTycoAgentServer();
		System.out.println("terminated at: " + System.currentTimeMillis());

	}

	/**
	 * run the backend server and wait for client connections to the backend. If
	 * a connection is made, it spawns a child server to handle the client
	 * request
	 */
	private static void runTycoAgentServer() {

		try {
			serverSocket = new ServerSocket(inputPort);

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}

		System.out.println("starting first while");
		while (true) {
			try {
				Socket sock = serverSocket.accept();
				Thread clientThread = new MiniTycoAgentServer(sock);
				clientThread.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

}
