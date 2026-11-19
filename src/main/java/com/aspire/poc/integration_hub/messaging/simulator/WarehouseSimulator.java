package com.aspire.poc.integration_hub.messaging.simulator;

import com.aspire.poc.integration_hub.messaging.dto.ShipmentRequestedEvent;
import com.aspire.poc.integration_hub.messaging.dto.ShipmentStatusEvent;
import com.aspire.poc.integration_hub.messaging.publisher.ShipmentPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WarehouseSimulator {

    private final JmsTemplate jmsTemplate;

    private final ShipmentPublisher shipmentPublisher;

    @JmsListener(destination = "warehouse.shipments")
    public void processShipment(ShipmentRequestedEvent event) throws InterruptedException {

        try {

            if ("CUST-WAREHOUSE-FAIL".equals(event.customerId())) {

                throw new RuntimeException("Simulated warehouse failure");
            }

            Thread.sleep(3000);

            jmsTemplate.convertAndSend("warehouse.status", new ShipmentStatusEvent(event.orderId(), "SHIPPED"));

        } catch (Exception exception) {

            System.err.println("Warehouse processing failed : " + exception.getMessage());

            shipmentPublisher.publishToDlq(event);
        }
    }
}