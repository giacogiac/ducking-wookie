package poc;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyMinimumViewport;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.util.Range;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;

public class Central {
	
	public static final int BOURSE_PORT = 12124;
	public static final int BOURSE_SECRET_PORT = 12134;
	public static final int REQUETE_PORT = 12154;
	
	private static Map<String, GraphBourse> bourse = new HashMap<String, GraphBourse>();
	
	private static Object sync = new Object();
	
	private static int requete = 0;
	
	private static Chart2D chart;

	private static Trace2DLtd trace;
	
    private Socket requeteSocket = null;  
    private ObjectOutputStream toRequete = null;
    private ObjectInputStream fromRequete= null;

	public static void main(String[] args) {
		genererCours(CoursBoursier.parseCSV("cours.csv"));
		JFrame stack = new JFrame("Bourse");
		stack.addWindowListener(new WindowAdapter() {
  	      /**
  	       * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
  	       */
  	      @Override
  	      public void windowClosing(final WindowEvent e) {
  	        System.exit(0);
  	      }
  	    });
		stack.setLayout(new GridLayout(2, 2));
		stack.add(bourse.get("FR0000133308").getChart());
		stack.add(bourse.get("US0378331005").getChart());
		stack.add(bourse.get("FR0000120271").getChart());
		stack.add(bourse.get("FR0000124141").getChart());
		//stack.add(bourse.get("FR0000131906").getChart());
		//stack.add(bourse.get("FR0000131104").getChart());
		//stack.add(bourse.get("US92826C8394").getChart());
		//stack.add(bourse.get("US9396401088").getChart());
		//stack.add(bourse.get("US38259P5089").getChart());
		stack.pack();
		stack.setExtendedState(JFrame.MAXIMIZED_BOTH);
		stack.setVisible(true);
		System.out.println("Stack Exchange OPEN !");
		ServerSocket ss;
		ServerSocket ss_secret;
		ServerSocket ss_requete;
		try {
			ss = new ServerSocket(BOURSE_PORT);
			ss_secret = new ServerSocket(BOURSE_SECRET_PORT);
			ss_requete = new ServerSocket(REQUETE_PORT);
		} catch (IOException iox) {
			System.out.println("I/O error at server socket creation");
			iox.printStackTrace();
			return;
		}
		chart = new Chart2D();
		chart.getAxisX().setPaintGrid(true);
		chart.getAxisY().setPaintGrid(true);
		chart.getAxisY().setRangePolicy(
				new RangePolicyMinimumViewport(new Range(0, +50)));
		chart.setGridColor(Color.LIGHT_GRAY);
		trace = new Trace2DLtd(100);
		trace.setName("Requetes par secondes");
		trace.setPhysicalUnits("ms", "requetes");
		trace.setColor(Color.RED);
		chart.addTrace(trace);
		Timer timer = new Timer(true);
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				synchronized (sync) {
					trace.addPoint(System.currentTimeMillis(), requete);
					requete = 0;
				}
			}

		};
		//timer.schedule(task, 0, 1000);
		JFrame requeteFrame = new JFrame("Requetes par secondes");
		requeteFrame.getContentPane().add(chart);
		requeteFrame.setSize(800, 600);
		//requeteFrame.setVisible(true);
		SecretServer secrets = new SecretServer(ss_secret);
		new Thread(secrets).start();
		RequeteServer requeteeess = new RequeteServer(ss_requete);
		new Thread(requeteeess).start();
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
	
	private static class SecretServer implements Runnable {
		
		ServerSocket ss;

		public SecretServer(ServerSocket ss) {
			super();
			this.ss = ss;
		}

		@Override
		public void run() {
			while (true) {
				Socket s = null;
				try {
					s = ss.accept();
					//System.out.println("Request from : " + s.getInetAddress());
					SecretRegionalHandler handler = new SecretRegionalHandler(s);
					new Thread(handler).start();
				} catch (IOException iox) {
					iox.printStackTrace();
				}
			}
		}
		
		private static class SecretRegionalHandler implements Runnable {
			private Socket regional;
			
			private ObjectOutputStream toRegional;
			private ObjectInputStream fromRegional;

			private SecretRegionalHandler(Socket socket) throws IOException {
				regional = socket;
				toRegional = new ObjectOutputStream(socket.getOutputStream());
				toRegional.flush();
				fromRegional = new ObjectInputStream(socket.getInputStream());
			}

			@Override
			public void run() {
				try {
					String ref;
	            	while((ref = (String) fromRegional.readObject()) != null)
	            	{
	                // Get reference from client
	                
	                // Get from data
	                CoursBoursier cours = bourse.get(ref).getCours();
	                
	                //System.out.println(cours);
	                
	                // Send to regional
	                toRegional.writeUnshared(cours);
	                toRegional.flush();
	            	}
	            } catch (IOException | ClassNotFoundException ex) {
	            	//System.out.println("Connexion closed from : " + regional.getInetAddress());
	            }
			}
			
		}
		
	}
	

	private static class RequeteServer implements Runnable {
		
		ServerSocket ss;

		public RequeteServer(ServerSocket ss) {
			super();
			this.ss = ss;
		}

		@Override
		public void run() {
			while (true) {
				Socket s = null;
				try {
					s = ss.accept();
					//System.out.println("Request from : " + s.getInetAddress());
					RequeteHandler handler = new RequeteHandler(s);
					new Thread(handler).start();
				} catch (IOException iox) {
					iox.printStackTrace();
				}
			}
		}
		
		private static class RequeteHandler implements Runnable {
			private Socket sockkkkkk;
			
			private ObjectOutputStream torequete;
			private ObjectInputStream fromrequete;

			private RequeteHandler(Socket socket) throws IOException {
				sockkkkkk = socket;
				torequete = new ObjectOutputStream(socket.getOutputStream());
				torequete.flush();
				fromrequete = new ObjectInputStream(socket.getInputStream());
			}

			@Override
			public void run() {
				try {
					String ref;
	            	while((ref = (String) fromrequete.readObject()) != null)
	            	{
	                // Get reference from client
	                
	                //System.out.println(cours);
	                
	                synchronized (sync) {
	                	torequete.writeInt(requete);
						requete = 0;
					}
	                torequete.reset();
	            	}
	            } catch (IOException | ClassNotFoundException ex) {
	            	//System.out.println("Connexion closed from : " + regional.getInetAddress());
	            }
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
			toRegional.flush();
			fromRegional = new ObjectInputStream(socket.getInputStream());
		}

		@Override
		public void run() {
			try {
				String ref;
            	while((ref = (String) fromRegional.readObject()) != null)
            	{
                // Get reference from client
                
                // Get from data
                CoursBoursier cours = bourse.get(ref).getCours();
                
                //System.out.println(cours);
                
                // Send to regional
                toRegional.writeUnshared(cours);
                toRegional.flush();
                synchronized (sync) {
                	 requete++;
				}
            	}
            } catch (IOException | ClassNotFoundException ex) {
            	System.out.println("Connexion closed from : " + regional.getInetAddress());
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
