package Server;

import java.net.InetAddress;
import java.util.ArrayList;

public class ConnectedClient {


    private int clientNumber;
    private InetAddress clientAddress;
    private int clientPort;
    private ArrayList<String> clientFilesList;

    //Constructor
    public ConnectedClient (int clientNumber, InetAddress clientAddress, int clientPort, ArrayList<String> clientFilesList)
    {
        this.clientNumber    = clientNumber;
        this.clientAddress   = clientAddress;
        this.clientPort      = clientPort;
        this.clientFilesList = clientFilesList;
    }

    public int getClientNumber () {
        return clientNumber;
    }

}
