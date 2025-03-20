package part2.client.serviceCenter.balance.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import part2.client.serviceCenter.balance.LoadBalance;

import java.util.*;

/**
 * 一致性哈希算法负载均衡实现类。
 * 通过虚拟节点将请求均匀分布到真实节点上。
 */
public class ConsistencyHashLoadBalance implements LoadBalance {

    private static final Logger logger = LogManager.getLogger(ConsistencyHashLoadBalance.class);

    // 每个真实节点对应的虚拟节点数量
    public static final int VIRTUAL_NODE_NUM = 5;

    // 虚拟节点映射，key为虚拟节点的哈希值，value为虚拟节点名称
    private final SortedMap<Integer, String> virtualNodes = new TreeMap<>();

    // 真实节点列表
    private final List<String> realNodes = new LinkedList<>();

    /**
     * 初始化一致性哈希环，添加真实节点和虚拟节点。
     *
     * @param serviceList 真实节点列表
     * @throws IllegalArgumentException 如果真实节点列表为空
     */
    public void initializeHashRing(List<String> serviceList) {
        if (serviceList == null || serviceList.isEmpty()) {
            logger.error("真实节点列表为空或未提供");
            throw new IllegalArgumentException("真实节点列表不能为空");
        }

        for (String server : serviceList) {
            addRealNode(server);
        }
    }

    /**
     * 添加真实节点及其对应的虚拟节点。
     *
     * @param realNode 真实节点名称
     * @throws IllegalArgumentException 如果真实节点名称为空
     */
    public void addRealNode(String realNode) {
        if (realNode == null || realNode.trim().isEmpty()) {
            logger.error("真实节点名称为空或未提供");
            throw new IllegalArgumentException("真实节点名称不能为空");
        }

        if (!realNodes.contains(realNode)) {
            realNodes.add(realNode);
            logger.info("真实节点[{}] 已添加", realNode);

            // 添加虚拟节点
            for (int i = 0; i < VIRTUAL_NODE_NUM; i++) {
                String virtualNode = realNode + "&&VN" + i;
                int hash = getHash(virtualNode);
                virtualNodes.put(hash, virtualNode);
                logger.info("虚拟节点[{}] 已添加, Hash值: {}", virtualNode, hash);
            }
        }
    }

    /**
     * 根据请求的 key 获取分配的节点名称。
     *
     * @param key         请求的 key
     * @param serviceList 真实节点列表
     * @return 分配的节点名称
     * @throws IllegalArgumentException 如果请求的 key 为空
     */
    public String getAssignedNode(String key, List<String> serviceList) {
        if (key == null || key.trim().isEmpty()) {
            logger.error("请求的 key 为空或未提供");
            throw new IllegalArgumentException("请求的 key 不能为空");
        }

        initializeHashRing(serviceList); // 初始化哈希环

        int hash = getHash(key);
        SortedMap<Integer, String> tailMap = virtualNodes.tailMap(hash);
        int assignedHash = tailMap.isEmpty() ? virtualNodes.firstKey() : tailMap.firstKey();
        String virtualNode = virtualNodes.get(assignedHash);

        // 返回真实节点名称
        return virtualNode.substring(0, virtualNode.indexOf("&&"));
    }

    /**
     * 删除真实节点及其对应的虚拟节点。
     *
     * @param realNode 真实节点名称
     */
    public void removeRealNode(String realNode) {
        if (realNode == null || realNode.trim().isEmpty()) {
            logger.error("真实节点名称为空或未提供");
            throw new IllegalArgumentException("真实节点名称不能为空");
        }

        if (realNodes.contains(realNode)) {
            realNodes.remove(realNode);
            logger.info("真实节点[{}] 已移除", realNode);

            // 移除虚拟节点
            for (int i = 0; i < VIRTUAL_NODE_NUM; i++) {
                String virtualNode = realNode + "&&VN" + i;
                int hash = getHash(virtualNode);
                virtualNodes.remove(hash);
                logger.info("虚拟节点[{}] 已移除, Hash值: {}", virtualNode, hash);
            }
        }
    }

    /**
     * 使用 FNV1_32_HASH 算法计算字符串的哈希值。
     *
     * @param str 输入字符串
     * @return 哈希值
     */
    private static int getHash(String str) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < str.length(); i++) {
            hash = (hash ^ str.charAt(i)) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;

        // 如果算出来的值为负数则取其绝对值
        if (hash < 0) {
            hash = Math.abs(hash);
        }
        return hash;
    }

    @Override
    public String selectServer(List<String> serverAddresses) {
        String key = UUID.randomUUID().toString(); // 生成随机请求 key
        return getAssignedNode(key, serverAddresses);
    }

    @Override
    public void addNode(String node) {
        addRealNode(node);
    }

    @Override
    public void removeNode(String node) {
        removeRealNode(node);
    }
}