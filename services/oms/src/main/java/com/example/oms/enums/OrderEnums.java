package com.example.oms.enums;

import java.util.Locale;

final class OrderEnums {
    private OrderEnums(){}

    static <E extends Enum<E>> E parseEnum(Class<E> type, String raw) {
        if (raw == null) throw new IllegalArgumentException("enum value is null for " + type.getSimpleName());
        String norm = raw.trim()
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase(Locale.ROOT);
        return Enum.valueOf(type, norm);
    }
}
