package part1.client.serviceCenter.ZKWatcher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import part1.client.cache.ServiceCache;

/**
 * 监听 Zookeeper 变更事件（节点创建、修改、删除），并同步本地缓存。
 */
@Slf4j
@RequiredArgsConstructor
public class ZookeeperWatcher {
    private final CuratorFramework client;  // Zookeeper 客户端
    private final ServiceCache serviceCache; // 本地缓存（存储服务列表）

    /**
     * 监听指定路径的节点变更，并同步到本地缓存
     *
     * @param path 要监听的 ZooKeeper 路径
     */
    public void startWatching(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path; // 自动补全 `/`
        }
        log.info("开始监听 Zookeeper 变更: {}", path);
        try {
            CuratorCache curatorCache = CuratorCache.build(client, path);
            curatorCache.listenable().addListener((type, oldData, newData) -> {
                switch (type) {
                    case NODE_CREATED:
                        handleNodeCreated(newData);
                        break;
                    case NODE_CHANGED:
                        handleNodeChanged(oldData, newData);
                        break;
                    case NODE_DELETED:
                        handleNodeDeleted(oldData);
                        break;
                    default:
                        log.warn("未处理的 Zookeeper 事件类型: {}", type);
                }
            });
            curatorCache.start();
        } catch (Exception e) {
            log.error("Zookeeper 监听初始化失败", e);
        }
    }

    /**
     * 处理节点创建事件
     */
    private void handleNodeCreated(ChildData newData) {
        if (newData == null) return;

        String[] pathParts = parsePath(newData);
        if (pathParts.length <= 2) {
            log.warn("节点路径格式错误: {}，跳过处理", newData.getPath());
            return;
        }

        String serviceName = pathParts[1];
        String address = pathParts[2];
        serviceCache.addService(serviceName, address);
        log.info("新服务注册: {} -> {}", serviceName, address);
    }

    /**
     * 处理节点更新事件
     */
    private void handleNodeChanged(ChildData oldData, ChildData newData) {
        if (oldData == null || newData == null) return;

        String[] oldPathParts = parsePath(oldData);
        String[] newPathParts = parsePath(newData);

        if (oldPathParts.length <= 2 || newPathParts.length <= 2) {
            log.warn("节点路径格式错误: {} -> {}，跳过处理", oldData.getPath(), newData.getPath());
            return;
        }

        String serviceName = oldPathParts[1];
        String oldAddress = oldPathParts[2];
        String newAddress = newPathParts[2];

        serviceCache.updateServiceAddress(serviceName, oldAddress, newAddress);
        log.info("服务地址更新: {} {} -> {}", serviceName, oldAddress, newAddress);
    }

    /**
     * 处理节点删除事件
     */
    private void handleNodeDeleted(ChildData oldData) {
        if (oldData == null) return;

        String[] pathParts = parsePath(oldData);
        if (pathParts.length <= 2) {
            log.warn("节点路径格式错误: {}，跳过处理", oldData.getPath());
            return;
        }

        String serviceName = pathParts[1];
        String address = pathParts[2];
        serviceCache.removeServiceAddress(serviceName, address);
        log.info("服务删除: {} -> {}", serviceName, address);
    }

    /**
     * 解析 Zookeeper 节点路径，例如 `/serviceA/127.0.0.1:8080` 解析为 `["", "serviceA", "127.0.0.1:8080"]`
     *
     * @param childData Zookeeper 监听到的节点数据
     * @return 解析后的路径数组
     */
    private String[] parsePath(ChildData childData) {
        return childData.getPath().split("/");
    }
}
