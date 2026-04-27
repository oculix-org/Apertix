# Apertix 4.10.0-3

## New features

- **Dual Linux build: modern + legacy glibc.** Each Linux native is now
  shipped twice in the JAR — the existing modern build at the previous
  resource paths, plus a new legacy build compiled inside
  `quay.io/pypa/manylinux_2_28_*` against glibc 2.28. The legacy native
  works on RHEL/Rocky/Alma 8 and 9, Ubuntu 22.04, Debian 12 and any newer
  distro, fixing the `GLIBC_2.38 not found` `UnsatisfiedLinkError` users
  hit on those systems with 4.10.0-2.

  ```
  linux-x86-64/libopencv_java4100.so          (modern, unchanged)
  linux-x86-64-legacy/libopencv_java4100.so   (NEW, glibc <= 2.28)
  linux-aarch64/libopencv_java4100.so         (modern, unchanged)
  linux-aarch64-legacy/libopencv_java4100.so  (NEW, glibc <= 2.28)
  ```

  Both natives are built from the same OpenCV 4.10.0 sources with the
  same CMake flags; only the toolchain differs.

  CI fails the build if the legacy native references any GLIBC symbol
  newer than 2.28 (`objdump -T | grep GLIBC | sort -V | tail -1`).

## Compatibility

- **Strictly backwards compatible.** No Java class changed. The default
  `nu.pattern.OpenCV.loadLocally()` keeps loading the modern native, so
  existing consumers are unaffected. The new `*-legacy/` paths are an
  opt-in addition: consumers running on glibc < 2.38 detect their glibc
  version and load `linux-x86-64-legacy/libopencv_java4100.so` (or the
  aarch64 equivalent) via `ClassLoader.getResourceAsStream`.

- macOS, Windows and Linux ARMv7 builds are untouched (single tier).

## Size impact

- ~140 MB added to the JAR (two extra ~66 MB Linux natives). One-time
  download per consumer, cached locally by Maven.

---

# Apertix 4.10.0-2

## Bug fixes

- **Fix: disable OpenCV `obsensor` module (broken Orbbec linkage on macOS).**
  OpenCV 4.10.0 introduced the `obsensor` module (Orbbec depth camera
  support) which is enabled by default in the build. On macOS, the
  resulting `libopencv_java4100.dylib` is linked against
  `@loader_path/libOrbbecSDK.1.9.dylib`, a library that is **not** bundled
  in the Apertix JAR. The first OpenCV call (`new Mat()`, `Imgproc.*`,
  `nu.pattern.OpenCV.loadLocally()`, etc.) therefore failed systematically
  with:
  ```
  UnsatisfiedLinkError: dlopen(libopencv_java4100.dylib, 0x0001):
    Library not loaded: @loader_path/libOrbbecSDK.1.9.dylib
  ```
  All CMake invocations in the CI workflows now pass
  `-D WITH_OBSENSOR=OFF -D BUILD_opencv_obsensor=OFF`, which removes the
  dependency cleanly instead of shipping the Orbbec SDK.

  Linux and Windows natives of 4.10.0-1 were already clean of any Orbbec
  reference, so this release is functionally identical to 4.10.0-1 on
  those platforms.

---

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
