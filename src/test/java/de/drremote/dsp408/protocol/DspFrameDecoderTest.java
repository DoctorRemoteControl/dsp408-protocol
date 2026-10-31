package de.drremote.dsp408.protocol;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DspFrameDecoderTest {
    private static final byte[] FRAME = HexFormat.of().parseHex("10020100082410035566101000100356");

    @Test void retainsEveryPossibleSplitIncludingStartAndChecksum() {
        byte[] wire = DspFrameCodec.encode(HexFormat.of().parseHex("0100082410035566101000"));
        for (int split = 0; split <= wire.length; split++) {
            var decoder = new DspFrameDecoder();
            List<DecodedFrame> frames = new ArrayList<>();
            frames.addAll(decoder.accept(Arrays.copyOfRange(wire, 0, split)));
            frames.addAll(decoder.accept(Arrays.copyOfRange(wire, split, wire.length)));
            assertEquals(1, frames.size(), "split=" + split);
            assertArrayEquals(wire, frames.getFirst().raw());
            assertEquals(0, decoder.pendingBytes());
        }
    }

    @Test void readsByteByByteAndBackToBackFrames() {
        byte[] wire = DspFrameCodec.encode(new byte[]{0,1,1,0x2c});
        var decoder = new DspFrameDecoder();
        List<DecodedFrame> frames = new ArrayList<>();
        for (int repeat = 0; repeat < 2; repeat++)
            for (byte b : wire) frames.addAll(decoder.accept(new byte[]{b}));
        assertEquals(2, frames.size());
        assertTrue(frames.stream().allMatch(DecodedFrame::checksumValid));
    }

    @Test void retainsInvalidChecksumForDiagnosticsAndContinues() {
        byte[] bad = DspFrameCodec.encode(new byte[]{0,1,1,0x2c});
        bad[bad.length-1] ^= 1;
        var decoder = new DspFrameDecoder();
        assertFalse(decoder.accept(bad).getFirst().checksumValid());
        assertTrue(decoder.accept(DspFrameCodec.encode(new byte[]{0,1,1,0x2c})).getFirst().checksumValid());
    }

    @Test void discardsNoiseAndKeepsSplitStartMarker() {
        var decoder = new DspFrameDecoder();
        byte[] noise = new byte[1_000_000];
        Arrays.fill(noise, (byte)0x55); noise[noise.length-1] = 0x10;
        assertTrue(decoder.accept(noise).isEmpty());
        assertEquals(1, decoder.pendingBytes());
        byte[] wire = DspFrameCodec.encode(new byte[]{0,1,1,0x2c});
        assertEquals(1, decoder.accept(Arrays.copyOfRange(wire, 1, wire.length)).size());
    }

    @Test void resynchronizesAfterInvalidTrailer() {
        var decoder = new DspFrameDecoder();
        byte[] bad = DspFrameCodec.encode(new byte[]{0,1,1,0x2c});
        bad[bad.length-2] = 4;
        assertTrue(decoder.accept(bad).isEmpty());
        assertEquals(1, decoder.accept(DspFrameCodec.encode(new byte[]{0,1,1,0x2c})).size());
    }

    @Test void acceptsMaximumDeclaredLengthWithoutGrowingUnbounded() {
        byte[] payload = new byte[258]; payload[2]=(byte)255;
        var decoder = new DspFrameDecoder();
        byte[] wire = DspFrameCodec.encode(payload);
        assertEquals(1, decoder.accept(wire).size());
        assertEquals(0, decoder.pendingBytes());
    }
}
