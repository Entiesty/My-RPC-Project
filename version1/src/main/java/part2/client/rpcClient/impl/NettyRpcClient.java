package part2.client.rpcClient.impl;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import part2.client.netty.nettyInitializer.NettyClientInitializer;
import part2.client.rpcClient.RpcClient;
import part2.common.message.RpcRequest;
import part2.common.message.RpcResponse;

/**
 * 基于 Netty 的 RPC 客户端实现类。
 * 负责与 RPC 服务器建立连接，发送请求并接收响应。
 */
@RequiredArgsConstructor // Lombok 注解，自动生成包含 final 字段的构造方法
public class NettyRpcClient implements RpcClient {

    // 服务器的主机地址
    private final String host;

    // 服务器的端口号
    private final int port;

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
     * 发送 RPC 请求到服务器，并接收响应。
     *
     * @param rpcRequest RPC 请求对象
     * @return RPC 响应对象
     * @throws RuntimeException 如果连接或通信过程中发生异常
     */
    @Override
    public RpcResponse sendRequest(RpcRequest rpcRequest) {
        try {
            // 连接到服务器
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            Channel channel = channelFuture.channel();

            // 发送 RPC 请求
            channel.writeAndFlush(rpcRequest);

            // 等待连接关闭
            channel.closeFuture().sync();

            // 从 Channel 的属性中获取 RPC 响应
            AttributeKey<RpcResponse> responseKey = AttributeKey.valueOf("RpcResponse");
            RpcResponse rpcResponse = channel.attr(responseKey).get();

            // 打印响应信息（用于调试）
            System.out.println("收到服务器响应: " + rpcResponse);

            // 返回响应
            return rpcResponse;
        } catch (InterruptedException e) {
            // 如果线程被中断，抛出运行时异常
            Thread.currentThread().interrupt(); // 重置中断状态
            throw new RuntimeException("RPC 请求发送过程中线程被中断", e);
        } catch (Exception e) {
            // 捕获其他异常并抛出运行时异常
            throw new RuntimeException("RPC 请求发送失败", e);
        }
    }
}
