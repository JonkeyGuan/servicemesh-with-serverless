package com.demo.admin;

import java.time.Duration;

import org.reactivestreams.Publisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.services.InstanceRegistry;

@Configuration
public class DeregisterOfflineConfiguration {

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public DeregisterOfflineTrigger deregisterOfflineTrigger(InstanceRegistry registry,
            Publisher<InstanceEvent> events) {
        DeregisterOfflineTrigger trigger = new DeregisterOfflineTrigger(events, registry);
        trigger.setInterval(Duration.ofMinutes(1));
        trigger.setLifetime(Duration.ofMinutes(1));
        return trigger;
    }
}
