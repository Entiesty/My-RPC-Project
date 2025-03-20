package part2.client.cache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务缓存管理类，存储服务名称与其可用的地址列表。
 * 采用线程安全的 {@link ConcurrentHashMap} 以保证并发安全。
 */
public class ServiceCache {
    private static final Logger logger = LogManager.getLogger(ServiceCache.class);

    /**
     * 线程安全的缓存存储服务名称与地址列表的映射关系
     */
    private static final Map<String, List<String>> SERVICE_CACHE = new ConcurrentHashMap<>();

    /**
     * 将服务及其地址添加到缓存中。
     *
     * @param serviceName 服务名称
     * @param address     服务地址
     */
    public void addService(String serviceName, String address) {
        SERVICE_CACHE.computeIfAbsent(serviceName, k -> new ArrayList<>()).add(address);
        logger.info("服务 [{}] 的地址 [{}] 已添加到缓存", serviceName, address);
    }

    /**
     * 替换服务地址。
     *
     * @param serviceName 服务名称
     * @param oldAddress  旧地址
     * @param newAddress  新地址
     */
    public void updateServiceAddress(String serviceName, String oldAddress, String newAddress) {
        List<String> addressList = SERVICE_CACHE.get(serviceName);
        if (addressList == null) {
            logger.warn("更新失败：服务 [{}] 不存在于缓存中", serviceName);
            return;
        }
        if (!addressList.contains(oldAddress)) {
            logger.warn("更新失败：服务 [{}] 的地址 [{}] 不存在", serviceName, oldAddress);
            return;
        }
        addressList.remove(oldAddress);
        addressList.add(newAddress);
        logger.info("服务 [{}] 地址 [{}] 已更新为 [{}]", serviceName, oldAddress, newAddress);
    }

    /**
     * 从缓存中获取服务地址列表。
     *
     * @param serviceName 服务名称
     * @return 服务地址列表（如果不存在，则返回空列表）
     */
    public List<String> getServiceAddresses(String serviceName) {
        return SERVICE_CACHE.getOrDefault(serviceName, Collections.emptyList());
    }

    /**
     * 从缓存中移除服务的特定地址。
     *
     * @param serviceName 服务名称
     * @param address     需要移除的地址
     */
    public void removeServiceAddress(String serviceName, String address) {
        List<String> addressList = SERVICE_CACHE.get(serviceName);
        if (addressList == null || !addressList.remove(address)) {
            logger.warn("移除失败：服务 [{}] 的地址 [{}] 不存在", serviceName, address);
            return;
        }
        logger.info("服务 [{}] 的地址 [{}] 已从缓存中移除", serviceName, address);
    }
}
