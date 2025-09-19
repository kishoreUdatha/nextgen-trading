package com.example.oms.utils;

public  class LogFmt {

    /** Build "k=v k2=v2 ..." safely; ignores odd last key */
    public static String kv(Object... kvs) {
        if (kvs == null || kvs.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        int pairs = (kvs.length / 2) * 2;
        for (int i = 0; i < pairs; i += 2) {
            if (i > 0) sb.append(' ');
            sb.append(kvs[i]).append('=').append(kvs[i + 1]);
        }
        return sb.toString();
    }
}
