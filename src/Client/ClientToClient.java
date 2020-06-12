package Client;

import java.io.*;
import java.net.Socket;

/**
 * Classe gérant la communication entre les clients (p2p) (multi-thread)
 */
public class ClientToClient implements Runnable {

    private Socket clientSocket;
    private String pathToFiles;

    /**
     * Constructeur de la classe ClientToClient
     * @param clientSocket
     * @param pathToFiles
     */
    public ClientToClient(Socket clientSocket, String pathToFiles){
        this.clientSocket = clientSocket;
        this.pathToFiles = pathToFiles;
    }


    /**
     * Gère les échanges entre les clients (p2p)
     */
    @Override
    public void run() {
        try {

            // Récupération du titre demandé
            DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
            String titre = dataInputStream.readUTF();

            // Accès au fichier local
            String pathToFile = pathToFiles + "/" + titre;
            File myFile = new File(pathToFile);

            // Conversion en byte array
            byte[] myByteArray = new byte[(int)myFile.length()];
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
            bis.read(myByteArray,0,myByteArray.length);

            // Stream au client (p2p)
            OutputStream os = clientSocket.getOutputStream();
            os.write(myByteArray,0,myByteArray.length);
            os.flush();
            clientSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
