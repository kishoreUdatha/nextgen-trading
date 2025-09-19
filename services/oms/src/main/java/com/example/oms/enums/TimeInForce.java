package com.example.oms.enums;

public enum TimeInForce {
    DAY, GTC, IOC, FOK; // adjust to your domain

    public static TimeInForce parse(String v) {
        return OrderEnums.parseEnum(TimeInForce.class, v);
    }
}