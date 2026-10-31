package de.drremote.dsp408.protocol;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HexFormat;
import static org.junit.jupiter.api.Assertions.*;

class DspFrameCodecTest {
    private static byte[] hex(String value) { return HexFormat.of().parseHex(value); }

    @Test void encodesKnownSystemInfoRequest() {
        assertArrayEquals(hex("10020001012c10032d"), DspFrameCodec.encode(hex("0001012c")));
    }

    @Test void decodesKnownRequestAndPreservesChecksumStatus() {
        DecodedFrame result = DspFrameCodec.decode(hex("10020001012c10032d"));
        assertArrayEquals(hex("0001012c"), result.payload());
        assertTrue(result.checksumValid());
        assertEquals(0x2d, result.calculatedChecksum());
        assertEquals(0x2d, result.receivedChecksum());
    }

    @Test void completeFrameCanContainEndMarkersInItsPayload() {
        byte[] payload = hex("0100082410035566101000");
        assertArrayEquals(payload, DspFrameCodec.decode(DspFrameCodec.encode(payload)).payload());
    }

    @Test void corruptChecksumRemainsObservable() {
        byte[] wire = hex("10020001012c10032c");
        assertFalse(DspFrameCodec.decode(wire).checksumValid());
        assertFalse(DspFrameCodec.isValidFrame(wire));
        assertArrayEquals(hex("0001012c"), DspFrameCodec.decode(wire).payload());
    }

    @Test void rejectsMissingBoundaries() {
        assertThrows(IllegalArgumentException.class, () -> DspFrameCodec.decode(new byte[0]));
        assertThrows(IllegalArgumentException.class, () -> DspFrameCodec.decode(hex("00020001012c10032d")));
        assertThrows(IllegalArgumentException.class, () -> DspFrameCodec.decode(hex("10020001012c10042d")));
        assertFalse(DspFrameCodec.isValidFrame(null));
    }

    @Test void streamReaderUsesPayloadLengthRatherThanEmbeddedEndMarkers() throws Exception {
        byte[] payload = hex("0100082410035566101000");
        assertArrayEquals(payload, DspFrameCodec.readFrame(new ByteArrayInputStream(DspFrameCodec.encode(payload))).payload());
    }

    @Test void streamReaderHandlesConsecutiveFramesAndOverlappingStarts() throws Exception {
        byte[] frame = hex("10020001012c10032d");
        byte[] stream = new byte[2 + frame.length * 2];
        stream[0] = 0x55; stream[1] = 0x10;
        System.arraycopy(frame, 0, stream, 2, frame.length);
        System.arraycopy(frame, 0, stream, 2 + frame.length, frame.length);
        var input = new ByteArrayInputStream(stream);
        assertTrue(DspFrameCodec.readFrame(input).checksumValid());
        assertTrue(DspFrameCodec.readFrame(input).checksumValid());
        assertEquals(0, input.available());
    }

    @Test void streamReaderRejectsEveryTruncatedPrefix() {
        byte[] frame = hex("10020001012c10032d");
        for (int size = 0; size < frame.length; size++) {
            byte[] prefix = Arrays.copyOf(frame, size);
            assertThrows(EOFException.class, () -> DspFrameCodec.readFrame(new ByteArrayInputStream(prefix)));
        }
    }

    @Test void streamReaderRejectsInvalidLengthTrailer() {
        assertThrows(IOException.class, () -> DspFrameCodec.readFrame(new ByteArrayInputStream(hex("10020001012c10042d"))));
    }

    @Test void decodedArraysAreDefensivelyCopied() {
        byte[] wire = hex("10020001012c10032d");
        DecodedFrame decoded = DspFrameCodec.decode(wire);
        wire[2] = 99;
        decoded.payload()[0] = 88;
        decoded.raw()[0] = 77;
        assertArrayEquals(hex("0001012c"), decoded.payload());
        assertEquals(0x10, decoded.raw()[0]);
    }
}
