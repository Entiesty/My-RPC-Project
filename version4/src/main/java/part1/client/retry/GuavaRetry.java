package part1.client.retry;

import com.github.rholder.retry.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import part1.client.rpcClient.RpcClient;
import part1.common.message.RpcRequest;
import part1.common.message.RpcResponse;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 使用 Guava Retry 实现带重试机制的 RPC 调用。
 */
public class GuavaRetry {

    private static final Logger logger = LogManager.getLogger(GuavaRetry.class);

    /**
     * 发送 RPC 请求，并在失败时进行重试。
     *
     * @param request RPC 请求对象
     * @param client  RPC 客户端
     * @return RPC 响应对象
     * @throws RuntimeException 如果重试次数达到上限或发生其他异常
     */
    public RpcResponse sendRequestWithRetry(RpcRequest request, RpcClient client) {
        // 创建重试器
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                // 无论出现什么异常，都进行重试
                .retryIfException()
                // 返回结果为 error 时进行重试
                .retryIfResult(response -> response != null && response.getCode() == 500)
                // 重试等待策略：等待 2s 后再进行重试
                .withWaitStrategy(WaitStrategies.fixedWait(2, TimeUnit.SECONDS))
                // 重试停止策略：重试达到 3 次
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                .build();

        try {
            // 执行带重试的 RPC 调用
            return retryer.call(() -> {
                logger.info("正在发送 RPC 请求...");
                return client.sendRequest(request);
            });
        } catch (ExecutionException | RetryException e) {
            // 处理重试失败的情况
            if (e instanceof RetryException) {
                RetryException retryException = (RetryException) e;
                logger.error("RPC 请求重试失败，尝试次数：{}", retryException.getNumberOfFailedAttempts());

                // 获取最后一次失败的尝试
                Attempt<?> lastAttempt = retryException.getLastFailedAttempt();
                if (lastAttempt.hasException()) {
                    logger.error("最后一次失败的异常信息：", lastAttempt.getExceptionCause());
                }
            } else {
                logger.error("RPC 请求执行失败：", e);
            }

            // 抛出运行时异常
            throw new RuntimeException("RPC 请求失败", e);
        }
    }
}