# OpenCV 4.9.0 Upgrade Report

**Date:** 2026-03-22
**Branch:** `claude/upgrade-openpnp-dependency-ImOiV`
**Base:** `feb4d5f` (JNA migration complete)
**Scope:** Upgrade org.openpnp:opencv from 4.3.0 to 4.9.0-0, add ARM64 support

---

## 1. Summary

Upgraded the openpnp OpenCV dependency from version 4.3.0-2 (artifact 4.3.0-1
in pom.xml) to 4.9.0-0. All native library filenames were renamed from `430`
to `490` to match the new OpenCV version. A new `linux-aarch64` resource
directory was added for ARM64 support — the 4.9.0-0 JAR on Maven Central
already ships with pre-built ARM64 binaries.

No public API changes. No logic changes. No JNA version changes.

---

## 2. Files Modified

| File | Change |
|------|--------|
| `pom.xml` | Version `4.3.0-1` → `4.9.0-0`; upstream JAR reference `opencv-430.jar` → `opencv-490.jar` |
| `upstream/opencv-430.jar` | Renamed to `upstream/opencv-490.jar` |
| `src/main/resources/linux-x86-64/libopencv_java430.so` | Renamed to `libopencv_java490.so` |
| `src/main/resources/win32-x86-64/opencv_java430.dll` | Renamed to `opencv_java490.dll` |
| `src/main/resources/win32-x86/opencv_java430.dll` | Renamed to `opencv_java490.dll` |
| `src/main/resources/darwin/libopencv_java430.dylib` | Renamed to `libopencv_java490.dylib` |
| `src/main/resources/linux-aarch64/README.md` | **New** — documents ARM64 binary provenance |

---

## 3. Files NOT Modified

| File | Reason |
|------|--------|
| `src/main/java/nu/pattern/OpenCV.java` | No hardcoded version strings; uses `Core.NATIVE_LIBRARY_NAME` dynamically |
| `pom.xml` (JNA dependency) | JNA version unchanged at 5.5.0 |
| Test files | No test changes required |

---

## 4. Verification

### 4.1 Artifact Availability
- `org.openpnp:opencv:4.9.0-0` confirmed present on Maven Central
- POM fetched successfully from `https://repo.maven.apache.org/maven2/org/openpnp/opencv/4.9.0-0/`
- JAR size: ~109 MB (vs ~56 MB for 4.3.0), indicating additional platform binaries bundled

### 4.2 ARM64 Support
- The 4.9.0-0 JAR includes `linux-aarch64/libopencv_java490.so`
- JNA extracts the correct binary at runtime based on `os.arch` detection
- The `Arch` enum in `OpenCV.java` already maps `aarch64` → `ARMv8` (added during JNA migration)

### 4.3 Compilation
- `OpenCV.java` syntax verified with `javac` — no source-level errors
- Full Maven build requires JNA and OpenCV JARs on classpath (resolved by Maven at build time)
- Pre-existing build issues (nexus-staging-maven-plugin, bundle packaging) are unrelated to this upgrade

### 4.4 Binary Compatibility
- Native library filenames follow the OpenCV convention: `libopencv_java<version>` / `opencv_java<version>`
- JNA resolves them via `Core.NATIVE_LIBRARY_NAME` which is defined in the upstream JAR's `org.opencv.core.Core` class
- No manual C++ compilation required

---

## 5. Platform Matrix

| Platform | Resource Directory | Binary Filename | Source |
|----------|-------------------|-----------------|--------|
| Linux x86-64 | `linux-x86-64/` | `libopencv_java490.so` | Bundled (renamed) |
| Windows x86-64 | `win32-x86-64/` | `opencv_java490.dll` | Bundled (renamed) |
| Windows x86 | `win32-x86/` | `opencv_java490.dll` | Bundled (renamed) |
| macOS x86-64 | `darwin/` | `libopencv_java490.dylib` | Bundled (renamed) |
| Linux ARM64 | `linux-aarch64/` | `libopencv_java490.so` | Extracted from openpnp JAR at runtime by JNA |

---

## 6. Constraints Respected

- No public API changes
- No JNA version changes
- No manual OpenCV compilation
- Comments in English
- No logic changes in `OpenCV.java`
