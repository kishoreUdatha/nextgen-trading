package com.example.oms.enums;

import java.util.Locale;

public enum OrderStatus {
    NEW, PARTIALLY_FILLED, FILLED, REJECTED, CANCELLED, EXPIRED,ROUTED;

    public static OrderStatus parse(String v) {
        return OrderEnums.parseEnum(OrderStatus.class, v);
    }
}