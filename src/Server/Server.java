package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Logger;


public class Server {

    private String interfaceName;
    private int serverPort;

    private InetAddress localAddress;
    private ServerSocket serverSocket;
    private Socket clientSocket;

    private int clientNumber;

    private Logger logger;

    private ArrayList<ConnectedClient> clients = new ArrayList<>();


    public Server(Logger logger, String interfaceName, int serverPort) {
        this.logger = logger;
        this.interfaceName = interfaceName;
        this.serverPort = serverPort;

        this.clientNumber = 1;
    }



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
            logger.severe("SocketException thrown when trying to get InetAddress");
            e.printStackTrace();
        }
    }


    public void createSocket() {
        try {
            serverSocket = new ServerSocket(serverPort, 10, localAddress);
        } catch (IOException e) {
            logger.severe("IOException thrown when trying to create ServerSocket");
            e.printStackTrace();
        }
    }



    public void displayInfos() {
        logger.info("Server has been started. Accessible at : " + serverSocket.getInetAddress() + ":" + serverSocket.getLocalPort());
        System.out.println();
    }



    public void acceptConnexions() throws IOException {
        while(true)
        {
            try {
                clientSocket = serverSocket.accept();
                DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());

                Thread t = new Thread(new AcceptClient(this, clientNumber, logger, clientSocket, dataInputStream, dataOutputStream, objectInputStream));

                logger.info("A new client has been accepted. ");

                t.start();

                clientNumber++;

            } catch (Exception e1) {
                try {
                    clientSocket.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                e1.printStackTrace();
            }

        }

    }

    public void removeClientFromList(ConnectedClient client) {
        clients.remove(client);
    }

    public ArrayList<ConnectedClient> getClientList() {
        return clients;
    }

    public void addClient(ConnectedClient client) {
        clients.add(client);
    }

}
