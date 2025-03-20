package part2.Server;


import part2.Server.provider.ServiceProvider;
import part2.Server.server.impl.NettyRpcServer;
import part2.common.service.UserService;
import part2.common.service.impl.UserServiceImpl;

/**
 * RPC 服务器测试类。
 * 负责启动 Netty RPC 服务器，并注册服务。
 */
public class TestServer {
    public static void main(String[] args) {
        // 创建并初始化用户服务
        UserService userService = new UserServiceImpl();

        // 创建服务提供者并注册服务
        ServiceProvider serviceProvider = new ServiceProvider("127.0.0.1", 9999);
        serviceProvider.register(userService, true);
        System.out.println("服务已注册：" + userService.getClass().getSimpleName());

        // 创建并启动 RPC 服务器
        RpcServer rpcServer = new NettyRpcServer(serviceProvider);
        int port = 9999; // 服务器监听端口

        try {
            System.out.println("正在启动 Netty RPC 服务器...");
            rpcServer.start(port);
        } catch (Exception e) {
            System.err.println("Netty RPC 服务器启动失败：" + e.getMessage());
        }
    }
}
