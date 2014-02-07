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
    private final ConcurrentMap<String, SortedSet<CoursBoursier>> bourse = new ConcurrentHashMap<>();

    private Socket socketCentral = null;
    private ObjectInputStream fromCentral = null;
    private ObjectOutputStream toCentral = null;

    /**
     * Récupération du dernier cours en fonction de la référence de l'entreprise
     *
     * @param ref Identificateur de l'entreprise
     * @return Dernier cours boursier mis en cache
     */
    public CoursBoursier getCours(String ref) {
        bourse.putIfAbsent(ref, new TreeSet<CoursBoursier>());
        CoursBoursier dernierCours;
        
        long currentTime = Calendar.getInstance().getTimeInMillis();
        if (!bourse.get(ref).isEmpty()) {
            dernierCours = bourse.get(ref).last();
            if(currentTime - dernierCours.time < 5)
                return dernierCours;
        }
        
        dernierCours = getFromCentral(ref);
        // Sauvegrade dans le cache
        bourse.get(ref).add(dernierCours);
        
        return dernierCours;
    }

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

    public void addCours(CoursBoursier cours) {
        String ref = cours.ISIN;
        if (bourse.containsKey(ref)) {
            bourse.get(ref).add(cours);
        } else {
            SortedSet<CoursBoursier> set = new TreeSet<>();
            set.add(cours);
            bourse.put(ref, set);
        }
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
}
