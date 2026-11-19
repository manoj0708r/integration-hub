package com.aspire.poc.integration_hub.messaging.publisher;

import com.aspire.poc.integration_hub.messaging.dto.ShipmentRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShipmentPublisher {

    private final JmsTemplate jmsTemplate;

    public void publish(ShipmentRequestedEvent event) {

        jmsTemplate.convertAndSend("warehouse.shipments", event);
    }

    public void publishToDlq(ShipmentRequestedEvent event) {

        jmsTemplate.convertAndSend("warehouse.dlq", event);
    }
}