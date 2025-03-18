package part1.client;

import part1.client.proxy.ClientProxy;
import part1.common.pojo.User;
import part1.common.service.UserService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * RPC 客户端测试类。
 * 负责调用远程 RPC 服务，并输出调用结果。
 */
public class TestClient {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                // 创建客户端代理
                ClientProxy clientProxy = new ClientProxy();

                // 获取远程服务的代理对象
                UserService userServiceProxy = clientProxy.getProxy(UserService.class);

                try {
                    // 远程调用：根据用户 ID 查询用户信息
                    int userId = 1;
                    System.out.println("请求获取用户信息，ID：" + userId);
                    User user = userServiceProxy.getUserByUserId(userId);
                    System.out.println("从服务端获取的用户信息：" + user);

                    // 远程调用：插入新用户
                    User newUser = User.builder()
                            .id(2)  // 设置 ID
                            .userName("Boogiepop")  // 用户名
                            .sex(true)  // 性别
                            .build();

                    System.out.println("请求插入新用户：" + newUser);
                    Integer insertedUserId = userServiceProxy.insertUserId(newUser);
                    System.out.println("服务器返回的插入用户 ID：" + insertedUserId);
                } catch (Exception e) {
                    System.err.println("RPC 调用过程中发生错误：" + e.getMessage());
                }
            });
        }
        executor.shutdown();
    }
}
