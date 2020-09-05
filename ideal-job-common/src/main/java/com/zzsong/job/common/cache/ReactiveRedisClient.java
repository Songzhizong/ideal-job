package com.zzsong.job.common.cache;

import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * @author 宋志宗
 * @date 2020/9/5
 */
public class ReactiveRedisClient implements ReactiveCache {
    private static final byte[] SCRIPT
            = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end"
            .getBytes(StandardCharsets.UTF_8);
    @Nonnull
    private final ReactiveStringRedisTemplate template;

    public ReactiveRedisClient(@Nonnull ReactiveStringRedisTemplate template) {
        this.template = template;
    }

    @Override
    public Mono<Boolean> expire(@Nonnull String key,
                                @Nonnull Duration expire) {
        return template.expire(key, expire);
    }

    @Override
    public Mono<String> get(@Nonnull String key) {
        return template.opsForValue().get(key);
    }

    @Override
    public Mono<Boolean> set(@Nonnull String key,
                             @Nonnull String value,
                             @Nullable Duration expire) {
        if (expire == null) {
            return template.opsForValue().set(key, value);
        } else {
            return template.opsForValue().set(key, value, expire);
        }
    }

    @Override
    public Mono<Boolean> setIfAbsent(@Nonnull String key,
                                     @Nonnull String value,
                                     @Nullable Duration expire) {
        if (expire == null) {
            return template.opsForValue().setIfAbsent(key, value);
        } else {
            return template.opsForValue().setIfAbsent(key, value, expire);
        }
    }

    @Override
    public Mono<Boolean> setIfPresent(@Nonnull String key,
                                      @Nonnull String value,
                                      @Nullable Duration expire) {
        if (expire == null) {
            return template.opsForValue().setIfPresent(key, value);
        } else {
            return template.opsForValue().setIfPresent(key, value, expire);
        }
    }

    @Override
    public Mono<Long> increment(@Nonnull String key, long delta) {
        return template.opsForValue().increment(key, delta);
    }

    @Override
    public Mono<Long> decrement(@Nonnull String key, long delta) {
        return template.opsForValue().decrement(key, delta);
    }

    @Override
    public Mono<Boolean> delete(@Nonnull String key) {
        return template.opsForValue().delete(key);
    }

    @Override
    public Mono<Boolean> delete(@Nonnull String key, @Nonnull String value) {
        ReactiveRedisConnection connection = template
                .getConnectionFactory().getReactiveConnection();
        return connection.scriptingCommands()
                .<Long>eval(
                        ByteBuffer.wrap(SCRIPT),
                        ReturnType.INTEGER, 1,
                        ByteBuffer.wrap(key.getBytes(StandardCharsets.UTF_8)),
                        ByteBuffer.wrap(value.getBytes(StandardCharsets.UTF_8))
                )
                .collectList()
                .map(list -> list.get(0) != null && list.get(0) != 0);
    }
}
