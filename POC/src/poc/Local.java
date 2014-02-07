package poc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

public class Local extends TimerTask {

	//Placeholder for testing
	private static long NB_REQUEST_PER_S = 1;
	
	private static final String REGIONNAL_ADRESS = "";
	private static final int REGIONAL_PORT = 5555;
	
	private Socket localSocket = null;  
    private DataOutputStream toRegional = null;
    private DataInputStream fromRegional = null;
    
    private int taskNb = 0;
    
    public Local(){
    	
//    	try {
//            this.localSocket = new Socket(REGIONNAL_ADRESS, REGIONAL_PORT);
//            this.toRegional = new DataOutputStream(localSocket.getOutputStream());
//            this.fromRegional = new DataInputStream(localSocket.getInputStream());
//        } catch (UnknownHostException e) {
//            System.err.println("Don't know about host: hostname");
//        } catch (IOException e) {
//            System.err.println("Couldn't get I/O for the connection to: hostname");
//        }
    	
    }
    
    @Override
	public void run() {
    	while(true){
    		taskNb++;
    	}
    	
//    	if (localSocket != null && toRegional != null && fromRegional != null) {
//            try {
//            	//--------------------------------------------------------------
//            	//TODO Process
//            	toRegional.writeBytes("");
//
//                String responseLine;
//                
//                //Readline deprecated TODO read with bytes
//                while ((responseLine = fromRegional.readLine()) != null) {
//                    System.out.println("Server: " + responseLine);
//                    if (responseLine.indexOf("Ok") != -1) {
//                      break;
//                    }
//                }
//            } catch (UnknownHostException e) {
//                System.err.println("Trying to connect to unknown host: " + e);
//            } catch (IOException e) {
//                System.err.println("IOException:  " + e);
//            }
//    	}
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
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        timer.cancel();
        System.out.println("Local cancelled");
        System.out.println(l.taskNb);
//        l.closeLocal();
    }
}
