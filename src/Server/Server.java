package Server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;


/**
 * Classe pour gérer l'objet serveur
 */
public class Server {

    private String interfaceName;
    private InetAddress localAddress;
    private int serverPort;

    private ServerSocket serverSocket;
    private Socket clientSocket;

    private int clientNumber;
    private ArrayList<ConnectedClient> clients = new ArrayList<>();


    public Logger logger = LogManager.getLogger(Server.class);


    /**
     * Constructeur du serveur
     *
     * @param interfaceName
     * @param serverPort
     */
    public Server(String interfaceName, int serverPort) {
        this.interfaceName = interfaceName;
        this.serverPort = serverPort;

        this.clientNumber = 1;
    }


    /**
     * Récupération de l'adresse locale (ip) à partir d'une interface
     */
    public void getLocalAddress() {

        try {
            NetworkInterface ni = NetworkInterface.getByName(this.interfaceName);

            Enumeration<InetAddress> inetAddresses =  ni.getInetAddresses();

            while(inetAddresses.hasMoreElements()) {
                InetAddress ia = inetAddresses.nextElement();

                if(!ia.isLinkLocalAddress()) {
                    if(!ia.isLoopbackAddress()) {
                        this.localAddress = ia;
                    }
                }
            }

        } catch (SocketException e) {
            logger.error("SocketException thrown when trying to get InetAddress");
            e.printStackTrace();
        }
    }


    /**
     * Création d'un socket de serveur pour recevoir les connexions
     */
    public void createSocket() {
        try {
            serverSocket = new ServerSocket(serverPort, 10, localAddress);
        } catch (IOException e) {
            logger.error("IOException thrown when trying to create ServerSocket");
            e.printStackTrace();
        }
    }


    /**
     * Information de confirmation de connexion du serveur
     */
    public void displayInfos() {
        logger.info("Server has been started. Accessible at : " + serverSocket.getInetAddress() + ":" + serverSocket.getLocalPort());
        System.out.println();
    }


    /**
     * Ecoute du socket et acceptation des connexions de clients (multi-thread)
     * @throws IOException
     */
    public void acceptConnexions() throws IOException {
        while(true)
        {
            try {
                clientSocket = serverSocket.accept();
                DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());

                Thread t = new Thread(new AcceptClient(this, clientNumber, clientSocket, dataInputStream, dataOutputStream, objectInputStream));

                logger.info("A new client has been accepted.");

                t.start();

                clientNumber++;

            } catch (Exception e1) {
                logger.error("Exception thrown when creating thread");
                try {
                    clientSocket.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                e1.printStackTrace();
            }

        }

    }

    /**
     * Retirer un client de la liste des clients connectés
     * @param client
     */
    public void removeClientFromList(ConnectedClient client) {
        clients.remove(client);
    }

    /**
     * Récupérer la liste des clients connectés avec leurs sons disponibles
     * @return
     */
    public ArrayList<ConnectedClient> getClientList() {
        return clients;
    }

    /**
     * Ajouter un nouveau client et sa liste de sons disponibles à la liste
     * @param client
     */
    public void addClient(ConnectedClient client) {
        clients.add(client);
    }

}
