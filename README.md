# DSP408 protocol library

Dependency-free Java 21 library for DSP Raider, the TCP proxy and the OSGi
controller. It contains framing, immutable frame data, hex helpers and payload
metadata. Connections, CLI, JSON and device-specific parameter catalogs live
in the respective consumers.

## Building

From the library directory:

```bash
mvn -B -ntp verify
mvn -B -ntp install
```

`verify` checks and packages. `install` also makes the library available in the
local Maven repository. Alternatively, the [workspace](../README.md) builds
it in the reactor before all consumers.

Maven coordinate: `de.drremote:dsp408-protocol:0.1.0-SNAPSHOT`. The JAR is also
an OSGi bundle and exports `de.drremote.dsp408.protocol` with package version
`0.1.0`.

## Choosing the right API

| API | Purpose / boundary |
| --- | --- |
| `DspFrameCodec.encode` | wrap a payload in a complete frame |
| `DspFrameCodec.decode` | read an already delimited diagnostic frame; check checksum status afterward |
| `DspFrameCodec.readFrame` | read a standard frame with a declared length field from a stream |
| `DspFrameDecoder` | process TCP fragments incrementally; one instance per direction |
| `DspPayloads` | identify commands and read-block indices from bytes |
| `Hex` | display bytes and parse hex input |

`DecodedFrame` copies its arrays. Parameter consumers must not treat an
invalid checksum status as a confirmed device value. Diagnostic loggers can
retain the faulty frame as such. A `10 03` byte sequence inside the payload
is not a premature frame terminator. The incremental decoder buffers at most
263 bytes.

Integration, Karaf feature and dependency direction:
[Workspace architecture](../ARCHITECTURE.md).
