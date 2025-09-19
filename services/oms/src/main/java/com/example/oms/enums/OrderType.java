package com.example.oms.enums;

public enum OrderType {
    MARKET, LIMIT, SL, SLM; // adjust to your domain

    public static OrderType parse(String v) {
        return OrderEnums.parseEnum(OrderType.class, v);
    }
}