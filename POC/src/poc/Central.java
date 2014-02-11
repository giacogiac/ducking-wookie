package poc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Central {
	
	public static final int BOURSE_PORT = 12124;
	
	private static Map<String, GraphBourse> bourse = new HashMap<String, GraphBourse>();

	public static void main(String[] args) {
		genererCours(CoursBoursier.parseCSV("cours.csv"));
		System.out.println("Stack Exchange OPEN !");
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
				System.out.println("Request from : " + s.getInetAddress());
				RegionalHandler handler = new RegionalHandler(s);
				new Thread(handler).start();
			} catch (IOException iox) {
				iox.printStackTrace();
			}
		}
	}
	
	private static class RegionalHandler implements Runnable {
		private Socket regional;
		
		private ObjectOutputStream toRegional;
		private ObjectInputStream fromRegional;

		private RegionalHandler(Socket socket) throws IOException {
			regional = socket;
			toRegional = new ObjectOutputStream(socket.getOutputStream());
			fromRegional = new ObjectInputStream(socket.getInputStream());
		}

		@Override
		public void run() {
			try {
            	while(true)
            	{
                // Get reference from client
                String ref = (String) fromRegional.readObject();
                
                // Get from cache
                CoursBoursier cours = bourse.get(ref).getCours();
                
                System.out.println(cours);
                
                // Send to client
                toRegional.writeObject(cours);
            	}
            } catch (IOException | ClassNotFoundException ex) {
            	System.out.println("Request from : " + regional.getInetAddress());
            }
		}
		
	}
	
	private static void genererCours(List<CoursBoursier> init) {
		for (CoursBoursier coursBoursier : init) {
			GraphBourse graph = new GraphBourse(coursBoursier);
			bourse.put(coursBoursier.ISIN, graph);
			new Thread(graph).start();
		}
	}
}
