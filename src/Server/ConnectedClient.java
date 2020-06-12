package Server;

import java.net.InetAddress;
import java.util.ArrayList;

/**
 * Objet permettant de gérer la liste des clients connectés
 */
public class ConnectedClient {


    private int clientNumber;
    private InetAddress clientAddress;
    private int clientPort;
    private ArrayList<String> clientFilesList;

    /**
     * Constructeur de l'objet ConnectedClient
     * @param clientNumber
     * @param clientAddress
     * @param clientPort
     * @param clientFilesList
     */
    public ConnectedClient (int clientNumber, InetAddress clientAddress, int clientPort, ArrayList<String> clientFilesList)
    {
        this.clientNumber    = clientNumber;
        this.clientAddress   = clientAddress;
        this.clientPort      = clientPort;
        this.clientFilesList = clientFilesList;
    }

    /**
     * Récupérer l'id du client
     * @return
     */
    public int getClientNumber() {
        return clientNumber;
    }
}
