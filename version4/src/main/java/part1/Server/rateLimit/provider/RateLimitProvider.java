package part1.Server.rateLimit.provider;

import part1.Server.rateLimit.RateLimit;
import part1.Server.rateLimit.impl.TokenBucketRateLimitImpl;

import java.util.HashMap;
import java.util.Map;

public class RateLimitProvider {
    private final Map<String, RateLimit> rateLimitMap = new HashMap<>();

    public RateLimit getRateLimit(String interfaceName) {
        if(!rateLimitMap.containsKey(interfaceName)) {
            RateLimit rateLimit = new TokenBucketRateLimitImpl(10, 1);

            rateLimitMap.put(interfaceName, rateLimit);

            return rateLimit;
        }
        return rateLimitMap.get(interfaceName);
    }
}
