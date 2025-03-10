package part1.common.message;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class RpcResponse implements Serializable {
    //HTTP状态信息
    private int code;
    private String message;
    //返回的数据对象
    private Object data;
    //构造成功信息
    public static RpcResponse success(Object data) {
        return RpcResponse.builder()
                .code(200)
                .message("请求消息成功！")
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
