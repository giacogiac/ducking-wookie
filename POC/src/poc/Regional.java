package poc;

import info.monitorenter.gui.chart.demos.Showcase;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Regional {

    public static final int BOURSE_PORT = 12123;
    private static int DELAIS_EXPIRATION = 5000; // 5 sec
    private final ConcurrentMap<String, SortedSet<CoursBoursier>> bourse = new ConcurrentHashMap<>();

    private Socket socketCentral = null;
    private ObjectInputStream fromCentral = null;
    private ObjectOutputStream toCentral = null;
    
    private JSlider delaiSlider;
    
    private void createDelaiSlider() {
        // Latency slider:
        this.delaiSlider = new JSlider(0, 10000);
        this.delaiSlider.setBackground(Color.WHITE);
        this.delaiSlider.setValue(DELAIS_EXPIRATION);
        this.delaiSlider.setMajorTickSpacing(500);
        this.delaiSlider.setMinorTickSpacing(100);
        this.delaiSlider.setSnapToTicks(true);
        this.delaiSlider.setPaintLabels(true);
        this.delaiSlider.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Cache delai", TitledBorder.LEFT,
            TitledBorder.BELOW_TOP));
        this.delaiSlider.setPaintTicks(true);

        this.delaiSlider.addChangeListener(new ChangeListener() {
          public void stateChanged(final ChangeEvent e) {
            JSlider source = (JSlider) e.getSource();
            // Only if not currently dragged...
            if (!source.getValueIsAdjusting()) {
              int value = source.getValue();
              DELAIS_EXPIRATION = value;
            }
          }
        });
      }

    /**
     * Récupération du dernier cours en fonction de la référence de l'entreprise
     * Si le cours n'est pas connu
     * @param ref Identificateur de l'entreprise
     * @return Dernier cours boursier mis en cache
     */
    public CoursBoursier getCours(String ref) {
        // Si la réfénrece n'existe pas on la créé
        bourse.putIfAbsent(ref, new TreeSet<CoursBoursier>());
        CoursBoursier dernierCours;
        
        long currentTime = Calendar.getInstance().getTimeInMillis();
        if (!bourse.get(ref).isEmpty()) {
            dernierCours = bourse.get(ref).last();
            // Si la dernière référence cachée n'a pas expiré
            if(currentTime - dernierCours.time < DELAIS_EXPIRATION)
                return dernierCours;
        }
        
        // Récupération de la dernière valeur du cours depuis le site central
        dernierCours = getFromCentral(ref);
        //System.out.println(dernierCours);
        // Sauvegrade dans le cache
        bourse.get(ref).add(dernierCours);
        
        return dernierCours;
    }

    /**
     * Contact site central pour récupérer la dernière valeur d'un cours boursier
     * @param ref Identifiant ISIN du cours à mettre à jour
     * @return La dernière valuer du cours
     */
    private CoursBoursier getFromCentral(String ref) {
        try {
            toCentral.writeObject(ref);
            toCentral.reset();
            // Update cache
            CoursBoursier cours = (CoursBoursier) fromCentral.readObject();
            return cours;
        } catch (IOException | ClassNotFoundException ex) {
            //Logger.getLogger(Regional.class.getName()).log(Level.SEVERE, null, ex);
        	System.out.println("Deconnexion");
        }
        return null;
    }


    public Regional() {
    	createDelaiSlider();
    	JFrame slider = new JFrame("R�gional");
    	slider.getContentPane().add(delaiSlider);
    	slider.pack();
    	slider.setSize(1300, 150);
    	slider.addWindowListener(new WindowAdapter() {
    	      /**
    	       * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
    	       */
    	      @Override
    	      public void windowClosing(final WindowEvent e) {
    	        System.exit(0);
    	      }
    	    });
    	slider.setVisible(true);
        try {
            this.socketCentral = new Socket("", Central.BOURSE_PORT);
            this.toCentral = new ObjectOutputStream(socketCentral.getOutputStream());
            this.fromCentral = new ObjectInputStream(socketCentral.getInputStream());
        } catch (IOException ex) {
            //Logger.getLogger(Regional.class.getName()).log(Level.SEVERE, null, ex);
        	System.out.println("Deconnexion");
        }
    }

    /**
     * Gère les connexions clients
     */
    public void runServer() {
        ServerSocket ss;
        try {
            ss = new ServerSocket(BOURSE_PORT);
        } catch (IOException ex) {
            Logger.getLogger(Regional.class.getName()).log(Level.SEVERE, "Server socket intiation failed", ex);
            return;
        }
        while (true) {
            try {
                Socket s = ss.accept();
                System.out.println("connection from" + s.getInetAddress());
                LocalHandler handler = new LocalHandler(s, this);
                new Thread(handler).run();
            } catch (IOException ex) {
                Logger.getLogger(Regional.class.getName()).log(Level.SEVERE, "Socket initialisation failed", ex);
            }
        }
    }

    /**
     * Interraction avec les requêtes clients
     */
    private static class LocalHandler implements Runnable {

        private final ObjectInputStream fromClient;
        private final ObjectOutputStream toClient;
        private final Regional regional;

        private LocalHandler(Socket socket, Regional regional) throws IOException {
        	this.toClient = new ObjectOutputStream(socket.getOutputStream());
            this.fromClient = new ObjectInputStream(socket.getInputStream());
            this.regional = regional;
        }

        @Override
        public void run() {
            try {
            	while(true)
            	{
                // Get reference from client
                String ref = (String) fromClient.readObject();
                
                // Get from cache
                CoursBoursier cours = regional.getCours(ref);
                
                // Send to client
                toClient.writeObject(cours);
                toClient.reset();
            	}
            } catch (IOException | ClassNotFoundException ex) {
                //Logger.getLogger(Regional.class.getName()).log(Level.SEVERE, null, ex);
            	System.out.println("Deconnexion");
            }
        }

    }
    
    public static void main(String[] args) {
        (new Regional()).runServer();
    }
}
