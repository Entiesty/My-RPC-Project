package part1.client.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import part1.common.message.RpcResponse;

/**
 * Netty 客户端处理器，用于处理从服务端接收到的 RPC 响应。
 * 继承自 SimpleChannelInboundHandler，专门处理 RpcResponse 类型的消息。
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    /**
     * 当从服务端接收到 RPC 响应时，会触发此方法。
     *
     * @param channelHandlerContext ChannelHandler 上下文，用于管理 Channel 的生命周期和操作
     * @param rpcResponse           从服务端接收到的 RPC 响应
     * @throws Exception 如果处理过程中发生异常
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse)
            throws Exception {
        // 定义 AttributeKey，用于存储 RPC 响应
        AttributeKey<RpcResponse> responseKey = AttributeKey.valueOf("RpcResponse");

        // 将 RPC 响应存储到 Channel 的属性中
        channelHandlerContext.channel().attr(responseKey).set(rpcResponse);

        // 关闭 Channel，表示本次请求-响应已完成
        channelHandlerContext.channel().close();
    }

    /**
     * 当处理过程中发生异常时，会触发此方法。
     *
     * @param ctx   ChannelHandler 上下文
     * @param cause 异常对象
     * @throws Exception 如果异常处理过程中发生异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 记录异常信息
        System.err.println("客户端处理 RPC 响应时发生异常: " + cause.getMessage());

        // 关闭 Channel
        ctx.close();
    }
}
