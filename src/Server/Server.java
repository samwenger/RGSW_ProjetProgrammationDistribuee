package Server;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.logging.Logger;


public class Server {

    private String interfaceName;
    private int serverPort;

    private InetAddress localAddress;
    private ServerSocket serverSocket;

    private int clientNumber;

    private SharedData sharedData;

    private Logger logger;



    public Server(Logger logger, String interfaceName, int serverPort) {
        this.logger = logger;
        this.interfaceName = interfaceName;
        this.serverPort = serverPort;

        this.clientNumber = 1;

        sharedData = new SharedData();
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



    public void acceptConnexions() {
        while(true)
        {
            try {
                // Accepter la connexion
                Socket clientSocket = serverSocket.accept();
                logger.info("A new client has been accepted.");

                // Thread pour chaque connexion
                AcceptClient acceptClient = new AcceptClient(logger, clientSocket, clientNumber, sharedData);
                Thread t = new Thread(acceptClient);

                t.start();

                clientNumber++;

            } catch (IOException e) {
                logger.severe("IOException thrown when accepting new connexions");
                e.printStackTrace();
            }

        }

    }

}
