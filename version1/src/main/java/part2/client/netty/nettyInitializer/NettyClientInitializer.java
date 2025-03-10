package part2.client.netty.nettyInitializer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import part2.client.netty.handler.NettyClientHandler;

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

        // 添加 LengthFieldBasedFrameDecoder，用于解决 TCP 粘包/拆包问题
        pipeline.addLast(new LengthFieldBasedFrameDecoder(
                Integer.MAX_VALUE, // 最大帧长度
                0,                 // 长度字段的偏移量
                4,                 // 长度字段的长度
                0,                 // 长度调整值
                4                  // 需要跳过的字节数
        ));

        // 添加 LengthFieldPrepender，用于在消息前添加长度字段
        pipeline.addLast(new LengthFieldPrepender(4));

        // 添加 ObjectEncoder，用于将 Java 对象编码为字节流
        pipeline.addLast(new ObjectEncoder());

        // 添加 ObjectDecoder，用于将字节流解码为 Java 对象
        pipeline.addLast(new ObjectDecoder(Class::forName));

        // 添加自定义的 NettyClientHandler，用于处理业务逻辑
        pipeline.addLast(new NettyClientHandler());
    }
}