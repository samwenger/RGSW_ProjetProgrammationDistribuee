package Client;

import java.net.InetAddress;

public class MyInetAddress {
    private String name;
    private InetAddress inetAddress;

    public MyInetAddress(String name, InetAddress inetAddress) {
        this.name = name;
        this.inetAddress = inetAddress;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }
}
