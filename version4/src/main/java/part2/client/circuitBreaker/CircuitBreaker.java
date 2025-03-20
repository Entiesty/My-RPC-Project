package part2.client.circuitBreaker;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 熔断器实现类，用于在系统出现故障时自动切断请求，防止故障扩散。
 */
public class CircuitBreaker {

    private static final Logger logger = LogManager.getLogger(CircuitBreaker.class);

    /**
     * -- GETTER --
     *  获取当前熔断器状态。
     *
     */
    // 当前状态
    @Getter
    private CircuitBreakerState state = CircuitBreakerState.CLOSED;

    @Getter
    // 失败次数计数器
    private final AtomicInteger failureCount = new AtomicInteger(0);

    @Getter
    // 成功次数计数器
    private final AtomicInteger successCount = new AtomicInteger(0);

    @Getter
    // 请求次数计数器（用于半开状态）
    private final AtomicInteger requestCount = new AtomicInteger(0);

    // 失败次数阈值，超过该值则触发熔断
    private final int failureThreshold;

    // 半开状态下，成功率达到该比例则关闭熔断器
    private final double halfOpenSuccessRate;

    // 熔断器从开启状态切换到半开状态的恢复时间（毫秒）
    private final long retryTimePeriod;

    // 上一次失败时间
    private long lastFailureTime = 0;

    /**
     * 构造函数。
     *
     * @param failureThreshold     失败次数阈值
     * @param halfOpenSuccessRate  半开状态下的成功率阈值
     * @param retryTimePeriod      恢复时间（毫秒）
     */
    public CircuitBreaker(int failureThreshold, double halfOpenSuccessRate, long retryTimePeriod) {
        this.failureThreshold = failureThreshold;
        this.halfOpenSuccessRate = halfOpenSuccessRate;
        this.retryTimePeriod = retryTimePeriod;
    }

    /**
     * 检查当前是否允许请求通过。
     *
     * @return 如果允许请求通过，返回 true；否则返回 false
     */
    public synchronized boolean allowRequest() {
        long currentTime = System.currentTimeMillis();
        logger.info("当前熔断器状态: {}, 失败次数: {}", state, failureCount.get());

        switch (state) {
            case OPEN:
                // 如果当前时间距离上次失败时间超过恢复时间，则切换到半开状态
                if (currentTime - lastFailureTime > retryTimePeriod) {
                    state = CircuitBreakerState.HALF_OPEN;
                    resetCounts();
                    logger.info("`熔断器从 OPEN 状态切换到 HALF_OPEN 状态`");

                    return true;
                }
                logger.info("熔断器处于 OPEN 状态，拒绝请求");

                return false;
            case HALF_OPEN:
                // 半开状态下允许部分请求通过
                requestCount.incrementAndGet();
                logger.info("熔断器处于 HALF_OPEN 状态，允许请求通过");

                return true;
            case CLOSED:
            default:
                // 关闭状态下允许所有请求通过
                logger.info("熔断器处于 CLOSED 状态，允许请求通过");

                return true;
        }
    }

    /**
     * 记录请求成功。
     */
    public synchronized void recordSuccess() {
        if (state == CircuitBreakerState.HALF_OPEN) {
            successCount.incrementAndGet();
            requestCount.incrementAndGet();
            logger.info("当前成功次数: {}, 请求次数: {}, 成功率阈值: {}",
                    successCount.get(), requestCount.get(), halfOpenSuccessRate * requestCount.get());

            // 如果成功率超过阈值，则切换到关闭状态
            if (successCount.get() >= halfOpenSuccessRate * requestCount.get()) {
                state = CircuitBreakerState.CLOSED;
                resetCounts();
                logger.info("熔断器从 HALF_OPEN 状态切换到 CLOSED 状态");
            }
        } else {
            resetCounts();
        }
    }

    /**
     * 记录请求失败。
     */
    public synchronized void recordFailure() {
        failureCount.incrementAndGet();
        requestCount.incrementAndGet();
        lastFailureTime = System.currentTimeMillis();

        logger.info("记录失败，当前失败次数: {}", failureCount.get());

        if (state == CircuitBreakerState.HALF_OPEN) {
            // 半开状态下失败，则切换到开启状态
            state = CircuitBreakerState.OPEN;
            logger.info("熔断器从 HALF_OPEN 状态切换到 OPEN 状态");
        } else if (failureCount.get() >= failureThreshold) {
            // 失败次数超过阈值，则切换到开启状态
            state = CircuitBreakerState.OPEN;
            logger.info("熔断器从 CLOSED 状态切换到 OPEN 状态");
        }
    }

    /**
     * 重置计数器。
     */
    private void resetCounts() {
        failureCount.set(0);
        successCount.set(0);
        requestCount.set(0);
        logger.info("重置计数器");
    }
}