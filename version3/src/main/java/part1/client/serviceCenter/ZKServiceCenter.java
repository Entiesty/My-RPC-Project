package part1.client.serviceCenter;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import part1.client.cache.ServiceCache;
import part1.client.serviceCenter.ZKWatcher.ZookeeperWatcher;
import part1.client.serviceCenter.balance.impl.ConsistencyHashLoadBalance;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 基于 Zookeeper 的服务中心实现类。
 * 用于服务注册与服务发现。
 */
public class ZKServiceCenter implements ServiceCenter {

    // 日志记录器
    private static final Logger logger = LogManager.getLogger(ZKServiceCenter.class);

    // Zookeeper 客户端实例
    private final CuratorFramework client;

    // Zookeeper 的根路径
    public static final String ROOT_PATH = "MyRPC";

    private final ServiceCache serviceCache;

    /**
     * 构造方法，初始化 Zookeeper 客户端。
     */
    public ZKServiceCenter() {
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
        serviceCache = new ServiceCache();
        ZookeeperWatcher watcher = new ZookeeperWatcher(client, serviceCache);
        watcher.startWatching(ROOT_PATH);

        logger.info("Zookeeper 连接成功！");
    }

    /**
     * 服务发现方法，根据服务名称从 Zookeeper 中获取服务地址。
     *
     * @param serviceName 服务名称
     * @return 服务地址（IP 和端口）
     * @throws RuntimeException 如果服务发现过程中发生异常
     */
    @Override
    public InetSocketAddress serviceDiscovery(String serviceName) {
        try {
            // 1️⃣ 先尝试从本地缓存获取服务地址
            List<String> addressList = serviceCache.getServiceAddresses(serviceName);

            // 2️⃣ 如果缓存为空，则从 Zookeeper 获取最新的服务地址
            if (addressList == null || addressList.isEmpty()) {
                addressList = client.getChildren().forPath("/" + serviceName);
            }

            // 3️⃣ 再次检查 addressList 是否为空
            if (addressList == null || addressList.isEmpty()) {
                logger.error("服务发现失败，未找到可用的服务地址: {}", serviceName);
                throw new RuntimeException("服务发现失败，未找到可用的服务地址: " + serviceName);
            }

            // 4️⃣ 获取第一个可用的服务地址
            String address = new ConsistencyHashLoadBalance().selectServer(addressList);

            // 5️⃣ 解析地址
            InetSocketAddress inetSocketAddress = parseAddress(address);

            logger.info("成功发现服务 [{}]，地址: {}", serviceName, inetSocketAddress);
            return inetSocketAddress;
        } catch (Exception e) {
            logger.error("服务发现失败，服务名称: {}", serviceName, e);
            throw new RuntimeException("服务发现失败", e);
        }
    }


    /**
     * 解析服务地址字符串，将其转换为 InetSocketAddress 对象。
     *
     * @param address 服务地址字符串（格式：IP:Port）
     * @return InetSocketAddress 对象
     */
    private InetSocketAddress parseAddress(String address) {
        // 将地址字符串按 ":" 分割为 IP 和端口
        String[] parts = address.split(":");

        // 返回 InetSocketAddress 对象
        return new InetSocketAddress(parts[0], Integer.parseInt(parts[1]));
    }
}