package part2.common.serializer.myCode;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import part2.common.message.MessageType;
import part2.common.message.RpcRequest;
import part2.common.message.RpcResponse;
import part2.common.serializer.mySerializer.Serializer;

/**
 * 自定义 Netty 编码器，将 RpcRequest 和 RpcResponse 序列化并写入 ByteBuf。
 */
@Slf4j  // 使用 Log4j 记录日志
@RequiredArgsConstructor // 自动生成构造方法，注入 Serializer
public class MyEncoder extends MessageToByteEncoder<Object> {

    private final Serializer serializer; // 序列化器

    @Override
    protected void encode(ChannelHandlerContext ctx, Object message, ByteBuf out) {
        log.info("message所对应的类对象为：{}", message.getClass());
        try {
            // 1. 识别消息类型，并写入消息标识
            if (message instanceof RpcRequest) {
                out.writeShort(MessageType.REQUEST.getCode()); // 请求类型
            } else if (message instanceof RpcResponse) {
                out.writeShort(MessageType.RESPONSE.getCode()); // 响应类型
            } else {
                log.warn("未知的消息类型: {}", message.getClass().getSimpleName());
                return; // 直接返回，不编码
            }

            // 2. 写入序列化方式
            out.writeShort(serializer.getType());

            // 3. 序列化对象
            byte[] serializedBytes = serializer.serialize(message);

            // 4. 写入序列化数据的长度
            out.writeInt(serializedBytes.length);

            // 5. 写入序列化后的字节数据
            out.writeBytes(serializedBytes);

            log.info("成功编码消息，类型: {}，序列化方式: {}，字节大小: {}",
                    message.getClass().getSimpleName(), serializer.getType(), serializedBytes.length);

        } catch (Exception e) {
            log.error("编码消息时发生错误", e);
        }
    }
}
