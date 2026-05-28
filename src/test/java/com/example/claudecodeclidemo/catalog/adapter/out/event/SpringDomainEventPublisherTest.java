package com.example.claudecodeclidemo.catalog.adapter.out.event;

import com.example.claudecodeclidemo.catalog.domain.event.ProductArchivedEvent;
import com.example.claudecodeclidemo.shared.domain.DomainEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SpringDomainEventPublisherTest {

    @Mock ApplicationEventPublisher applicationEventPublisher;
    @InjectMocks SpringDomainEventPublisher publisher;

    @Test
    void publish_withoutTransaction_forwardsImmediately() {
        DomainEvent event = archivedEvent();

        publisher.publish(event);

        verify(applicationEventPublisher).publishEvent(event);
    }

    @Test
    void publish_withinTransaction_defersUntilAfterCommit() {
        DomainEvent event = archivedEvent();

        TransactionSynchronizationManager.initSynchronization();
        try {
            publisher.publish(event);

            // still inside transaction — must NOT have fired yet
            verifyNoInteractions(applicationEventPublisher);

            // simulate commit
            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(s -> s.afterCommit());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }

        verify(applicationEventPublisher).publishEvent(event);
    }

    @Test
    void publishAll_withoutTransaction_forwardsEveryEvent() {
        DomainEvent e1 = archivedEvent();
        DomainEvent e2 = archivedEvent();

        publisher.publishAll(List.of(e1, e2));

        verify(applicationEventPublisher).publishEvent(e1);
        verify(applicationEventPublisher).publishEvent(e2);
    }

    private static DomainEvent archivedEvent() {
        return new ProductArchivedEvent(UUID.randomUUID(), Instant.now(), UUID.randomUUID());
    }
}
