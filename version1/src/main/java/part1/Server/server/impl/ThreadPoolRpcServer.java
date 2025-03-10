package part1.Server.server.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import part1.Server.RpcServer;
import part1.Server.provider.ServiceProvider;
import part1.Server.server.work.WorkThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 基于线程池的 RPC 服务器实现类。
 * 该服务器使用线程池来处理客户端连接，每个客户端连接会被分配到一个线程中执行。
 */
public class ThreadPoolRpcServer implements RpcServer {

    // 线程池，用于处理客户端连接
    private final ThreadPoolExecutor threadPool;

    // 服务提供者，用于根据接口名称获取服务实现类
    private final ServiceProvider serviceProvider;
    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolRpcServer.class);

    /**
     * 构造函数，初始化线程池和服务提供者。
     * 使用默认的线程池参数：
     * - 核心线程数：CPU 核心数
     * - 最大线程数：1000
     * - 空闲线程存活时间：60 秒
     * - 工作队列容量：100
     *
     * @param serviceProvider 服务提供者
     */
    public ThreadPoolRpcServer(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
        this.threadPool = new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors(), // 核心线程数
                1000, // 最大线程数
                60, // 空闲线程存活时间
                TimeUnit.SECONDS, // 时间单位
                new ArrayBlockingQueue<>(100) // 工作队列
        );
    }

    /**
     * 构造函数，初始化线程池和服务提供者。
     * 允许自定义线程池参数。
     *
     * @param serviceProvider 服务提供者
     * @param corePoolSize    核心线程数
     * @param maximumPoolSize 最大线程数
     * @param keepAliveTime   空闲线程存活时间
     * @param unit            时间单位
     * @param workQueue       工作队列
     */
    public ThreadPoolRpcServer(ServiceProvider serviceProvider,
                               int corePoolSize,
                               int maximumPoolSize,
                               long keepAliveTime,
                               TimeUnit unit,
                               ArrayBlockingQueue<Runnable> workQueue) {
        this.serviceProvider = serviceProvider;
        this.threadPool = new ThreadPoolExecutor(
                corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    /**
     * 启动 RPC 服务器，监听指定端口并处理客户端连接。
     * 每个客户端连接会被提交到线程池中执行。
     *
     * @param port 服务器监听的端口号
     */
    @Override
    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("RPC 服务器已启动，正在监听端口: " + port);

            // 持续监听客户端连接
            while (true) {
                // 接受客户端连接
                Socket clientSocket = serverSocket.accept();
                System.out.println("接收到客户端连接: " + clientSocket.getRemoteSocketAddress());

                // 将客户端连接提交到线程池中处理
                threadPool.execute(new WorkThread(clientSocket, serviceProvider));
            }
        } catch (IOException e) {
            System.err.println("服务器启动或监听过程中发生错误: " + e.getMessage());
            logger.error("服务器启动或监听过程中发生错误: {}", e.getMessage(), e);
        }
    }

    /**
     * 停止 RPC 服务器。
     * 当前实现为空，可根据需要扩展实现具体的停止逻辑。
     */
    @Override
    public void stop() {
        System.out.println("RPC 服务器已停止");
        // 关闭线程池
        threadPool.shutdown();
    }
}