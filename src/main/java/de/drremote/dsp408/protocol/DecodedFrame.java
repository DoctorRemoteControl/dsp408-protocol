package de.drremote.dsp408.protocol;

/** A diagnostic frame: a bad checksum is represented, not silently discarded. */
public record DecodedFrame(byte[] raw, byte[] payload, int calculatedChecksum, int receivedChecksum) {
    public DecodedFrame {
        raw = raw.clone();
        payload = payload.clone();
    }

    @Override public byte[] raw() { return raw.clone(); }
    @Override public byte[] payload() { return payload.clone(); }
    public boolean checksumValid() { return calculatedChecksum == receivedChecksum; }
}
