package part1.client.serviceCenter.balance.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import part1.client.serviceCenter.balance.LoadBalance;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机负载均衡实现类，从服务器地址列表中随机选择一个地址。
 */
public class RandomLoadBalance implements LoadBalance {

    private static final Logger logger = LogManager.getLogger(RandomLoadBalance.class);

    @Override
    public String selectServer(List<String> serverAddresses) {
        if (serverAddresses == null || serverAddresses.isEmpty()) {
            logger.error("服务器地址列表为空或未提供"); // 错误日志
            throw new IllegalArgumentException("服务器地址列表不能为空");
        }

        if (serverAddresses.size() == 1) {
            logger.warn("只有一个服务器地址可用，无需负载均衡"); // 警告日志
        }

        // 随机选择一个服务器地址
        int randomIndex = ThreadLocalRandom.current().nextInt(serverAddresses.size());
        String selectedServer = serverAddresses.get(randomIndex);

        logger.info("已选择服务器: {}", selectedServer); // 信息日志
        return selectedServer;
    }

    @Override
    public void addNode(String node) {
        if (node == null || node.trim().isEmpty()) {
            logger.error("节点地址为空或未提供"); // 错误日志
            throw new IllegalArgumentException("节点地址不能为空");
        }

        if (node.startsWith("192.168.")) {
            logger.warn("正在添加一个私有IP地址的节点: {}", node); // 警告日志
        }

        // 模拟添加节点逻辑
        logger.info("已添加节点: {}", node); // 信息日志
    }

    @Override
    public void removeNode(String node) {
        if (node == null || node.trim().isEmpty()) {
            logger.error("节点地址为空或未提供"); // 错误日志
            throw new IllegalArgumentException("节点地址不能为空");
        }

        if (node.startsWith("192.168.")) {
            logger.warn("正在移除一个私有IP地址的节点: {}", node); // 警告日志
        }

        // 模拟删除节点逻辑
        logger.info("已移除节点: {}", node); // 信息日志
    }
}