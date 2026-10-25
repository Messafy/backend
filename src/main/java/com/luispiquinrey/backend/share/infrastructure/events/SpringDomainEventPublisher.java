package com.luispiquinrey.backend.share.infrastructure.events;

import com.luispiquinrey.backend.share.events.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SpringDomainEventPublisher.class);

    private final ApplicationEventPublisher events;

    public SpringDomainEventPublisher(ApplicationEventPublisher events) {
        this.events = events;
    }

    @Override
    public void publish(Object event) {
        log.debug("Publishing domain event {}", event.getClass().getSimpleName());
        events.publishEvent(event);
    }
}
