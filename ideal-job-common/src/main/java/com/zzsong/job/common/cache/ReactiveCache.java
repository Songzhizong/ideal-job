package com.zzsong.job.common.cache;

import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;

/**
 * @author 宋志宗
 * @date 2020/9/5
 */
public interface ReactiveCache {
    Mono<Boolean> expire(@Nonnull String key, @Nonnull Duration expire);

    Mono<String> get(@Nonnull String key);

    default Mono<Boolean> set(@Nonnull String key, @Nonnull String value) {
        return set(key, value, null);
    }

    Mono<Boolean> set(@Nonnull String key, @Nonnull String value, @Nullable Duration expire);

    default Mono<Boolean> setIfAbsent(@Nonnull String key, @Nonnull String value) {
        return setIfAbsent(key, value, null);
    }

    Mono<Boolean> setIfAbsent(@Nonnull String key, @Nonnull String value, @Nullable Duration expire);

    default Mono<Boolean> setIfPresent(@Nonnull String key, @Nonnull String value) {
        return setIfPresent(key, value, null);
    }

    Mono<Boolean> setIfPresent(@Nonnull String key, @Nonnull String value, @Nullable Duration expire);

    default Mono<Long> increment(@Nonnull String key) {
        return increment(key, 1L);
    }

    Mono<Long> increment(@Nonnull String key, long delta);

    default Mono<Long> decrement(@Nonnull String key) {
        return decrement(key, 1L);
    }

    Mono<Long> decrement(@Nonnull String key, long delta);

    Mono<Boolean> delete(@Nonnull String key);

    Mono<Boolean> delete(@Nonnull String key, @Nonnull String value);
}
