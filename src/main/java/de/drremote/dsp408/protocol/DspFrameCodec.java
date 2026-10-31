package de.drremote.dsp408.protocol;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;

/** DSP408 framing only; no connection management, device commands or JSON. */
public final class DspFrameCodec {
    public static final int DLE = 0x10;
    public static final int STX = 0x02;
    public static final int ETX = 0x03;
    public static final int MAX_FRAME_LENGTH = 3 + 255 + 5;

    private DspFrameCodec() { }

    /** Raw experiments may encode arbitrary payloads, including unknown headers. */
    public static byte[] encode(byte[] payload) {
        Objects.requireNonNull(payload, "payload");
        byte[] frame = new byte[payload.length + 5];
        frame[0] = DLE;
        frame[1] = STX;
        System.arraycopy(payload, 0, frame, 2, payload.length);
        frame[frame.length - 3] = DLE;
        frame[frame.length - 2] = ETX;
        frame[frame.length - 1] = (byte) xor(frame, frame.length - 1);
        return frame;
    }

    /** Decode an already delimited frame, retaining a potentially bad checksum. */
    public static DecodedFrame decode(byte[] frame) {
        if (frame == null || frame.length < 5
                || frame[0] != DLE || frame[1] != STX
                || frame[frame.length - 3] != DLE || frame[frame.length - 2] != ETX) {
            throw new IllegalArgumentException("Invalid DSP frame boundaries");
        }
        return new DecodedFrame(frame, Arrays.copyOfRange(frame, 2, frame.length - 3),
                xor(frame, frame.length - 1), frame[frame.length - 1] & 0xff);
    }

    public static boolean isValidFrame(byte[] frame) {
        try {
            return decode(frame).checksumValid();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** Read one standard length-prefixed payload, leaving the next frame unread. */
    public static DecodedFrame readFrame(InputStream input) throws IOException {
        Objects.requireNonNull(input, "input");
        boolean dle = false;
        while (true) {
            int b = input.read();
            if (b < 0) throw new EOFException("EOF before DSP frame start");
            if (dle && b == STX) break;
            dle = b == DLE;
        }
        byte[] header = readExactly(input, 3, "payload header");
        int remaining = header[2] & 0xff;
        byte[] frame = new byte[remaining + 8];
        frame[0] = DLE;
        frame[1] = STX;
        System.arraycopy(header, 0, frame, 2, header.length);
        byte[] bodyAndTrailer = readExactly(input, remaining + 3, "payload/trailer/checksum");
        System.arraycopy(bodyAndTrailer, 0, frame, 5, bodyAndTrailer.length);
        try {
            return decode(frame);
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid DSP frame trailer for declared payload length", e);
        }
    }

    private static byte[] readExactly(InputStream input, int length, String description) throws IOException {
        byte[] bytes = input.readNBytes(length);
        if (bytes.length != length) throw new EOFException("EOF reading DSP " + description);
        return bytes;
    }

    private static int xor(byte[] bytes, int length) {
        int result = 0;
        for (int i = 0; i < length; i++) result ^= bytes[i] & 0xff;
        return result;
    }
}
