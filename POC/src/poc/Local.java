package poc;

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

public class Local extends TimerTask {
	//Randomisť

	//Placeholder for testing
	private static int NB_REQUEST_PER_S = 500;
	private static int NB_TOTAL_ISINS = 5;
	
	private static final String REGIONNAL_ADRESS = "";
	private static final int REGIONAL_PORT = 5555;
	
	private Socket localSocket = null;  
    private ObjectOutputStream toRegional = null;
    private ObjectInputStream fromRegional = null;
    
    private int taskNb = 0;
    private final ConcurrentMap<String, SortedSet<CoursBoursier>> bourse = new ConcurrentHashMap<>();
    private final List<CoursBoursier> initial = CoursBoursier.parseCSV("cours.csv");
    
    public Local(){
    	
    	try {
            this.localSocket = new Socket(REGIONNAL_ADRESS, REGIONAL_PORT);
            this.fromRegional = new ObjectInputStream(localSocket.getInputStream());
            this.toRegional = new ObjectOutputStream(localSocket.getOutputStream());
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: hostname");
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: hostname");
        }
    	
    }
    
    @Override
	public void run() {
    	taskNb++;
    	try {
            // Get reference from generation
            String ref = generateRef();
            
            // Send to Regional
            toRegional.writeObject(ref);
            
            // Get from return from regional and stock in dataset
            CoursBoursier cours = getCours(ref);
            
            // Print
            System.out.println(cours);

        } catch (Exception ex) {
            Logger.getLogger(Regional.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String generateRef(){
    	Random rand = new Random();
    	
    	int randomNum = rand.nextInt(Math.min(initial.size()-1,NB_TOTAL_ISINS) + 1);
    	return initial.get(randomNum).ISIN;
    }
    
    private CoursBoursier getFromRegional(String ref) {
        try {
            toRegional.writeObject(ref);
            // Update cache
            return (CoursBoursier) fromRegional.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(Regional.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public CoursBoursier getCours(String ref) {
        bourse.putIfAbsent(ref, new TreeSet<CoursBoursier>());
        CoursBoursier dernierCours;

        dernierCours = getFromRegional(ref);
//        // Sauvegrade dans le data
        bourse.get(ref).add(dernierCours);
        
        return dernierCours;
    }
    
    public void closeLocal(){
    	try {
	    	toRegional.close();
	        fromRegional.close();
	        localSocket.close();   
    	} catch (UnknownHostException e) {
    		System.err.println("Trying to connect to unknown host: " + e);
    	} catch (IOException e) {
    		System.err.println("IOException:  " + e);
    	}
    }
	
	public static void main(String[] args) {
		Local l = new Local();
		TimerTask timerTask = l;
		
        //Run timer (local) task
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(timerTask, 0, 1000/NB_REQUEST_PER_S);
        System.out.println("Local started");
        
        //Cancel after sometime test only
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        timer.cancel();
//        System.out.println("Local cancelled");
//        System.out.println(">>>>>> " + l.taskNb);
//        l.closeLocal();
    }
}
