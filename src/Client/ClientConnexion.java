package Client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientConnexion implements Runnable {

    private ServerSocket mySkServer;
    private Socket socketClient;

    private InetAddress localAddress;
    private int localPort;

    private String pathToFiles;


    public ClientConnexion(int localPort, String localName, String pathToFiles) throws IOException {
        this.localPort = localPort;
        this.localAddress = InetAddress.getByName(localName);
        this.pathToFiles = pathToFiles;

        mySkServer = new ServerSocket(this.localPort, 10, this.localAddress);
    }

    @Override
    public void run() {
        // attendre des connexions

        while(true) {
            try {
                socketClient = mySkServer.accept();
                ClientToClient p2p = new ClientToClient(socketClient, pathToFiles);
                Thread t = new Thread(p2p);
                t.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
