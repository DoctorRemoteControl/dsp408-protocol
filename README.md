# DSP408 protocol library

[![Verify](https://github.com/DoctorRemoteControl/dsp408-protocol/actions/workflows/verify.yml/badge.svg)](https://github.com/DoctorRemoteControl/dsp408-protocol/actions/workflows/verify.yml)

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
local Maven repository. Alternatively, the [multi-project build instructions](https://github.com/DoctorRemoteControl/dsp-raider/blob/main/docs/DEVELOPMENT.md#building) builds
it in the reactor before all consumers.

Maven coordinate: `de.drremote:dsp408-protocol:0.1.0-SNAPSHOT`. The JAR is also
an OSGi bundle and exports `de.drremote.dsp408.protocol` with package version
`0.1.0`.

## Automated verification

The [GitHub Actions workflow](.github/workflows/verify.yml) prepares a
Java 21 verification build on pushes, pull requests and manual runs. It uses
pinned action revisions and a fresh temporary Maven repository without a
dependency cache. Automated tests use synthetic frames and saved data, with
no physical DSP connection.

Source repository: [DoctorRemoteControl/dsp408-protocol](https://github.com/DoctorRemoteControl/dsp408-protocol).
[GitHub Actions](https://github.com/DoctorRemoteControl/dsp408-protocol/actions/workflows/verify.yml)
records the result for each published revision. Raider and the proxy
each lock this library's source commit in `ci/protocol-revision`, check out
that exact revision and install it before their own builds. This repository
does not currently publish a Maven package. No license file has been selected.

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
[Workspace architecture](https://github.com/DoctorRemoteControl/dsp-raider/blob/main/docs/ARCHITECTURE.md).
