package part3.Server.provider;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import part3.Server.serviceRegister.ServiceRegister;
import part3.Server.serviceRegister.impl.ZKServiceRegister;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务提供者类，用于注册和获取服务实例。
 * 该类维护一个接口名称到服务实现的映射，并提供注册和查询方法。
 */
@RequiredArgsConstructor
public class ServiceProvider {
    private static final Logger logger = LogManager.getLogger(ServiceProvider.class); // 日志记录器

    private final String host; // 服务器主机地址
    private final int port; // 服务器端口号
    private final ServiceRegister serviceRegister; // 服务注册器

    // 存储已注册的接口与其对应的服务实现对象
    private final Map<String, Object> serviceInstanceMap;

    /**
     * 构造方法，初始化服务注册器和服务存储映射。
     *
     * @param host 服务器地址
     * @param port 服务器端口
     */
    public ServiceProvider(String host, int port) {
        this.host = host;
        this.port = port;
        this.serviceRegister = new ZKServiceRegister();
        this.serviceInstanceMap = new HashMap<>();
    }

    /**
     * 注册服务实现类。
     * 该方法会将服务类实现的所有接口与其自身关联，并存储到本地缓存中，
     * 同时将服务注册到注册中心。
     *
     * @param service 需要注册的服务对象
     */
    public void register(Object service) {
        Class<?>[] interfaces = service.getClass().getInterfaces(); // 获取服务类的所有接口

        if (interfaces.length == 0) {
            logger.warn("服务 {} 未实现任何接口，无法注册！", service.getClass().getName());
            return;
        }

        for (Class<?> interfaceClass : interfaces) {
            String interfaceName = interfaceClass.getName();
            serviceInstanceMap.put(interfaceName, service);
            serviceRegister.register(interfaceName, new InetSocketAddress(host, port));
            logger.info("服务 [{}] 已成功注册到本地缓存，并在注册中心绑定地址：{}:{}", interfaceName, host, port);
        }
    }

    /**
     * 根据接口名称获取对应的服务实例。
     *
     * @param interfaceName 接口的全限定名
     * @return 服务实现对象；如果未找到，则返回 null
     */
    public Object getRegisteredService(String interfaceName) {
        Object service = serviceInstanceMap.get(interfaceName);
        if (service == null) {
            logger.error("请求的服务 [{}] 未注册！", interfaceName);
        }
        return service;
    }
}
