package part1.Server;

import part1.Server.provider.ServiceProvider;
import part1.Server.server.impl.ThreadPoolRpcServer;
import part1.common.service.UserService;
import part1.common.service.impl.UserServiceImpl;

public class TestServer {
    public static void main(String[] args) {
        UserService userService = new UserServiceImpl();
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.registerService(userService);
        RpcServer rpcServer = new ThreadPoolRpcServer(serviceProvider);

        rpcServer.start(9999);
    }
}
