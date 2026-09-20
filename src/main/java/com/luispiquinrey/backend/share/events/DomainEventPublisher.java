package com.luispiquinrey.backend.share.events;

public interface DomainEventPublisher {

    void publish(Object event);
}
