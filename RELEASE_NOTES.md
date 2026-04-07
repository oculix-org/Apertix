# Apertix 4.10.0-0

Fork of [openpnp/opencv](https://github.com/openpnp/opencv) packaging
**OpenCV 4.10.0** with a JNA-based native loader for Java.

## Overview

Apertix is a drop-in replacement for `nu.pattern.opencv` that ships the
official OpenCV Java bindings as a single multi-platform fat JAR. It is
designed to be consumed as a standard Maven dependency with zero runtime
configuration, zero `--add-opens`, and zero manual extraction of native
libraries.

This release is the first stable multi-platform build of Apertix and covers
seven target architectures across Linux, macOS, and Windows.

## Major changes since the upstream fork

### OpenCV upgrade
- Upgraded from OpenCV 4.5.x to **OpenCV 4.10.0**
- Native libraries recompiled from scratch on each target platform using
  dedicated CI runners (no cross-compilation except for Linux ARMv7)
- Full `BUILD_FAT_JAVA_LIB` configuration with non-free modules enabled

### JNA migration
- Complete migration from the legacy `nu.pattern` class-loader reflection
  tricks to **JNA-based native loading**
- **Compatible with JDK 16 and later** without any `--add-opens` flag
- Removes all reliance on `sun.misc.Unsafe` and internal JDK APIs
- JNA version pinned to 5.14.0 for broad `glibc` compatibility

### Bug fixes from the upstream fork
- Fixed stale `libopencv_java` manifest issue on Linux and macOS that
  prevented correct native resolution across version bumps
- Fixed ARMv8 detection: `linux-arm` is now properly distinguished from
  `linux-aarch64` in the resource directory layout
- Removed deprecated `TemporaryDirectory` handling and `finalize()` usage
  (both deprecated in JDK 9+, removed in JDK 18+)

## Supported platforms

All native libraries in this release are built against **OpenCV 4.10.0**
and published as release assets:

| Platform | Architecture | Asset                                         |
| -------- | ------------ | --------------------------------------------- |
| Windows  | x86-64       | `opencv_java4100-win-x64.dll` (51 MB)         |
| Windows  | x86-32       | `opencv_java4100-win-x86.dll` (34 MB)         |
| Linux    | x86-64       | `libopencv_java4100-linux-x86-64.so` (66 MB)  |
| Linux    | ARM64        | `libopencv_java4100-linux-aarch64.so` (30 MB) |
| Linux    | ARMv7        | `libopencv_java4100-linux-arm.so` (21 MB)     |
| macOS    | Intel x86-64 | `libopencv_java4100-darwin.dylib` (23 MB)     |
| macOS    | Apple Silicon| `libopencv_java4100-darwin-aarch64.dylib` (24 MB) |

All libraries are also bundled into the multi-platform fat JAR
`opencv-4.10.0-0.jar` (174 MB), which is the recommended way to consume
Apertix as a Maven dependency.

## Maven coordinates
```xml
<dependency>
    <groupId>io.github.julienmerconsulting.apertix</groupId>
    <artifactId>opencv</artifactId>
    <version>4.10.0-0</version>
</dependency>
```

## Building from source

The full manual build procedure for each target platform is documented in
[BUILDING.md](BUILDING.md), including a minimal cmake recipe contributed by
[@RaiMan](https://github.com/RaiMan) for Apple Silicon.

## Base project

Apertix is a fork of [openpnp/opencv](https://github.com/openpnp/opencv) by
Jason von Nieda, released under the BSD License. We are grateful to the
upstream project for the original directory layout, native loading
strategy, and the per-platform CI approach that Apertix builds on.

## Release assets

Download individual native libraries or the full fat JAR from the
[GitHub release page](https://github.com/julienmerconsulting/Apertix/releases/tag/v4.10.0-0).
