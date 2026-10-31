package de.drremote.dsp408.protocol;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DspPayloadsTest {
    @Test void identifiesCommandsAndReadBlocksWithoutModelDependencies() {
        assertNull(DspPayloads.command(null));
        assertNull(DspPayloads.command(new byte[3]));
        assertEquals(0x24, DspPayloads.command(new byte[]{1,0,2,0x24,0x1f}));
        assertEquals(0x1f, DspPayloads.readBlockIndex(new byte[]{1,0,2,0x24,0x1f}));
        assertNull(DspPayloads.readBlockIndex(new byte[]{0,1,2,0x27,0x1f}));
        assertEquals("0xFF", DspPayloads.commandHex(255));
        assertEquals("null", DspPayloads.commandHex(null));
    }

    @Test void formatsWireDataDeterministically() {
        assertEquals("10 02 FF", Hex.bytes(new byte[]{0x10,2,(byte)255}));
        assertEquals("1002FF", Hex.compact(new byte[]{0x10,2,(byte)255}));
        assertEquals(".A.", Hex.ascii(new byte[]{0,65,(byte)255}));
    }

    @Test void parsesStreamHexAndKeepsCheckedErrorContract() throws Exception {
        assertArrayEquals(new byte[]{0x10,2,(byte)255},Hex.parse("10 02\nff"));
        assertArrayEquals(new byte[0],Hex.parse(null));
        assertThrows(java.io.IOException.class,() -> Hex.parse("XYZ"));
        assertThrows(java.io.IOException.class,() -> Hex.parse("A"));
        assertEquals("10 02 ...",Hex.preview(new byte[]{0x10,2,(byte)255},2));
    }
}
