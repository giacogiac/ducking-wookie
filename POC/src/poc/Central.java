package poc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class Central {
	
	private static final int BOURSE_PORT = 5555;
	
	private static Map<String, List<CoursBoursier>> bourse = new HashMap<String, List<CoursBoursier>>();

	public static void main(String[] args) {
		ServerSocket ss;
		try {
			ss = new ServerSocket(BOURSE_PORT);
		} catch (IOException iox) {
			System.out.println("I/O error at server socket creation");
			iox.printStackTrace();
			return;
		}
		while (true) {
			Socket s = null;
			try {
				s = ss.accept();
				System.out.println("connection from" + s.getInetAddress());
				RegionalHandler handler = new RegionalHandler(s);
				new Thread(handler).start();
			} catch (IOException iox) {
				iox.printStackTrace();
			}
		}
	}
	
	private static class RegionalHandler implements Runnable {
		private ObjectOutputStream toClient;
		private ObjectInputStream fromClient;

		private RegionalHandler(Socket socket) throws IOException {
			fromClient = new ObjectInputStream(socket.getInputStream());
			toClient = new ObjectOutputStream(socket.getOutputStream());
		}

		@Override
		public void run() {
			
		}
		
	}
	
	private static void genererCours() {
		
	}
	

}
