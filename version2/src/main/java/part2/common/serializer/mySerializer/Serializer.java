package part2.common.serializer.mySerializer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 序列化接口，定义了对象的序列化和反序列化方法。
 * 具体实现类可提供不同的序列化方式（如 JSON、Java 序列化）。
 */
public interface Serializer {
    /**
     * 日志记录器，用于记录序列化相关的日志信息。
     */
    Logger logger = LogManager.getLogger(Serializer.class);

    /**
     * 将对象序列化为字节数组。
     *
     * @param obj 要序列化的对象
     * @return 序列化后的字节数组
     */
    byte[] serialize(Object obj);

    /**
     * 反序列化字节数组，转换回原始对象。
     *
     * @param bytes       要反序列化的字节数组
     * @param messageType 消息类型（用于区分不同的对象类型）
     * @return 反序列化得到的对象
     */
    Object deserialize(byte[] bytes, int messageType);

    /**
     * 获取序列化方式的类型编号。
     *
     * @return 序列化类型编号
     */
    int getType();

    /**
     * 根据序列化方式的编号获取对应的序列化器。
     *
     * @param code 序列化方式的编号
     * @return 对应的 {@link Serializer} 实例
     * @throws IllegalArgumentException 如果传入的编号无效，则抛出异常
     */
    static Serializer getSerializerByCode(int code) {
        switch (code) {
            case 0:
                return new ObjectSerializer();
            case 1:
                return new JsonSerializer();
            default:
                logger.error("无效的序列化类型编号: {}", code);
                return null;
        }
    }
}
