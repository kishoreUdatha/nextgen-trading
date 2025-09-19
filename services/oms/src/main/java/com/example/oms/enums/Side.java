package com.example.oms.enums;

public enum Side {
    BUY, SELL;

    public static Side parse(String v) {
        return OrderEnums.parseEnum(Side.class, v);
    }
}