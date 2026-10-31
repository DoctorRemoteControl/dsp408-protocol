# DSP408 wire protocol

Dependency-free Java 21 framing and payload metadata for DSP Raider, DSP Proxy and
the OSGi controller. This directory is an independent Maven project.

```bash
mvn verify
mvn install
```

Consumers use `de.drremote:dsp408-protocol:0.1.0-SNAPSHOT`. The JAR is also an OSGi
bundle exporting `de.drremote.dsp408.protocol` at package version `0.1.0`.

Use `DspFrameCodec.decode` for an already delimited diagnostic frame; the returned
checksum status must be checked by strict consumers. Use `readFrame` or a
per-direction `DspFrameDecoder` for standard length-prefixed stream data. Byte
arrays in decoded results are copied to preserve immutable capture evidence.

See the [workspace architecture](../ARCHITECTURE.md) for integration and migration.
