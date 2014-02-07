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
import java.util.Random;
import java.util.Vector;

public class Central {
	
	private static final int BOURSE_PORT = 5555;
	
	private static Map<String, List<CoursBoursier>> bourse = new HashMap<String, List<CoursBoursier>>();

	public static void main(String[] args) {
		genererCours(CoursBoursier.parseCSV("cours.csv"));
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
	
	private static void genererCours(List<CoursBoursier> init) {
		for (CoursBoursier coursBoursier : init) {
			List<CoursBoursier> historique = new Vector<CoursBoursier>();
			bourse.put(coursBoursier.ISIN, historique);
			BourseSimulator simulator = new BourseSimulator(historique, coursBoursier.ISIN, coursBoursier.company, coursBoursier.cotation);
			new Thread(simulator).start();
		}
	}
	
	private static class BourseSimulator implements Runnable {
		private Random rand;
		private double tendance;
		
		private List<CoursBoursier> historique;
		private double currentValue;	
		
		private String ISIN;
		private String company;

		private BourseSimulator(List<CoursBoursier> historique, String iSIN, String company, double init) {
			this.historique = historique;
			this.ISIN = iSIN;
			this.company = company;
			currentValue = init;
			rand = new Random();
			tendance = rand.nextDouble();
		}

		@Override
		public void run() {
			historique.add(new CoursBoursier(Calendar.getInstance().getTimeInMillis(), ISIN, company, currentValue));
			while (true) {
				currentValue += rand.nextDouble() - tendance;
				tendance += (rand.nextDouble() - 0.5) * 0.1;
				historique.add(new CoursBoursier(Calendar.getInstance().getTimeInMillis(), ISIN, company, currentValue));
			}
		}
		
	}

}
