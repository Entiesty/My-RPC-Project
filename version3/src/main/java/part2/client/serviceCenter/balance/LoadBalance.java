package part2.client.serviceCenter.balance;

import java.util.List;

/**
 * 负载均衡接口，定义了负载均衡的基本操作。
 */
public interface LoadBalance {
    /**
     * 从地址列表中选择一个地址进行负载均衡。
     *
     * @param serverAddresses 服务器地址列表
     * @return 选择的服务器地址
     * @throws IllegalArgumentException 如果服务器地址列表为空或null
     */
    String selectServer(List<String> serverAddresses);

    /**
     * 添加一个新的节点到负载均衡器中。
     *
     * @param node 要添加的节点地址
     * @throws IllegalArgumentException 如果节点地址为空或null
     */
    void addNode(String node);

    /**
     * 从负载均衡器中删除一个节点。
     *
     * @param node 要删除的节点地址
     * @throws IllegalArgumentException 如果节点地址为空或null
     */
    void removeNode(String node);
}
