package part2.client.circuitBreaker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class CircuitBreakerProvider {
    private final Map<String, CircuitBreaker> circuitBreakerMap = new HashMap<>();
    private static final Logger logger = LogManager.getLogger(CircuitBreakerProvider.class);

    public synchronized CircuitBreaker getCircuitBreaker(String interfaceName) {
        CircuitBreaker circuitBreaker;
        if (circuitBreakerMap.containsKey(interfaceName)) {
            circuitBreaker = circuitBreakerMap.get(interfaceName);
        } else {
            logger.info("创建服务类{}的熔断器", interfaceName);
            circuitBreaker = new CircuitBreaker(2, 0.5, 210);
            circuitBreakerMap.put(interfaceName, circuitBreaker);

        }
        return circuitBreaker;
    }
}
