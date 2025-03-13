package part2.Server.serviceRegister.impl;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import part2.Server.serviceRegister.ServiceRegister;

import java.net.InetSocketAddress;

/**
 * 基于 Zookeeper 的服务注册实现类。
 * 负责将服务注册到 Zookeeper，以便客户端能够发现服务。
 */
public class ZKServiceRegister implements ServiceRegister {

    // 日志记录器
    private static final Logger logger = LogManager.getLogger(ZKServiceRegister.class);

    // Zookeeper 客户端实例
    private final CuratorFramework client;

    // Zookeeper 的根路径
    public static final String ROOT_PATH = "MyRPC";

    /**
     * 构造方法，初始化 Zookeeper 客户端。
     */
    public ZKServiceRegister() {
        // 重试策略：初始重试间隔 1000ms，最多重试 3 次
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

        // 创建 Zookeeper 客户端
        this.client = CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1:2181") // Zookeeper 服务器地址
                .retryPolicy(retryPolicy) // 设置重试策略
                .sessionTimeoutMs(40000) // 会话超时时间
                .namespace(ROOT_PATH) // 设置命名空间
                .build();

        // 启动客户端
        this.client.start();
        logger.info("Zookeeper 客户端启动成功，根路径: {}", ROOT_PATH);
    }

    /**
     * 注册服务到 Zookeeper。
     *
     * @param serviceName    服务名称
     * @param serviceAddress 服务地址
     * @throws RuntimeException 如果注册过程中发生异常
     */
    @Override
    public void register(String serviceName, InetSocketAddress serviceAddress) {
        try {
            // 检查服务节点是否存在，如果不存在则创建持久节点
            if (client.checkExists().forPath("/" + serviceName) == null) {
                client.create()
                        .creatingParentsIfNeeded() // 如果父节点不存在则创建
                        .withMode(CreateMode.PERSISTENT) // 创建持久节点
                        .forPath("/" + serviceName);
                logger.info("创建服务节点: /{}", serviceName);
            }

            // 构建服务地址路径
            String path = "/" + serviceName + "/" + getServiceAddress(serviceAddress);

            // 创建临时节点，存储服务地址
            client.create()
                    .creatingParentsIfNeeded() // 如果父节点不存在则创建
                    .withMode(CreateMode.EPHEMERAL) // 创建临时节点
                    .forPath(path);

            logger.info("注册服务地址: {}", path);
        } catch (Exception e) {
            // 捕获异常，记录日志并抛出运行时异常
            logger.error("服务注册失败，服务名称: {}", serviceName, e);
            throw new RuntimeException("服务注册失败", e);
        }
    }

    /**
     * 将服务地址转换为字符串格式（IP:Port）。
     *
     * @param serviceAddress 服务地址
     * @return 服务地址字符串
     */
    private String getServiceAddress(InetSocketAddress serviceAddress) {
        return serviceAddress.getHostName() + ":" + serviceAddress.getPort();
    }
}