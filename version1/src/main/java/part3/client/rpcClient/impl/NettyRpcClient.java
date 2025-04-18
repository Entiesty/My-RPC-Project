package part3.client.rpcClient.impl;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import part3.client.netty.nettyInitializer.NettyClientInitializer;
import part3.client.rpcClient.RpcClient;
import part3.client.serviceCenter.ServiceCenter;
import part3.client.serviceCenter.ZKServiceCenter;
import part3.common.message.RpcRequest;
import part3.common.message.RpcResponse;

import java.net.InetSocketAddress;

/**
 * 基于 Netty 的 RPC 客户端实现类。
 * 负责与 RPC 服务器建立连接，发送请求并接收响应。
 */
public class NettyRpcClient implements RpcClient {

    // 日志记录器
    private static final Logger logger = LogManager.getLogger(NettyRpcClient.class);

    // 服务中心实例，用于服务发现
    private final ServiceCenter serviceCenter;

    // Netty 的 Bootstrap 实例，用于配置和启动客户端
    public static final Bootstrap bootstrap;

    // Netty 的 EventLoopGroup 实例，用于处理客户端的事件循环
    public static final EventLoopGroup eventLoopGroup;

    // 静态初始化块，初始化 Netty 的 Bootstrap 和 EventLoopGroup
    static {
        eventLoopGroup = new NioEventLoopGroup(); // 创建事件循环组
        bootstrap = new Bootstrap(); // 创建 Bootstrap 实例
        bootstrap.group(eventLoopGroup) // 设置事件循环组
                .channel(NioSocketChannel.class) // 使用 NIO 传输通道
                .handler(new NettyClientInitializer()); // 设置 ChannelInitializer
    }

    /**
     * 构造方法，初始化服务中心。
     */
    public NettyRpcClient() {
        this.serviceCenter = new ZKServiceCenter();
        logger.info("NettyRpcClient 初始化完成，服务中心已启动");
    }

    /**
     * 发送 RPC 请求到服务器，并接收响应。
     *
     * @param rpcRequest RPC 请求对象
     * @return RPC 响应对象
     * @throws RuntimeException 如果连接或通信过程中发生异常
     */
    @Override
    public RpcResponse sendRequest(RpcRequest rpcRequest) {
        try {
            // 从服务中心获取服务地址
            InetSocketAddress address = serviceCenter.serviceDiscovery(rpcRequest.getInterfaceName());
            String host = address.getHostName();
            int port = address.getPort();

            logger.info("发现服务地址: {}:{}", host, port);

            // 连接到服务器
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            Channel channel = channelFuture.channel();

            logger.info("成功连接到服务器: {}:{}", host, port);

            // 发送 RPC 请求
            channel.writeAndFlush(rpcRequest);
            logger.info("RPC 请求已发送: {}", rpcRequest);

            // 等待连接关闭
            channel.closeFuture().sync();
            logger.info("连接已关闭");

            // 从 Channel 的属性中获取 RPC 响应
            AttributeKey<RpcResponse> responseKey = AttributeKey.valueOf("RpcResponse");
            RpcResponse rpcResponse = channel.attr(responseKey).get();

            logger.info("收到服务器响应: {}", rpcResponse);

            // 返回响应
            return rpcResponse;
        } catch (InterruptedException e) {
            // 如果线程被中断，记录日志并抛出运行时异常
            logger.error("RPC 请求发送过程中线程被中断", e);
            Thread.currentThread().interrupt(); // 重置中断状态
            throw new RuntimeException("RPC 请求发送过程中线程被中断", e);
        } catch (Exception e) {
            // 捕获其他异常，记录日志并抛出运行时异常
            logger.error("RPC 请求发送失败", e);
            throw new RuntimeException("RPC 请求发送失败", e);
        }
    }
}