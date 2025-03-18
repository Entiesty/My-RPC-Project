package part2.common.serializer.mySerializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import part2.common.message.RpcRequest;
import part2.common.message.RpcResponse;

/**
 * JSON 序列化器，基于 FastJSON 实现序列化与反序列化。
 * 该类实现 Serializer 接口，可用于 JSON 格式的数据转换。
 */
public class JsonSerializer implements Serializer {
    private static final Logger logger = LogManager.getLogger(JsonSerializer.class); // 日志记录器

    /**
     * 将对象序列化为 JSON 格式的字节数组。
     *
     * @param objectToSerialize 需要序列化的对象
     * @return JSON 序列化后的字节数组
     * @throws SerializationException 如果序列化过程中发生错误
     */
    @Override
    public byte[] serialize(Object objectToSerialize) {
        if (objectToSerialize == null) {
            logger.warn("尝试序列化 null 对象！");

            return new byte[0];
        }

        try {
            byte[] serializedData = JSONObject.toJSONBytes(objectToSerialize);
            logger.info("对象 [{}] 序列化成功，大小: {} 字节", objectToSerialize.getClass().getName(),
                    serializedData.length);

            return serializedData;
        } catch (Exception e) {
            logger.error("对象 [{}] 序列化失败！", objectToSerialize.getClass().getName(), e);
            throw new SerializationException("对象序列化失败", e);
        }
    }

    /**
     * 将 JSON 字节数组反序列化为对象。
     *
     * @param data        需要反序列化的字节数组
     * @param messageType 消息类型（0: RpcRequest, 1: RpcResponse）
     * @return 反序列化后的对象
     * @throws SerializationException 如果反序列化过程中发生错误
     */
    @Override
    public Object deserialize(byte[] data, int messageType) {
        if (data == null || data.length == 0) {
            logger.warn("尝试反序列化空数据！");

            return null;
        }

        try {
            Object deserializedObject;
            switch (messageType) {
                case 0:
                    deserializedObject = processRpcRequest(data);
                    break;
                case 1:
                    deserializedObject = processRpcResponse(data);
                    break;
                default:
                    logger.warn("不支持的消息类型: {}", messageType);
                    throw new SerializationException("不支持的消息类型: " + messageType);
            }
            logger.info("对象反序列化成功，类型: [{}]", deserializedObject.getClass().getName());

            return deserializedObject;
        } catch (Exception e) {
            logger.error("数据反序列化失败！", e);
            throw new SerializationException("数据反序列化失败", e);
        }
    }

    /**
     * 处理 RpcRequest 的反序列化，并修正参数类型。
     *
     * @param data JSON 格式的字节数组
     * @return 反序列化后的 RpcRequest 对象
     */
    private RpcRequest processRpcRequest(byte[] data) {
        RpcRequest request = JSON.parseObject(data, RpcRequest.class);
        Object[] params = request.getParams();
        Class<?>[] paramTypes = request.getParamsType();

        if (params != null && paramTypes != null && params.length == paramTypes.length) {
            for (int i = 0; i < params.length; i++) {
                if (!paramTypes[i].isAssignableFrom(params[i].getClass())) {
                    params[i] = JSONObject.toJavaObject((JSONObject) params[i], paramTypes[i]);
                }
            }
            request.setParams(params);
        }

        return request;
    }

    /**
     * 处理 RpcResponse 的反序列化，并修正返回数据类型。
     *
     * @param data JSON 格式的字节数组
     * @return 反序列化后的 RpcResponse 对象
     */
    private RpcResponse processRpcResponse(byte[] data) {
        RpcResponse response = JSON.parseObject(data, RpcResponse.class);
        Object responseData = response.getData();
        Class<?> dataType = response.getDataType();

        if (responseData != null && dataType != null && !dataType.isAssignableFrom(responseData.getClass())) {
            response.setData(JSONObject.toJavaObject((JSONObject) responseData, dataType));
        }

        return response;
    }

    /**
     * 获取序列化器的类型编号。
     *
     * @return 1，表示 JSON 序列化方式
     */
    @Override
    public int getType() {
        return 1;
    }

    /**
     * 自定义异常类，用于处理序列化和反序列化异常。
     */
    public static class SerializationException extends RuntimeException {
        public SerializationException(String message) {
            super(message);
        }

        public SerializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
