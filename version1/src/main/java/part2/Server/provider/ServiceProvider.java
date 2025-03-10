package part2.Server.provider;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务提供者类，用于注册和获取服务实现。
 * 通过维护一个接口名称到服务实现的映射，支持根据接口名称获取对应的服务实例。
 */
@RequiredArgsConstructor // Lombok 注解，自动生成包含 final 字段的构造方法
public class ServiceProvider {

    // 接口名称到服务实现的映射（声明为 final，确保不可变）
    private final Map<String, Object> interfaceProvider;

    public ServiceProvider() {
        this.interfaceProvider = new HashMap<>();
    }

    /**
     * 注册服务实现。
     * 将服务实现类实现的所有接口与其自身关联，并存储到映射中。
     *
     * @param service 服务实现对象
     */
    public void registerService(Object service) {
        // 获取服务实现类实现的所有接口
        Class<?>[] interfaces = service.getClass().getInterfaces();

        // 遍历所有接口，将接口名称与服务实现对象关联
        for (Class<?> interfaceClass : interfaces) {
            interfaceProvider.put(interfaceClass.getName(), service);
        }
    }

    /**
     * 根据接口名称获取对应的服务实现。
     *
     * @param interfaceName 接口的全限定名
     * @return 服务实现对象；如果未找到，则返回 null
     */
    public Object getService(String interfaceName) {
        return interfaceProvider.get(interfaceName);
    }
}
