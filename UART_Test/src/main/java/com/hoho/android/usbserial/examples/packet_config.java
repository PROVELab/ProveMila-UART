package com.hoho.android.usbserial.examples;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
class Sensors_CFG {
    final static int HEADER_SIZE_BITS = 16;
    final static int HEADER_SIZE_BYTES = 2;
    final static int MAX_DATA_SIZE_BITS = 50;
    final static int MAX_DATA_SIZE_BYTES = 7;
    final static int CRC_SIZE_BITS = 16;
    final static int CRC_SIZE_BYTES = 2;
    final static String[] ordered_sensors = new String[]{
        "TP1", "TP2", "TP3", "TP4", "S", "S2", "S3", "S4", 
        "S5", "ZeroDamns"
    };
    final static short[] polys = new short[]{
        (short) 0xed2f, (short) 0xe92f, 
        (short) 0x8fdb, (short) 0x968b, 
        (short) 0x9eb2
    };
    public static final Map<String, SensorTuple<Integer, String>> sensor_details = Collections.unmodifiableMap(new HashMap<String, SensorTuple<Integer, String>>(){{
        // TirePressureSensor1
        put("TP1", new SensorTuple<Integer, String>(4, "TirePressureSensor1"));
        // TirePressureSensor2
        put("TP2", new SensorTuple<Integer, String>(4, "TirePressureSensor2"));
        put("TP3", new SensorTuple<Integer, String>(4, null));
        put("TP4", new SensorTuple<Integer, String>(4, null));
        put("S", new SensorTuple<Integer, String>(6, null));
        put("S2", new SensorTuple<Integer, String>(6, null));
        put("S3", new SensorTuple<Integer, String>(6, null));
        // Shynnisthebestsensor4
        put("S4", new SensorTuple<Integer, String>(6, "Shynnisthebestsensor4"));
        put("S5", new SensorTuple<Integer, String>(6, null));
        // Nobodygivesadamn?
        put("ZeroDamns", new SensorTuple<Integer, String>(4, "Nobodygivesadamn?"));
    }});
}
