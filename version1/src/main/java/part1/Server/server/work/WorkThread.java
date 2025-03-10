package part1.Server.server.work;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import part1.common.message.RpcRequest;
import part1.common.message.RpcResponse;
import part1.Server.provider.ServiceProvider;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * 工作线程类，用于处理客户端的 RPC 请求。
 * 每个客户端连接都会创建一个新的 WorkThread 实例。
 */
@RequiredArgsConstructor // Lombok 注解，自动生成包含所有 final 字段的构造方法
public class WorkThread implements Runnable {

    // 客户端连接的 Socket 对象
    private final Socket clientSocket;

    // 服务提供者，用于根据接口名称获取服务实现类
    private final ServiceProvider serviceProvider;

    // 日志记录器
    private static final Logger logger = LoggerFactory.getLogger(WorkThread.class);

    /**
     * 线程执行方法，处理客户端的 RPC 请求。
     */
    @Override
    public void run() {
        try (
                // 创建输出流，用于向客户端发送数据
                ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                // 创建输入流，用于从客户端读取数据
                ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream())
        ) {
            // 读取客户端传过来的 RPC 请求
            RpcRequest rpcRequest = (RpcRequest) inputStream.readObject();

            // 调用服务方法，获取响应
            RpcResponse rpcResponse = invokeServiceMethod(rpcRequest);

            // 向客户端写入响应
            outputStream.writeObject(rpcResponse);
            outputStream.flush();
        } catch (IOException | ClassNotFoundException e) {
            logger.error("处理 RPC 请求时发生错误: {}", e.getMessage(), e);
        } finally {
            // 关闭 Socket 连接
            closeClientSocket();
        }
    }

    /**
     * 根据 RPC 请求调用相应的服务方法，并返回响应。
     *
     * @param rpcRequest 客户端的 RPC 请求
     * @return RPC 响应
     */
    private RpcResponse invokeServiceMethod(RpcRequest rpcRequest) {
        // 获取服务接口名称
        String interfaceName = rpcRequest.getInterfaceName();

        // 从服务提供者中获取服务实现类
        Object serviceInstance = serviceProvider.getService(interfaceName);

        // 获取方法对象
        try {
            Method method = serviceInstance.getClass().getMethod(rpcRequest.getMethodName(),
                    rpcRequest.getParamsType());

            // 反射调用方法，获取返回值
            Object result = method.invoke(serviceInstance, rpcRequest.getParams());

            // 返回成功响应
            return RpcResponse.success(result);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            logger.error("调用服务方法时发生错误: {}", e.getMessage(), e);

            // 返回失败响应
            return RpcResponse.fail();
        }
    }

    /**
     * 关闭客户端 Socket 连接。
     */
    private void closeClientSocket() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            logger.error("关闭客户端 Socket 时发生错误: {}", e.getMessage(), e);
        }
    }
}
