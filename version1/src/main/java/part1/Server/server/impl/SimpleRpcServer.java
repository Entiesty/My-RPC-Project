package part1.Server.server.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import part1.Server.RpcServer;
import part1.Server.provider.ServiceProvider;
import part1.Server.server.work.WorkThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 简单的 RPC 服务器实现类，用于启动和停止 RPC 服务。
 * 该服务器会监听指定端口，并为每个客户端连接创建一个新的工作线程来处理 RPC 请求。
 */
@RequiredArgsConstructor // Lombok 注解，自动生成包含所有 final 字段的构造方法
public class SimpleRpcServer implements RpcServer {

    // 服务提供者，用于根据接口名称获取服务实现类
    private final ServiceProvider serviceProvider;

    // 日志记录器
    public static final Logger logger = LoggerFactory.getLogger(SimpleRpcServer.class);

    /**
     * 启动 RPC 服务器，监听指定端口并处理客户端连接。
     *
     * @param port 服务器监听的端口号
     */
    @Override
    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("RPC 服务器已启动，正在监听端口: {}", port);

            // 持续监听客户端连接
            while (true) {
                // 接受客户端连接
                Socket clientSocket = serverSocket.accept();
                logger.info("接收到客户端连接: {}", clientSocket.getRemoteSocketAddress());

                // 为每个客户端连接创建一个新的工作线程
                new Thread(new WorkThread(clientSocket, serviceProvider)).start();
            }
        } catch (IOException e) {
            logger.error("服务器启动或监听过程中发生错误: {}", e.getMessage(), e);
        }
    }

    /**
     * 停止 RPC 服务器。
     * 当前实现为空，可根据需要扩展实现具体的停止逻辑。
     */
    @Override
    public void stop() {
        logger.info("RPC 服务器已停止");
        // 可在此处添加资源释放或关闭连接的逻辑
    }
}
