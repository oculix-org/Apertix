# Apertix JNA Loader Audit Report

**Date:** 2026-03-22
**Branch audited:** `feature/convert-to-jna-loader` (commit 077ed58)
**Scope:** Audit of JNA loader conversion, compatibility with OculiX (SikuliX RunTime.java)

---

## 1. Executive Summary

The `feature/convert-to-jna-loader` branch is an **incomplete, work-in-progress** migration from the custom `nu.pattern.OpenCV` native loader to JNA's `NativeLibrary` for loading OpenCV native binaries. The conversion is **not ready for production** due to several critical and high-severity issues documented below.

**Severity counts:** 3 Critical, 4 High, 3 Medium, 2 Low

---

## 2. What Changed (fed49ee → 077ed58)

### 2.1 Dependency Change
- **Removed:** no explicit native-loading dependency (custom `nu.pattern` code handled everything)
- **Added:** `net.java.dev.jna:jna:5.5.0` as a **compile-scope** (non-optional) runtime dependency

### 2.2 OpenCV.java Changes
- Version bump: library filenames updated from `342` to `430`
- Removed Linux x86_32 platform support
- Added `binary.close()` after `Files.copy()` (good fix for resource leak)
- **No JNA integration in OpenCV.java** — the class still uses the old `System.loadLibrary`/`System.load` approach

### 2.3 Test Changes
- `LibraryLoadingTest`: All test logic commented out — **zero test coverage for `nu.pattern` loader**
- `LoadLibraryRunListener`: Entirely commented out — surefire listener that bootstrapped `OpenCV.loadShared()` removed
- `MserTest`: Switched from `OpenCV.loadLocally()` to direct JNA `NativeLibrary.getInstance()` + `System.load()`

### 2.4 Resource Changes
- Added `src/main/resources/darwin/` with `libopencv_java342.dylib` and `libopencv_java430.dylib`
- These are for JNA's default search path (JNA looks in `{platform}/` resource directories)
- **Only macOS** has libraries in the JNA-compatible location; Linux and Windows do not

### 2.5 POM Changes
- Version bumped to 4.3.0-1
- Swapped from `jnaerator-runtime:0.12` to `jna:5.5.0`
- Removed upstream test class extraction step from antrun plugin
- Removed `LoadLibraryRunListener` from surefire listener config
- Reformatted indentation (tabs vs spaces — noise in the diff)

---

## 3. Critical Issues

### CRITICAL-1: JNA is a compile-scope dependency but unused in production code

**File:** `pom.xml:301-305`

JNA 5.5.0 is declared with default (compile) scope, meaning it becomes a **transitive dependency for all consumers**. However, `OpenCV.java` (the only production class) does not import or use JNA at all. The JNA usage is only in `MserTest.java` (test code).

**Impact:** Every downstream project that depends on `org.openpnp:opencv:4.3.0-1` will pull in JNA 5.5.0 unnecessarily, adding ~1.8MB to their classpath and potential version conflicts.

**Recommendation:** Either:
- (a) Complete the migration so `OpenCV.java` uses JNA, or
- (b) Move JNA to `<scope>test</scope>` if it's only needed for tests, or
- (c) Remove JNA entirely and keep the working `nu.pattern` loader

### CRITICAL-2: Two incompatible loading strategies coexist with no integration

The codebase now has two completely separate native loading strategies:

1. **`nu.pattern.OpenCV`** — custom extract-to-temp + `System.loadLibrary`/`System.load` (production API)
2. **JNA `NativeLibrary.getInstance()`** — used only in `MserTest.java` (test code)

Neither strategy is aware of the other. The public API (`OpenCV.loadShared()` / `OpenCV.loadLocally()`) still uses the old approach. Consumers calling `OpenCV.loadShared()` will never use JNA.

**Impact:** The branch name suggests a "convert to JNA loader" but the conversion was never applied to the production code. The test demonstrates JNA works but the actual library doesn't use it.

### CRITICAL-3: All `nu.pattern` loader tests are disabled

**Files:** `LibraryLoadingTest.java`, `LoadLibraryRunListener.java`

All tests for the existing loader (`OpenCV.loadLocally()`, `OpenCV.loadShared()`, spurious-load safety) have been commented out. The commit message explicitly warns: *"IMPORTANT: Look at this commit and revert / fix the test changes so that we don't lose test coverage."*

This was never done. The branch has **zero regression coverage** for the public API.

---

## 4. High-Severity Issues

### HIGH-1: JNA resource layout only covers macOS

**Directory:** `src/main/resources/darwin/`

JNA's `NativeLibrary.getInstance()` searches for libraries using platform-specific resource paths following the `{os}-{arch}` naming convention (e.g., `darwin`, `linux-x86-64`, `win32-x86-64`). The `darwin/` directory was added with macOS binaries, but:
- No `linux-x86-64/` directory exists
- No `win32-x86-64/` or `win32-x86/` directories exist

**Impact:** `MserTest` (which uses JNA loading) can only work on macOS. On Linux/Windows, `NativeLibrary.getInstance()` will fail to find the library unless it's already installed system-wide.

### HIGH-2: Duplicate/stale native libraries bloating JAR

The JAR now contains **both** the old `nu/pattern/opencv/` resource tree **and** the new `darwin/` tree:

| File | Size | Purpose |
|------|------|---------|
| `darwin/libopencv_java430.dylib` | 56.8MB | JNA loader (new) |
| `nu/pattern/opencv/osx/x86_64/libopencv_java430.dylib` | 57.2MB | nu.pattern loader (old) |
| `darwin/libopencv_java342.dylib` | 63.0MB | **Stale — wrong version** |

The `libopencv_java342.dylib` in `darwin/` is the **old 3.4.2 version** and should not be shipped with 4.3.0. Additionally, having two copies of the macOS 4.3.0 dylib (~114MB total) is wasteful.

**Impact:** JAR bloat of ~120MB in unnecessary/duplicate binaries.

### HIGH-3: `SharedLoader.finalize()` is deprecated and unreliable

**File:** `OpenCV.java:216-225`

The `SharedLoader` class overrides `finalize()` to clean up the library path. Since Java 9, `finalize()` is deprecated. It is not guaranteed to run, and in Java 18+ it may be removed entirely.

**Impact:** Library path cleanup may never occur, leaving stale entries in `ClassLoader.usr_paths`. This is pre-existing but becomes more relevant as the branch targets modern JDK support.

### HIGH-4: Reflection access to `ClassLoader.usr_paths` breaks on JDK 16+

**File:** `OpenCV.java:240-260`

`addLibraryPath()` and `removeLibraryPath()` use `Field.setAccessible(true)` on `ClassLoader.usr_paths`. Since JDK 16, the strong encapsulation of internal APIs means this will throw `InaccessibleObjectException` unless `--add-opens java.base/java.lang=ALL-UNNAMED` is passed to the JVM.

**Impact:** `OpenCV.loadShared()` will fail on JDK 16+ without JVM flags — which is presumably one of the motivations for the JNA migration. The JNA approach in `MserTest` would solve this, but it was never integrated into the production code.

---

## 5. Medium-Severity Issues

### MEDIUM-1: `MserTest` uses `com.sun.jna` internal API

**File:** `MserTest.java:20`

The import `com.sun.jna.NativeLibrary` references a `com.sun.*` package, which is technically an internal API. While JNA deliberately uses this package name for historical reasons and it's the standard JNA API, some static analysis tools and module systems may flag it.

### MEDIUM-2: `MserTest` enables JNA debug logging unconditionally

**File:** `MserTest.java:33-34`

```java
System.setProperty("jna.debug_load", "true");
System.setProperty("jna.debug_load.jna", "true");
```

This sets **global JVM system properties** that enable verbose JNA debug logging. In a test suite, this pollutes stdout for all subsequent tests. These debug properties are clearly from development experimentation and should not be committed.

### MEDIUM-3: `TemporaryDirectory.deleteOldInstancesOnStart()` NPE risk

**File:** `OpenCV.java:120`

```java
for (File file : tempDirectory.toFile().listFiles())
```

`File.listFiles()` can return `null` if an I/O error occurs or if the path is not a directory. This would cause a `NullPointerException`. This is pre-existing but worth noting.

---

## 6. Low-Severity Issues

### LOW-1: `ARMv8` arch pattern uses "arm" which may match ARM 32-bit

**File:** `OpenCV.java:69`

```java
ARMv8("arm");
```

The `os.arch` value `"arm"` typically indicates 32-bit ARM, not ARMv8 (64-bit), which usually reports as `"aarch64"`. This means:
- Actual ARMv8/aarch64 systems won't match
- 32-bit ARM systems will match but there's no `linux/ARMv8/` binary present (it was removed in this branch)

### LOW-2: README table lists "OS X x86_32" as supported but code doesn't support it

**File:** `README.md:106`

The support table lists macOS x86_32 but `OpenCV.java` only handles macOS x86_64.

---

## 7. OculiX Compatibility Analysis

### OculiX's Approach (RunTime.java)

OculiX (SikuliX) manages native library loading through its `RunTime` class, which:
1. Exports native libraries from JAR resources to a temp directory
2. Adds the directory to `java.library.path` and system PATH (Windows)
3. Manipulates `ClassLoader.usr_paths` via reflection (same technique as Apertix)
4. Calls `System.loadLibrary()` with the native library name

### Compatibility Assessment

| Aspect | Apertix (current) | OculiX | Compatible? |
|--------|-------------------|--------|-------------|
| Loading mechanism | `System.loadLibrary` / `System.load` | `System.loadLibrary` | Yes |
| Path manipulation | `ClassLoader.usr_paths` reflection | `ClassLoader.usr_paths` reflection | Yes (both break on JDK 16+) |
| Library name | `Core.NATIVE_LIBRARY_NAME` | Configured per library | Yes |
| Temp directory | `opencv_openpnp*` in system temp | SikuliX-managed temp | No conflict |
| Cleanup | Shutdown hook / `deleteOldInstancesOnStart` | SikuliX cleanup | Independent |

**Key findings:**

1. **Current state is compatible.** Since `OpenCV.java` was NOT actually changed to use JNA, OculiX calling `OpenCV.loadShared()` or `OpenCV.loadLocally()` will work exactly as before.

2. **If JNA migration were completed**, OculiX would need updates:
   - OculiX expects libraries to be loadable via `System.loadLibrary()` after Apertix sets up the path
   - If Apertix switched entirely to JNA's `NativeLibrary`, the `ClassLoader.usr_paths` manipulation and path setup that OculiX relies on would be removed
   - OculiX would need to either: (a) use JNA's `NativeLibrary` itself, or (b) call `System.load()` with the path obtained from JNA

3. **The JNA dependency (compile-scope) will propagate to OculiX.** If OculiX already uses a different JNA version, this could cause version conflicts.

---

## 8. Recommendations

### Immediate Actions (before merge)

1. **Decide on the loading strategy.** The branch is in limbo — choose one:
   - **Option A: Complete the JNA migration.** Rewrite `OpenCV.java` to use `NativeLibrary.getInstance()` internally, populate JNA resource directories for all platforms, and update tests.
   - **Option B: Abandon JNA migration.** Revert to the pre-JNA state, keep the `nu.pattern` loader, move JNA to test scope or remove it, and restore the commented-out tests.

2. **Restore test coverage.** Uncomment `LibraryLoadingTest` and `LoadLibraryRunListener`, or write equivalent tests for whichever loading strategy is chosen.

3. **Remove stale binaries.** Delete `darwin/libopencv_java342.dylib` (wrong version) and either the `darwin/` or `nu/pattern/opencv/osx/` duplicate.

4. **Remove JNA debug properties** from `MserTest.java`.

### If Completing JNA Migration (Option A)

1. Create platform resource directories: `linux-x86-64/`, `win32-x86-64/`, `win32-x86/`
2. Wrap JNA loading in `OpenCV.java` so the public API (`loadShared()`/`loadLocally()`) uses JNA internally
3. Handle the case where JNA is not on the classpath (graceful fallback)
4. Scope JNA as a compile dependency (already done) but consider making it `optional`
5. Coordinate with OculiX for any breaking changes
6. Fix the `ARMv8` architecture detection pattern to match `"aarch64"`

### If Keeping `nu.pattern` Loader (Option B)

1. Address the `ClassLoader.usr_paths` reflection issue for JDK 16+ (add `--add-opens` documentation or find alternative)
2. Fix `finalize()` deprecation — use a `Cleaner` (Java 9+) or explicit cleanup API
3. Fix `listFiles()` NPE risk in `deleteOldInstancesOnStart()`

---

## 9. Files Reviewed

| File | Status |
|------|--------|
| `src/main/java/nu/pattern/OpenCV.java` | Audited |
| `src/test/java/nu/pattern/MserTest.java` | Audited |
| `src/test/java/nu/pattern/LibraryLoadingTest.java` | Audited |
| `src/test/java/nu/pattern/LoadLibraryRunListener.java` | Audited |
| `pom.xml` | Audited |
| `NOTES.md` | Reviewed |
| `README.md` | Reviewed |
| `src/main/resources/` (all native binaries) | Inventoried |
| OculiX `RunTime.java` (reference) | Reviewed for compatibility |
