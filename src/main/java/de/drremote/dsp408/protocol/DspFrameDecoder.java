package de.drremote.dsp408.protocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** Incremental, bounded parser. One instance belongs to one traffic direction. */
public final class DspFrameDecoder {
    private final byte[] pending = new byte[DspFrameCodec.MAX_FRAME_LENGTH];
    private int size;
    private long discarded;

    public List<DecodedFrame> accept(byte[] chunk) {
        Objects.requireNonNull(chunk, "chunk");
        List<DecodedFrame> frames = new ArrayList<>();
        for (byte b : chunk) {
            pending[size++] = b;
            drain(frames);
        }
        return List.copyOf(frames);
    }

    public int pendingBytes() { return size; }
    public long discardedBytes() { return discarded; }

    private void drain(List<DecodedFrame> frames) {
        while (size > 0) {
            int start = findStart();
            if (start < 0) {
                discard(size - (pending[size - 1] == DspFrameCodec.DLE ? 1 : 0));
                return;
            }
            discard(start);
            if (size < 5) return;
            int length = (pending[4] & 0xff) + 8;
            if (size < length) return;
            if (pending[length - 3] != DspFrameCodec.DLE || pending[length - 2] != DspFrameCodec.ETX) {
                discard(1);
                continue;
            }
            frames.add(DspFrameCodec.decode(Arrays.copyOf(pending, length)));
            remove(length);
        }
    }

    private int findStart() {
        for (int i = 0; i < size - 1; i++) {
            if (pending[i] == DspFrameCodec.DLE && pending[i + 1] == DspFrameCodec.STX) return i;
        }
        return -1;
    }

    private void discard(int count) {
        discarded += count;
        remove(count);
    }

    private void remove(int count) {
        System.arraycopy(pending, count, pending, 0, size - count);
        size -= count;
    }
}
