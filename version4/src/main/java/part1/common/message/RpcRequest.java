package part1.common.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest implements Serializable {
    //服务类名，因为客服端不知道服务端具体的服务类，传递的只是接口名，所以服务类名为接口名，在服务端接口名指向实现类
    private String interfaceName;
    //方法名
    private String methodName;
    //参数列表
    private Object[] params;
    //参数类型列表
    private Class<?>[] paramsType;
}
