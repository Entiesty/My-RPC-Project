package part3.client.serviceCenter;

import java.net.InetSocketAddress;

public interface ServiceCenter {
    InetSocketAddress serviceDiscovery(String serviceName);
}
