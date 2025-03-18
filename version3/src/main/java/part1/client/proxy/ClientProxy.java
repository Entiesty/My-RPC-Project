package part1.client.proxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import part1.client.rpcClient.RpcClient;
import part1.client.rpcClient.impl.NettyRpcClient;
import part1.common.message.RpcRequest;
import part1.common.message.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * RPC 客户端代理类，用于动态代理目标接口，将方法调用封装为 RPC 请求并发送到服务端。
 * 通过 JDK 动态代理技术，客户端可以像调用本地方法一样调用远程服务。
 */
public class ClientProxy implements InvocationHandler {

    // 日志记录器
    private static final Logger logger = LogManager.getLogger(ClientProxy.class);

    // RPC 客户端实例，用于发送请求和接收响应
    private final RpcClient client;

    /**
     * 构造方法，初始化 RPC 客户端。
     */
    public ClientProxy() {
        this.client = new NettyRpcClient();
        logger.info("ClientProxy 初始化完成，RPC 客户端已启动");
    }

    /**
     * 动态代理的核心方法，当代理对象的方法被调用时，会触发此方法。
     * 该方法将方法调用封装为 RPC 请求，发送到服务端，并返回服务端的响应。
     *
     * @param proxy  代理对象
     * @param method 被调用的方法
     * @param args   方法的参数
     * @return 方法调用的返回值（从服务端响应中提取的数据）
     * @throws RuntimeException 如果 RPC 请求发送失败
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        try {
            // 构建 RPC 请求对象
            RpcRequest request = RpcRequest.builder()
                    .interfaceName(method.getDeclaringClass().getName()) // 接口名称
                    .methodName(method.getName()) // 方法名称
                    .params(args) // 方法参数
                    .paramsType(method.getParameterTypes()) // 方法参数类型
                    .build();

            logger.info("构建 RPC 请求: {}", request);

            // 通过 RPC 客户端发送请求到服务端，并接收响应
            RpcResponse response = client.sendRequest(request);

            logger.info("收到 RPC 响应: {}", response);

            // 如果响应不为空，返回响应中的数据；否则返回 null
            if (response != null) {
                return response.getData();
            }
            return null;
        } catch (Exception e) {
            // 捕获异常，记录日志并抛出运行时异常
            logger.error("RPC 请求发送失败", e);
            throw new RuntimeException("RPC 请求发送失败", e);
        }
    }

    /**
     * 创建目标接口的代理对象。
     * 通过 JDK 动态代理技术生成代理对象，代理对象会实现目标接口。
     *
     * @param clazz 目标接口的 Class 对象
     * @param <T>   目标接口的类型
     * @return 代理对象
     * @throws IllegalArgumentException 如果 clazz 不是接口
     */
    @SuppressWarnings("unchecked") // 忽略未检查的类型转换警告
    public <T> T getProxy(Class<T> clazz) {
        // 检查 clazz 是否是接口
        if (!clazz.isInterface()) {
            logger.error("提供的类 {} 不是接口", clazz.getName());
            throw new IllegalArgumentException("clazz 必须是接口");
        }

        // 使用 JDK 动态代理创建代理对象
        Object proxy = Proxy.newProxyInstance(
                clazz.getClassLoader(), // 目标接口的类加载器
                new Class[]{clazz},    // 目标接口的 Class 对象数组
                this                  // InvocationHandler 实例（即当前对象）
        );

        logger.info("成功创建代理对象: {}", clazz.getName());

        // 将代理对象强制转换为目标接口类型
        return (T) proxy;
    }
}