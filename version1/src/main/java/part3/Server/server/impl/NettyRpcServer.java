package part3.Server.server.impl;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.RequiredArgsConstructor;
import part3.Server.RpcServer;
import part3.Server.netty.nettyInitializer.NettyServerInitializer;
import part3.Server.provider.ServiceProvider;

/**
 * 基于 Netty 的 RPC 服务器实现类。
 * 负责监听端口，接收客户端请求，并处理 RPC 调用。
 */
@RequiredArgsConstructor // Lombok 注解，自动生成包含 final 字段的构造方法
public class NettyRpcServer implements RpcServer {

    // 业务服务提供者，负责处理 RPC 请求
    private final ServiceProvider serviceProvider;

    /**
     * 启动 Netty RPC 服务器。
     *
     * @param port 服务器监听的端口号
     */
    @Override
    public void start(int port) {
        // 负责接收客户端连接的线程组
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();

        // 负责处理已建立连接的线程组
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        System.out.println("Netty RPC 服务器启动中...");

        try {
            // 创建 Netty 服务器启动器
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup) // 绑定线程组
                    .channel(NioServerSocketChannel.class) // 指定服务器通道类型
                    .childHandler(new NettyServerInitializer(serviceProvider)); // 设置通道初始化器

            // 绑定端口并启动服务器
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            System.out.println("Netty RPC 服务器已启动，监听端口：" + port);

            // 监听通道关闭事件，保证服务器运行
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            // 处理异常并提供详细错误信息
            Thread.currentThread().interrupt(); // 恢复线程中断状态
            throw new RuntimeException("Netty RPC 服务器运行时发生中断异常", e);
        } catch (Exception e) {
            throw new RuntimeException("Netty RPC 服务器启动失败", e);
        } finally {
            // 优雅地关闭线程组，释放资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            System.out.println("Netty RPC 服务器已关闭");
        }
    }

    /**
     * 停止 Netty RPC 服务器（暂未实现）。
     */
    @Override
    public void stop() {
        // 目前未提供具体的服务器停止逻辑
        System.out.println("Netty RPC 服务器 stop() 方法尚未实现");
    }
}
