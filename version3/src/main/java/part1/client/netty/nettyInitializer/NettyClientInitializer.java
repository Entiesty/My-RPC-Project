package part1.client.netty.nettyInitializer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import part1.client.netty.handler.NettyClientHandler;
import part1.common.serializer.myCode.MyDecoder;
import part1.common.serializer.myCode.MyEncoder;
import part1.common.serializer.mySerializer.JsonSerializer;

/**
 * Netty 客户端初始化器，用于配置客户端的 ChannelPipeline。
 * 每个新连接建立时，都会调用此初始化器来配置对应的 ChannelPipeline。
 */
public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {

    /**
     * 初始化 ChannelPipeline，添加所需的 ChannelHandler。
     *
     * @param socketChannel 客户端的 SocketChannel
     * @throws Exception 如果初始化过程中发生异常
     */
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        // 获取 ChannelPipeline
        ChannelPipeline pipeline = socketChannel.pipeline();

        // 添加 ObjectEncoder，用于将 Java 对象编码为字节流
        pipeline.addLast(new MyEncoder(new JsonSerializer()));

        // 添加 ObjectDecoder，用于将字节流解码为 Java 对象
        pipeline.addLast(new MyDecoder());

        // 添加自定义的 NettyClientHandler，用于处理业务逻辑
        pipeline.addLast(new NettyClientHandler());
    }
}