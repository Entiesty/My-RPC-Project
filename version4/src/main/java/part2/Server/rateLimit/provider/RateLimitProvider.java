package part2.Server.rateLimit.provider;

import part2.Server.rateLimit.RateLimit;
import part2.Server.rateLimit.impl.TokenBucketRateLimitImpl;

import java.util.HashMap;
import java.util.Map;

public class RateLimitProvider {
    private final Map<String, RateLimit> rateLimitMap = new HashMap<>();

    public RateLimit getRateLimit(String interfaceName) {
        if(!rateLimitMap.containsKey(interfaceName)) {
            RateLimit rateLimit = new TokenBucketRateLimitImpl(200, 10);

            rateLimitMap.put(interfaceName, rateLimit);

            return rateLimit;
        }
        return rateLimitMap.get(interfaceName);
    }
}
