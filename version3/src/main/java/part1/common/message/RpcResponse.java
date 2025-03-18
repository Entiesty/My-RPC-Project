package part2.common.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcResponse implements Serializable {
    //HTTP状态信息
    private int code;
    private String message;
    private Class<?> dataType;
    //返回的数据对象
    private Object data;
    //构造成功信息
    public static RpcResponse success(Object data) {
        return RpcResponse.builder()
                .code(200)
                .message("请求消息成功！")
                .dataType(data.getClass())
                .data(data)
                .build();
    }
    //构造失败信息
    public static RpcResponse fail() {
        return RpcResponse.builder()
                .code(500)
                .message("服务器内部发生错误")
                .build();
    }
}
