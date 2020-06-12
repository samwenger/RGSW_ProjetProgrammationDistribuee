package Client;

import java.net.InetAddress;

/**
 * Objet MyInetAddress utilisé pour la gestion de la liste des ip de la machine
 */
public class MyInetAddress {
    private String name;
    private InetAddress inetAddress;

    /**
     * Constructeur de l'objet MyInetAddress
     * @param name
     * @param inetAddress
     */
    public MyInetAddress(String name, InetAddress inetAddress) {
        this.name = name;
        this.inetAddress = inetAddress;
    }

   /* public InetAddress getInetAddress() {
        return inetAddress;
    }*/
}
