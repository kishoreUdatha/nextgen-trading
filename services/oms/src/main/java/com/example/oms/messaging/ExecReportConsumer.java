package com.example.oms.messaging;

import com.example.oms.domain.OrderEntity;
import com.example.oms.ws.OrderEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class ExecReportConsumer {

    private final OrderEventPublisher publisher;

    public ExecReportConsumer(OrderEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void handleExecReport(OrderEntity e) {
        switch (e.getStatus().toString()) {
            case "PARTIALLY_FILLED" -> publisher.publishPartiallyFilled(e.getId().toString());
            case "CANCELLED" -> publisher.publishCancelled(e.getId().toString(), "IOC_EXPIRED");
            default -> {
                // Optionally handle other statuses
            }
        }
    }
}
