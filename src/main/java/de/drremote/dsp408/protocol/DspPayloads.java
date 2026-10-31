package de.drremote.dsp408.protocol;

/** Metadata from payload bytes, independent of consumer-specific frame models. */
public final class DspPayloads {
    private DspPayloads() { }

    public static Integer command(byte[] payload) {
        return payload != null && payload.length > 3 ? payload[3] & 0xff : null;
    }

    public static Integer readBlockIndex(byte[] payload) {
        Integer command = command(payload);
        return command != null && command == 0x24 && payload.length > 4 ? payload[4] & 0xff : null;
    }

    public static String commandHex(Integer command) {
        return command == null ? "null" : "0x%02X".formatted(command);
    }
}
