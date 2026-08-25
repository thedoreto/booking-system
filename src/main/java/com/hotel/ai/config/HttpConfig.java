package com.hotel.ai.config;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @deprecated Part of the legacy AI implementation.
 *             The new AI implementation uses LangChain4j.
 */
@Deprecated
@Configuration
public class HttpConfig {
    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient();
    }
}
