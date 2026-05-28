package com.example.claudecodeclidemo.catalog.adapter.out.event;

import com.example.claudecodeclidemo.catalog.application.port.out.DomainEventPublisher;
import com.example.claudecodeclidemo.shared.domain.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringDomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(DomainEvent event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            // Defer to after commit so consumers never see uncommitted aggregate state
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    applicationEventPublisher.publishEvent(event);
                }
            });
        } else {
            applicationEventPublisher.publishEvent(event);
        }
    }
}
