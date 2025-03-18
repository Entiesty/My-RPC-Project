package part1.common.serializer.mySerializer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

/**
 * 基于 Java 内置序列化机制的对象序列化器。
 * 该类实现了 Serializer 接口，提供对象的序列化和反序列化功能。
 */
public class ObjectSerializer implements Serializer {
    private static final Logger logger = LogManager.getLogger(ObjectSerializer.class); // 日志记录器

    /**
     * 将对象序列化为字节数组。
     *
     * @param objectToSerialize 需要序列化的对象
     * @return 序列化后的字节数组
     * @throws SerializationException 如果序列化过程中发生错误
     */
    @Override
    public byte[] serialize(Object objectToSerialize) {
        if (objectToSerialize == null) {
            logger.warn("尝试序列化 null 对象！");

            return new byte[0]; // 避免 NullPointerException
        }

        try (
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)
        ) {
            objectOutputStream.writeObject(objectToSerialize);
            objectOutputStream.flush();

            byte[] serializedData = byteArrayOutputStream.toByteArray();
            logger.info("对象 [{}] 序列化成功，大小: {} 字节", objectToSerialize.getClass().getName(),
                    serializedData.length);

            return serializedData;
        } catch (IOException e) {
            logger.error("对象 [{}] 序列化失败！", objectToSerialize.getClass().getName(), e);
            throw new SerializationException("对象序列化失败", e);
        }
    }

    /**
     * 将字节数组反序列化为对象。
     *
     * @param data        需要反序列化的字节数组
     * @param messageType 消息类型（未使用，但可扩展）
     * @return 反序列化后的对象
     * @throws SerializationException 如果反序列化过程中发生错误
     */
    @Override
    public Object deserialize(byte[] data, int messageType) {
        if (data == null || data.length == 0) {
            logger.warn("尝试反序列化空数据！");

            return null;
        }

        try (
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)
        ) {
            Object deserializedObject = objectInputStream.readObject();
            logger.info("对象反序列化成功，类型: [{}]", deserializedObject.getClass().getName());

            return deserializedObject;
        } catch (IOException | ClassNotFoundException e) {
            logger.error("数据反序列化失败！", e);
            throw new SerializationException("数据反序列化失败", e);
        }
    }

    /**
     * 获取序列化器的类型编号。
     *
     * @return 序列化器类型编号
     */
    @Override
    public int getType() {
        return 0; // 代表 Java 原生序列化方式
    }

    /**
     * 自定义异常类，用于处理序列化和反序列化异常。
     */
    public static class SerializationException extends RuntimeException {
        public SerializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
