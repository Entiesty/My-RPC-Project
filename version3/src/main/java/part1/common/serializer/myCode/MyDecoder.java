package part2.common.serializer.myCode;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import part2.common.message.MessageType;
import part2.common.serializer.mySerializer.Serializer;

import java.util.List;

/**
 * 自定义 Netty 解码器，将接收到的字节流解析成 RpcRequest 或 RpcResponse 对象。
 */
@Slf4j // 使用 Log4j 记录日志
public class MyDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        try {
            // 确保数据足够（short + short + int 至少 8 字节）
            if (in.readableBytes() < 8) {
                return;
            }

            // 1. 读取消息类型
            short msgType = in.readShort();
            if (msgType != MessageType.REQUEST.getCode() && msgType != MessageType.RESPONSE.getCode()) {
                log.warn("收到未知消息类型: {}", msgType);
                return;
            }

            // 2. 读取序列化方式
            short serializerCode = in.readShort();
            Serializer serializer = Serializer.getSerializerByCode(serializerCode);
            if (serializer == null) {
                log.warn("不支持的序列化类型: {}", serializerCode);
                return;
            }

            // 3. 读取数据长度
            int length = in.readInt();
            if (in.readableBytes() < length) {
                log.warn("数据长度不足，期望: {}，实际: {}", length, in.readableBytes());
                return;
            }

            // 4. 读取字节数据
            byte[] bytes = new byte[length];
            in.readBytes(bytes);

            // 5. 反序列化
            Object message = serializer.deserialize(bytes, msgType);
            out.add(message);

            log.info("成功解码消息，类型: {}，序列化方式: {}，字节大小: {}", msgType, serializerCode, length);

        } catch (Exception e) {
            log.error("解码消息时发生错误", e);
        }
    }
}
