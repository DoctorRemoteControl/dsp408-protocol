package de.drremote.dsp408.protocol;

import java.util.HexFormat;
import java.io.IOException;
import java.util.Arrays;

/** Deterministic wire formatting shared by model and transport adapters. */
public final class Hex {
    private Hex() { }

    public static String bytes(byte[] data) {
        return HexFormat.ofDelimiter(" ").withUpperCase().formatHex(data);
    }

    public static String compact(byte[] data) {
        return HexFormat.of().withUpperCase().formatHex(data);
    }

    public static String ascii(byte[] data) {
        StringBuilder result = new StringBuilder(data.length);
        for (byte b : data) {
            int value = b & 0xff;
            result.append(value >= 32 && value <= 126 ? (char) value : '.');
        }
        return result.toString();
    }

    public static String preview(byte[] data, int maxBytes) {
        if (data == null || data.length == 0) return "";
        int length = Math.min(data.length, Math.max(0, maxBytes));
        return bytes(Arrays.copyOf(data, length)) + (length < data.length ? " ..." : "");
    }

    public static byte[] parse(String value) throws IOException {
        if (value == null) return new byte[0];
        try {
            return HexFormat.of().parseHex(value.replaceAll("\\s+", ""));
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid hex: " + value, e);
        }
    }
}
