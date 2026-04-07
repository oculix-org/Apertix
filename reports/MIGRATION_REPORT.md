# JNA Migration Report

**Branch:** `claude/complete-jna-migration-Es1w8`
**Base:** `claude/audit-jna-loader-Es1w8` (commit f59f4e6)
**Date:** 2026-03-22

---

## Summary

Completed the JNA migration in `OpenCV.java`. The native loading mechanism
now uses JNA's `NativeLibrary.getInstance()` instead of the old approach
that relied on `ClassLoader.usr_paths` reflection (broken on JDK 16+)
and a manual `extractNativeBinary()` with hardcoded resource paths.

The public API (`loadShared()` / `loadLocally()`) is unchanged.

---

## Files Modified

| File | Change |
|------|--------|
| `src/main/java/nu/pattern/OpenCV.java` | Rewrote loaders to use JNA `NativeLibrary.getInstance()`. Removed `extractNativeBinary()`, `addLibraryPath()`, `removeLibraryPath()`, `finalize()`, `TemporaryDirectory` class. Added `cleanupOldTempDirs()`, `deleteRecursively()`. Fixed `Arch` enum. |
| `pom.xml` | Re-added `LoadLibraryRunListener` to surefire config |
| `src/test/java/nu/pattern/LibraryLoadingTest.java` | Restored — tests `OpenCV.loadLocally()` spurious call safety |
| `src/test/java/nu/pattern/LoadLibraryRunListener.java` | Restored — bootstraps `OpenCV.loadShared()` before tests |
| `src/test/java/nu/pattern/MserTest.java` | Fixed to use `OpenCV.loadLocally()`, removed raw JNA calls and debug properties |
| `src/main/resources/linux-x86-64/` | **New** — JNA resource dir with `libopencv_java430.so` |
| `src/main/resources/win32-x86-64/` | **New** — JNA resource dir with `opencv_java430.dll` |
| `src/main/resources/win32-x86/` | **New** — JNA resource dir with `opencv_java430.dll` |
| `src/main/resources/darwin/libopencv_java342.dylib` | **Deleted** — stale 3.4.2 binary |
| `src/main/resources/nu/pattern/opencv/` | **Deleted** — entire old resource tree (replaced by JNA dirs) |

---

## What Was Removed

- **`ClassLoader.usr_paths` reflection** (`addLibraryPath` / `removeLibraryPath`) — the root cause of JDK 16+ breakage
- **`finalize()` override** — deprecated since Java 9, no longer needed since we no longer modify `usr_paths`
- **`extractNativeBinary()`** + its 60-line platform switch — replaced by JNA's automatic platform detection and extraction
- **`TemporaryDirectory` inner class** — JNA manages its own temp extraction; added `cleanupOldTempDirs()` for backward-compat cleanup of pre-JNA temp dirs
- **Old `nu/pattern/opencv/` resource tree** — dead code after migration, replaced by JNA-standard directories

## What Was Fixed

- **Architecture detection:** `ARMv8` now matches `"aarch64"` (was incorrectly `"arm"`). Added separate `ARMv7` enum matching `"arm"`.
- **NPE risk:** `File.listFiles()` null check added in `cleanupOldTempDirs()`
- **Stale binary:** Removed `darwin/libopencv_java342.dylib` (wrong OpenCV version)
- **Test coverage:** Restored `LibraryLoadingTest`, `LoadLibraryRunListener`, fixed `MserTest`
- **JNA debug noise:** Removed `jna.debug_load` system properties from `MserTest`

---

## JNA Resource Layout

JNA uses `Platform.RESOURCE_PREFIX` to locate native libraries on the classpath:

| Directory | Binary | Platform |
|-----------|--------|----------|
| `darwin/` | `libopencv_java430.dylib` | macOS x86_64 |
| `linux-x86-64/` | `libopencv_java430.so` | Linux x86_64 |
| `win32-x86-64/` | `opencv_java430.dll` | Windows x86_64 |
| `win32-x86/` | `opencv_java430.dll` | Windows x86_32 |

---

## Test Results

Compiled and tested on **Java 21** — no `--add-opens` flags needed.

| Test | Result |
|------|--------|
| `LibraryLoadingTest.spuriousLoads()` | **PASS** |
| `MserTest.testMser()` | **PASS** |

> **Note:** `mvn test` could not run because Maven Central was unreachable
> from the build environment (proxy restrictions). Tests were validated using
> `javac` + `java` directly with the same classpath. The first `mvn test`
> in a real environment should be run to confirm end-to-end.

---

## Remaining Issues

1. **No ARM64 binary** — There is no `linux-aarch64/libopencv_java430.so` in the repo. The `Arch.ARMv8` enum now correctly detects `"aarch64"`, but JNA will fail to find a binary for that platform until one is built and added.

2. **OSGi `Import-Package`** still has `!sun.reflect.*` — harmless but vestigial since the reflection code was removed. Can be cleaned up in a follow-up.

3. **OculiX compatibility** — The public API is unchanged, so existing callers of `OpenCV.loadShared()` / `OpenCV.loadLocally()` will continue to work. However, OculiX's own `RunTime.java` uses the same `ClassLoader.usr_paths` reflection pattern, which will also break on JDK 16+ independently.
