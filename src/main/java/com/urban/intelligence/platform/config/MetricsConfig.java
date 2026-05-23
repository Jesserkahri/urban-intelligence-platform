package com.urban.intelligence.platform.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * MetricsConfig - defines Micrometer counters and timed annotations for observability.
 *
 * All metrics are accessible via /actuator/metrics endpoint.
 * No external monitoring stack required.
 */
@Configuration
@EnableAspectJAutoProxy
public class MetricsConfig {

    /** Enables @Timed annotation on any Spring-managed bean (services, controllers, etc.) */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    @Bean
    public Counter authLoginSuccessCounter(MeterRegistry registry) {
        return Counter.builder("auth.login.attempts")
            .description("Total login attempts")
            .tag("outcome", "success")
            .register(registry);
    }

    @Bean
    public Counter authLoginFailureCounter(MeterRegistry registry) {
        return Counter.builder("auth.login.attempts")
            .description("Total login attempts")
            .tag("outcome", "failure")
            .register(registry);
    }
}
