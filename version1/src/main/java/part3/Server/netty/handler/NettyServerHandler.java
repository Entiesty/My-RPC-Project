package part3.Server.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import part3.Server.provider.ServiceProvider;
import part3.common.message.RpcRequest;
import part3.common.message.RpcResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Netty 服务器处理器，用于处理客户端发送的 RPC 请求。
 * 通过反射调用服务方法，并将结果封装为 RPC 响应返回给客户端。
 */
@RequiredArgsConstructor // Lombok 注解，自动生成包含 final 字段的构造方法
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    // 服务提供者，用于获取服务实现类
    private final ServiceProvider serviceProvider;

    /**
     * 当从客户端接收到 RPC 请求时，会触发此方法。
     *
     * @param channelHandlerContext ChannelHandler 上下文，用于管理 Channel 的生命周期和操作
     * @param rpcRequest            从客户端接收到的 RPC 请求
     * @throws Exception 如果处理过程中发生异常
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest)
            throws Exception {
        // 反射调用服务方法，获取响应
        RpcResponse response = invokeServiceMethod(rpcRequest);

        // 将响应写入 Channel 并刷新
        channelHandlerContext.writeAndFlush(response);

        // 关闭 Channel
        channelHandlerContext.close();
    }

    /**
     * 根据 RPC 请求调用相应的服务方法，并返回响应。
     *
     * @param request 客户端的 RPC 请求
     * @return RPC 响应
     */
    private RpcResponse invokeServiceMethod(RpcRequest request) {
        // 获取服务接口名称
        String interfaceName = request.getInterfaceName();

        // 从服务提供者中获取服务实现类
        Object service = serviceProvider.getRegisteredService(interfaceName);

        // 获取方法对象
        Method method;
        try {
            method = service.getClass().getMethod(request.getMethodName(), request.getParamsType());

            // 反射调用方法，获取返回值
            Object result = method.invoke(service, request.getParams());

            // 返回成功响应
            return RpcResponse.success(result);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            // 如果方法调用失败，返回失败响应
            return RpcResponse.fail();
        }
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
        System.err.println("服务器处理 RPC 请求时发生异常: " + cause.getMessage());

        // 关闭 Channel
        ctx.close();
    }
}