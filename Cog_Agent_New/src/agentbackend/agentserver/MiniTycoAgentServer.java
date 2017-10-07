package agentbackend.agentserver;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * mini server that takes in a socket and reads the json being written to it
 * from the client, then does that json request
 * 
 * @author nikhilchakravarthy
 *
 */
public class MiniTycoAgentServer extends Thread {
	Socket socket;
	Command command;


	/**
	 * constructor that takes in the socket from the parent server
	 * @param socket - socket passed from parent server
	 */
	public MiniTycoAgentServer(Socket socket) {
		this.socket = socket;

	}
	

	
	
/**
 * reads from the socket at attempts to make a command object. if it was successful in doing so, start an ExecuteRequest thread that does the backend on that command
 */
	@Override
	public void run() {
		try {
			InputStream input = this.socket.getInputStream();
			int b = input.read();

			// System.out.println((char)b);
			String message = "";

			while (b > 0 && (b != 0x0a)) {
				// System.out.println("inside b loop where b = " +
				// (char) b);

				// System.out.println("here");
				message += (char) b;
				if ((char) b != '-') {

					b = input.read();
				} else {
					System.out.println(message);
					break;
				}

			}
			if (message.length() >= 1) {
				System.out.println("received and starting this: " + message);
				this.command = new Command(message.substring(0, message.length() - 1));

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (this.command != null) {
			new ExecuteRequest_new(command, socket).run2();
		}
	}

}
