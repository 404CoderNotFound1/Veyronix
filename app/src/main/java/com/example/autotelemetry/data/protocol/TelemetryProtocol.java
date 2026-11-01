package com.example.autotelemetry.data.protocol;

/** Shared protocol constants/utilities intentionally written in Java to demonstrate mixed Kotlin/Java. */
public final class TelemetryProtocol {
    public static final int SPEED_ID = 0x100;
    public static final int BATTERY_ID = 0x101;
    public static final int AMBIENT_TEMP_ID = 0x102;
    public static final int STATUS_ID = 0x103;

    private TelemetryProtocol() { }

    public static int unsigned(byte value) {
        return value & 0xFF;
    }

    public static int unsigned16BigEndian(byte high, byte low) {
        return (unsigned(high) << 8) | unsigned(low);
    }

    public static int signed16BigEndian(byte high, byte low) {
        return (short) unsigned16BigEndian(high, low);
    }
}
