package part2.Server.rateLimit.impl;

import part2.Server.rateLimit.RateLimit;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class TokenBucketRateLimitImpl implements RateLimit {
    private final int capacity;
    private final int refillRate;
    private int tokens;
    private final ReentrantLock lock = new ReentrantLock();

    public TokenBucketRateLimitImpl(int capacity, int refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.tokens = capacity;

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleWithFixedDelay(this::refill, 0, 1, TimeUnit.SECONDS);
    }

    public void refill() {
        lock.lock();

        try {
            tokens = Math.min(capacity, tokens + refillRate);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean tryConsume() {
        lock.lock();

        try {
            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }
}
