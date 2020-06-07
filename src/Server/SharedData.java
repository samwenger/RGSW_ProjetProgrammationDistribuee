package Server;

import java.util.ArrayList;

public class SharedData {

    public volatile ArrayList<ConnectedClient> clientsList = new ArrayList<>();

    public void addClient(ConnectedClient client) {
        clientsList.add(client);
    }

    public ArrayList<ConnectedClient> getClientsList() {
        return clientsList;
    }

    public void deleteClient(int id) {
        for(int i=0; i<clientsList.size(); i++){
            if(clientsList.get(i).getClientNumber() == id){
                clientsList.remove(i);
                return;
            }
        }
    }

}
