package poc;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyMinimumViewport;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.util.Range;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.BoxLayout;

public class Local extends TimerTask {
	
	static TimerTask timerTask;
	
	private static Chart2D chart;

	private static Trace2DLtd trace;
	
    //Run timer (local) task
    static Timer timer;
    
    private static double erreur = 0;
    private static int nberreur = 0;
    
    private static Object sync = new Object();
    
	//Randomis�

	//Placeholder for testing
	private static int FREQ_REQUEST_MILLI = 100; //Range 1 or more
	private static int NB_TOTAL_ISINS; //Range 1 or more
	
	private static final String REGIONNAL_ADRESS = "localhost";
	private static final int REGIONAL_PORT = Regional.BOURSE_PORT;
	
	private static final String CENTRAL_ADRESS = "localhost";
	private static final int CENTRAL_PORT = Central.BOURSE_SECRET_PORT;
	
	private Socket localSocket = null;  
    private ObjectOutputStream toRegional = null;
    private ObjectInputStream fromRegional = null;
    
    
    private Socket centralSocket = null;  
    private ObjectOutputStream toCentral = null;
    private ObjectInputStream fromCentral = null;
    
    private int taskNb = 0;
    private final ConcurrentMap<String, SortedSet<CoursBoursier>> bourse = new ConcurrentHashMap<>();
    private final List<CoursBoursier> initial = CoursBoursier.parseCSV("cours.csv");
    
    private JSlider freqSlider;
    private JSlider nbcoursSlider;
    
	private void createFreqSlider() {
		// Latency slider:
		this.freqSlider = new JSlider(1, 1000);
		this.freqSlider.setBackground(Color.WHITE);
		this.freqSlider.setValue(FREQ_REQUEST_MILLI);
		this.freqSlider.setMajorTickSpacing(100);
		this.freqSlider.setMinorTickSpacing(20);
		this.freqSlider.setSnapToTicks(true);
		this.freqSlider.setPaintLabels(true);
		this.freqSlider.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Frequence requetes",
				TitledBorder.LEFT, TitledBorder.BELOW_TOP));
		this.freqSlider.setPaintTicks(true);

		this.freqSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				// Only if not currently dragged...
				if (!source.getValueIsAdjusting()) {
					int value = source.getValue();
					FREQ_REQUEST_MILLI = value;
				}
			}
		});
	}
	
	private void createNBcoursSlider() {
		// Latency slider:
		this.nbcoursSlider = new JSlider(1, NB_TOTAL_ISINS);
		this.nbcoursSlider.setBackground(Color.WHITE);
		this.nbcoursSlider.setValue(FREQ_REQUEST_MILLI);
		this.nbcoursSlider.setMajorTickSpacing(1);
		this.nbcoursSlider.setMinorTickSpacing(1);
		this.nbcoursSlider.setSnapToTicks(true);
		this.nbcoursSlider.setPaintLabels(true);
		this.nbcoursSlider.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Nombres de cours",
				TitledBorder.LEFT, TitledBorder.BELOW_TOP));
		this.nbcoursSlider.setPaintTicks(true);

		this.nbcoursSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				// Only if not currently dragged...
				if (!source.getValueIsAdjusting()) {
					int value = source.getValue();
					NB_TOTAL_ISINS = value;
				}
			}
		});
	}
    
    public Local(){
    	System.out.println("Init Local");
    	chart = new Chart2D();
		chart.getAxisX().setPaintGrid(true);
		chart.getAxisY().setPaintGrid(true);
		chart.getAxisY().setRangePolicy(
				new RangePolicyMinimumViewport(new Range(0, 10)));
		chart.setGridColor(Color.LIGHT_GRAY);
		trace = new Trace2DLtd(100);
		trace.setName("Requetes par secondes");
		trace.setPhysicalUnits("ms", "Erreur %");
		trace.setColor(Color.RED);
		chart.addTrace(trace);
		JFrame erreur = new JFrame("ERREUR");
		erreur.add(chart);
		erreur.setSize(800, 600);
		erreur.addWindowListener(new WindowAdapter() {
	  	      /**
	  	       * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
	  	       */
	  	      @Override
	  	      public void windowClosing(final WindowEvent e) {
	  	        System.exit(0);
	  	      }
	  	    });
		erreur.setVisible(true);
    	NB_TOTAL_ISINS = initial.size();
    	createNBcoursSlider();
    	createFreqSlider();
    	JFrame toolbar = new JFrame("TOOLBAR");
    	toolbar.getContentPane().setLayout(new BoxLayout(toolbar.getContentPane(), BoxLayout.Y_AXIS));
    	toolbar.getContentPane().add(this.nbcoursSlider);
    	toolbar.getContentPane().add(this.freqSlider);
    	toolbar.setSize(1300, 250);
    	toolbar.addWindowListener(new WindowAdapter() {
  	      /**
  	       * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
  	       */
  	      @Override
  	      public void windowClosing(final WindowEvent e) {
  	        System.exit(0);
  	      }
  	    });
    	toolbar.setVisible(true);
    	try {
            this.localSocket = new Socket(REGIONNAL_ADRESS, REGIONAL_PORT);
            this.toRegional = new ObjectOutputStream(localSocket.getOutputStream());
            this.fromRegional = new ObjectInputStream(localSocket.getInputStream());
            this.centralSocket = new Socket(CENTRAL_ADRESS, CENTRAL_PORT);
            this.toCentral = new ObjectOutputStream(centralSocket.getOutputStream());
            this.fromCentral = new ObjectInputStream(centralSocket.getInputStream());
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: hostname");
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: hostname");
        }
    }
    
    @Override
	public void run() {
    	for(int i = 0; i < FREQ_REQUEST_MILLI; i++) {
    	try {
            // Get reference from generation
            String ref = generateRef();
                       
            // Get from return from regional and stock in dataset
            CoursBoursier cours = getCours(ref);
            
            // Print
            // System.out.println(cours);

        } catch (Exception ex) {
            Logger.getLogger(Regional.class.getName()).log(Level.SEVERE, null, ex);
        }
    	}
    }
    
    private String generateRef(){
    	Random rand = new Random();
    	
    	int randomNum = rand.nextInt(NB_TOTAL_ISINS);
    	return initial.get(randomNum).ISIN;
    }
    
    private CoursBoursier getFromRegional(String ref) {
        try {
            toRegional.writeObject(ref);
            toRegional.reset();
            // Update cache
            return (CoursBoursier) fromRegional.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(Regional.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private CoursBoursier getFromCentral(String ref) {
        try {
            toCentral.writeObject(ref);
            toCentral.reset();
            // Update cache
            return (CoursBoursier) fromCentral.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(Regional.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public CoursBoursier getCours(String ref) {
        CoursBoursier dernierCours = getFromRegional(ref);
        
        CoursBoursier dernierCoursCentral = getFromCentral(ref);
        synchronized (sync) {
			erreur +=Math.abs(dernierCours.cotation - dernierCoursCentral.cotation)/dernierCoursCentral.cotation*100.0;
			nberreur++;
		}
        return dernierCours;
    }
    
    public void closeLocal(){
    	try {
	    	toRegional.close();
	        fromRegional.close();
	        localSocket.close();  
	        toCentral.close();
	        fromCentral.close();
	        centralSocket.close();  
    	} catch (UnknownHostException e) {
    		System.err.println("Trying to connect to unknown host: " + e);
    	} catch (IOException e) {
    		System.err.println("IOException:  " + e);
    	}
    }
	
	public static void main(String[] args) {
		Local l = new Local();
		timerTask = l;
		
        //Run timer (local) task
        timer = new Timer(true);
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
        System.out.println("Local started");
        Timer graph = new Timer (true);
        TimerTask graphTask = new TimerTask() {
			@Override
			public void run() {
				if(nberreur<1) return;
				synchronized (sync) {
					System.out.println(erreur/nberreur);
					trace.addPoint(System.currentTimeMillis(), erreur/nberreur);
					nberreur = 0;
					erreur = 0;
				}
				
			}

		};
		timer.schedule(graphTask, 0, 50);
        while (true) {}
    }
}
