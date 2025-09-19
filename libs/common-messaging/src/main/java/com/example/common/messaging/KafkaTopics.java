package com.example.common.messaging;

public class KafkaTopics {
    private final String p;

    public KafkaTopics(String prefix) {
        this.p = (prefix == null || prefix.isBlank()) ? "tp" : prefix;
    }

    public String ordersPlaced() {
        return p + ".orders.placed";
    }

    public String ordersValidated() {
        return p + ".orders.validated";
    }

    public String ordersRouted() {
        return p + ".orders.routed";
    }

    public String ordersUpdated() {
        return p + ".orders.updated";
    }

    public String riskApproved() {
        return p + ".risk.approved";
    }

    public String riskBlocks() {
        return p + ".risk.blocks";
    }

    public String tradesBooked() {
        return p + ".trades.booked";
    }

    public String positionsUpdated() {
        return p + ".positions.updated";
    }

    public String mdLtp() {
        return p + ".md.ltp";
    }

    public String mdPriceBands() {
        return p + ".md.pricebands";
    }

    public String execRoute() {
        return p + ".exec.route";
    }

    public String execReports() {
        return p + ".exec.reports";
    }

    public String outbox() {
        return p + ".outbox.events";
    }
}
