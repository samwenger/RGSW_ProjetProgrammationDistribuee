package Client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientConnexion implements Runnable {

    private ServerSocket mySkServer;
    private Socket socketClient;

    private InetAddress localAddress;

    private String pathToFiles;


    public ClientConnexion(String localName, String pathToFiles) throws IOException {
        this.localAddress = InetAddress.getByName(localName);
        this.pathToFiles = pathToFiles;

        mySkServer = new ServerSocket(0, 10, this.localAddress);
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

    public int getLocalPort() {
        return mySkServer.getLocalPort();
    }
}
