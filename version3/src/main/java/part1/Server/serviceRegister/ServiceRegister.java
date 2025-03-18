package part2.Server.serviceRegister;

import java.net.InetSocketAddress;

public interface ServiceRegister {
    void register(String serviceName, InetSocketAddress serviceAddress);
}
