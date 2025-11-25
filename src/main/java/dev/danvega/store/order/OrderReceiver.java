package dev.danvega.store.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class OrderReceiver {

    private static final Logger log = LoggerFactory.getLogger(OrderReceiver.class);
    private static final String ORDER_QUEUE = "order-queue";

    @JmsListener(destination = ORDER_QUEUE)
    public void receiveOrder(Order order) {
        log.info("Received order: {}", order);
    }

}
