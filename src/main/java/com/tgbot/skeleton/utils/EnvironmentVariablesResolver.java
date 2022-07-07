package com.tgbot.skeleton.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EnvironmentVariablesResolver {

    public static String getAsString(final String key) {
        final String env = System.getenv(key);
        if (env == null) {
            log.error("Env variable {} not found", key);
        }
        return env;
    }
}
