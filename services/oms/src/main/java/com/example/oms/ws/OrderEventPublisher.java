package com.example.oms.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    public OrderEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publishPartiallyFilled(String orderId) {
        ObjectNode event = mapper.createObjectNode()
                .put("orderId", orderId)
                .put("status", "PARTIALLY_FILLED");
        messagingTemplate.convertAndSend("/topic/orders", event.toString());
    }

    public void publishCancelled(String orderId, String reason) {
        ObjectNode event = mapper.createObjectNode()
                .put("orderId", orderId)
                .put("status", "CANCELLED")
                .put("reason", reason);
        messagingTemplate.convertAndSend("/topic/orders", event.toString());
    }

    public void publishUpdate(String event) {
        messagingTemplate.convertAndSend("/topic/orders", event.toString());
    }
}
