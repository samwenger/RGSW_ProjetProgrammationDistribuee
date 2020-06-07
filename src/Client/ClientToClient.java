package Client;

import java.io.*;
import java.net.Socket;

public class ClientToClient implements Runnable {

    private Socket clientSocket;
    private String pathToFiles;

    public ClientToClient(Socket clientSocket, String pathToFiles){
        this.clientSocket = clientSocket;
        this.pathToFiles = pathToFiles;
    }


    @Override
    public void run() {
        System.out.println("Un client s'est connecté.");

        try {

            DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
            String titre = dataInputStream.readUTF();
            System.out.println("The file asked is: " + titre);

            String pathToFile = pathToFiles + titre;

            File myFile = new File(pathToFile);
            byte[] myByteArray = new byte[(int)myFile.length()];
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
            bis.read(myByteArray,0,myByteArray.length);

            OutputStream os = clientSocket.getOutputStream();
            os.write(myByteArray,0,myByteArray.length);
            os.flush();
            clientSocket.close();

           // Thread.currentThread().interrupt();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
