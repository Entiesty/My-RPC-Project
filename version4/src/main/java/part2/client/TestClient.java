package part2.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import part2.client.proxy.ClientProxy;
import part2.common.pojo.User;
import part2.common.service.UserService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RPC 客户端测试类。
 * 负责调用远程 RPC 服务，并输出调用结果。
 */
public class TestClient {
    private static final Logger logger = LogManager.getLogger(TestClient.class);

    public static void main(String[] args) {
        logger.info("任务开始时间: {}", System.currentTimeMillis());
        AtomicInteger j = new AtomicInteger(1);

        // 线程池
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // 创建客户端代理
        ClientProxy clientProxy = new ClientProxy();
        UserService userServiceProxy = clientProxy.getProxy(UserService.class);

        for (int i = 0; i < 100; i++) {
            int finalI = i + 1;
            executor.submit(() -> {
                Thread.currentThread().setName("Thread" + finalI);
                logger.info("当前线程名为{}", Thread.currentThread().getName());
                long startTime = System.nanoTime(); // 记录开始时间

                if (finalI % 20 == 0) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                try {
                    // 远程调用：根据用户 ID 查询用户信息
                    int userId = 1;
                    System.out.println("请求获取用户信息，ID：" + userId);
                    User user = userServiceProxy.getUserByUserId(userId);
                    System.out.println("从服务端获取的用户信息：" + user);

                    // 远程调用：插入新用户
                    User newUser = User.builder()
                            .id(2)
                            .userName("Boogiepop")
                            .sex(true)
                            .build();

                    System.out.println("请求插入新用户：" + newUser);
                    Integer insertedUserId = userServiceProxy.insertUserId(newUser);
                    System.out.println("服务器返回的插入用户 ID：" + insertedUserId);
                } catch (Exception e) {
                    System.err.println("RPC 调用过程中发生错误：" + e.getMessage());
                }

                long endTime = System.nanoTime(); // 记录结束时间
                long durationMs = (endTime - startTime) / 1_000_000; // 转换为毫秒

                logger.info("第{}个任务执行耗时: {} ms", j.getAndIncrement(), durationMs);
            });
        }

        executor.shutdown();
    }
}
