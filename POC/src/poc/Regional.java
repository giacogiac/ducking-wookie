package poc;

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

public class Regional {

    private static final int BOURSE_PORT = 5555;
    private static final int DELAIS_EXPIRATION = 5000; // 5 sec
    private final ConcurrentMap<String, SortedSet<CoursBoursier>> bourse = new ConcurrentHashMap<>();

    private Socket socketCentral = null;
    private ObjectInputStream fromCentral = null;
    private ObjectOutputStream toCentral = null;

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
            // Update cache
            return (CoursBoursier) fromCentral.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(Regional.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }


    public Regional() {
        try {
            this.socketCentral = new Socket("", BOURSE_PORT);
            this.fromCentral = new ObjectInputStream(socketCentral.getInputStream());
            this.toCentral = new ObjectOutputStream(socketCentral.getOutputStream());
        } catch (IOException ex) {
            Logger.getLogger(Regional.class.getName()).log(Level.SEVERE, null, ex);
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
                new Thread(handler).start();
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
            this.fromClient = new ObjectInputStream(socket.getInputStream());
            this.toClient = new ObjectOutputStream(socket.getOutputStream());
            this.regional = regional;
        }

        @Override
        public void run() {
            try {
                // Get reference from client
                String ref = (String) fromClient.readObject();

                // Get from cache
                CoursBoursier cours = regional.getCours(ref);
                // Send to client
                toClient.writeObject(cours);

            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(Regional.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
    
    public static void main(String[] args) {
        (new Regional()).runServer();
    }
}
