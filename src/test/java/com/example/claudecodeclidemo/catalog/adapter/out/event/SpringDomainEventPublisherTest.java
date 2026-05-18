package com.example.claudecodeclidemo.catalog.adapter.out.event;

import com.example.claudecodeclidemo.catalog.domain.event.ProductArchivedEvent;
import com.example.claudecodeclidemo.shared.domain.DomainEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SpringDomainEventPublisherTest {

    @Mock ApplicationEventPublisher applicationEventPublisher;
    @InjectMocks SpringDomainEventPublisher publisher;

    @Test
    void publish_forwardsEventToSpring() {
        DomainEvent event = new ProductArchivedEvent(
                UUID.randomUUID(), Instant.now(), UUID.randomUUID());

        publisher.publish(event);

        verify(applicationEventPublisher).publishEvent(event);
    }

    @Test
    void publishAll_forwardsEveryEvent() {
        DomainEvent e1 = new ProductArchivedEvent(UUID.randomUUID(), Instant.now(), UUID.randomUUID());
        DomainEvent e2 = new ProductArchivedEvent(UUID.randomUUID(), Instant.now(), UUID.randomUUID());

        publisher.publishAll(List.of(e1, e2));

        verify(applicationEventPublisher).publishEvent(e1);
        verify(applicationEventPublisher).publishEvent(e2);
    }
}
