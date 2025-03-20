package part2.Server.netty.nettyInitializer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import lombok.RequiredArgsConstructor;
import part2.Server.netty.handler.NettyServerHandler;
import part2.Server.provider.ServiceProvider;
import part2.common.serializer.myCode.MyDecoder;
import part2.common.serializer.myCode.MyEncoder;
import part2.common.serializer.mySerializer.JsonSerializer;

/**
 * Netty 服务器通道初始化器。
 * 该类用于配置服务器端的 Netty 管道（Pipeline），
 * 主要负责添加编解码器和业务处理器。
 */
@RequiredArgsConstructor // Lombok 注解，自动生成包含 final 字段的构造方法
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

    // 服务提供者，用于处理具体的 RPC 业务逻辑
    private final ServiceProvider serviceProvider;

    /**
     * 初始化 Netty 通道。
     *
     * @param socketChannel 连接的客户端通道
     * @throws Exception 如果初始化过程中出现异常
     */
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        try {
            // 获取通道的管道对象，用于添加处理器
            ChannelPipeline pipeline = socketChannel.pipeline();

            // 添加对象编码器，将 Java 对象转换为字节流
            pipeline.addLast(new MyEncoder(new JsonSerializer()));

            // 添加对象解码器，将字节流转换回 Java 对象
            pipeline.addLast(new MyDecoder());

            // 添加自定义的 Netty 服务器处理器，用于处理 RPC 请求
            pipeline.addLast(new NettyServerHandler(serviceProvider));
        } catch (Exception e) {
            // 捕获异常并抛出更详细的错误信息
            throw new RuntimeException("初始化 Netty 服务器通道失败", e);
        }
    }
}
