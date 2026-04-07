# Building Apertix

This document describes how the Apertix native binaries are produced, both
automatically via the CI pipeline and manually on a developer workstation.

It is written for two audiences:

1. **Maintainers** who need to understand the full build pipeline, troubleshoot
   CI failures, or cut a new release.
2. **Contributors and users** who want to reproduce a specific native library
   (for example, rebuild the Apple Silicon `.dylib` on their own machine) to
   validate the published binaries or experiment with a different OpenCV
   configuration.

---

## Table of contents

1. [Philosophy](#philosophy)
2. [Supported platforms](#supported-platforms)
3. [Artifact layout](#artifact-layout)
4. [Common prerequisites](#common-prerequisites)
5. [Manual build — Linux x86-64](#manual-build--linux-x86-64)
6. [Manual build — Linux ARM64 (aarch64)](#manual-build--linux-arm64-aarch64)
7. [Manual build — Linux ARMv7 (arm)](#manual-build--linux-armv7-arm)
8. [Manual build — macOS Intel (x86-64)](#manual-build--macos-intel-x86-64)
9. [Manual build — macOS Apple Silicon (aarch64)](#manual-build--macos-apple-silicon-aarch64)
10. [Manual build — Windows (x86 and x64)](#manual-build--windows-x86-and-x64)
11. [The CI pipeline](#the-ci-pipeline)
12. [Release process](#release-process)
13. [Troubleshooting](#troubleshooting)
14. [Credits](#credits)

---

## Philosophy

Apertix is a repackaging of the official OpenCV Java bindings into a
multi-platform fat JAR that can be consumed as a single Maven dependency. The
repository itself contains no prebuilt native binaries — they are produced on
demand by CI runners and attached to GitHub releases. This keeps the git
history lean and avoids the usual problems of committing large binary blobs
(bloated clones, merge conflicts on binaries, unclear provenance).

Each supported platform has its own dedicated CI job that:

1. Downloads the upstream OpenCV source for the target version.
2. Configures the build with `cmake` using flags tailored to that platform.
3. Compiles OpenCV with the Java bindings enabled (`BUILD_JAVA=ON`,
   `BUILD_FAT_JAVA_LIB=ON`).
4. Extracts the resulting native library and upstream `opencv-<version>.jar`.
5. Uploads both as a GitHub Actions artifact.

A final `upload-release` job collects every platform's artifact and pushes
the native libraries as assets on the GitHub release matching the current
`pom.xml` version.

This document explains how to do each of those steps by hand, which is useful
when:

- Validating a CI-built binary on real hardware (CI runners are ephemeral and
  cannot be inspected interactively).
- Rebuilding with a reduced or expanded OpenCV module set.
- Porting Apertix to a new platform that CI does not yet cover.
- Debugging a platform-specific link error.

---

## Supported platforms

The current CI pipeline produces native libraries for the following targets.
All binaries are built against **OpenCV 4.10.0**.

| Platform         | Architecture | Native file                       | CI runner         | CI job                |
| ---------------- | ------------ | --------------------------------- | ----------------- | --------------------- |
| Linux            | x86-64       | `libopencv_java4100.so`           | `ubuntu-latest`   | `build_mac_linux_x64` |
| Linux            | aarch64      | `libopencv_java4100.so`           | ARM64 native      | `build_linux_arm64`   |
| Linux            | ARMv7        | `libopencv_java4100.so`           | ARM native        | `build_linux_arm`     |
| macOS            | x86-64       | `libopencv_java4100.dylib`        | `macos-latest`    | `build_mac_linux_x64` |
| macOS            | aarch64      | `libopencv_java4100.dylib`        | `macos-14`        | `build_mac_aarch64`   |
| Windows          | x86-64       | `opencv_java4100.dll`             | `ubuntu-latest`   | `build_windows`       |
| Windows          | x86-32       | `opencv_java4100.dll`             | `ubuntu-latest`   | `build_windows`       |

Note that the Windows job does not compile OpenCV from source — it downloads
the official OpenCV Windows installer from the upstream GitHub releases and
extracts the prebuilt `.dll` files from it. This is the same approach used
upstream by `nu.pattern.opencv` and is reliable enough that recompiling
ourselves would only add risk.

---

## Artifact layout

Inside the final Apertix fat JAR, the native libraries are placed under
platform-specific resource directories following the JNA naming convention:

```
src/main/resources/
├── darwin/                    # macOS Intel
│   └── libopencv_java4100.dylib
├── darwin-aarch64/            # macOS Apple Silicon
│   └── libopencv_java4100.dylib
├── linux-x86-64/              # Linux x86-64
│   └── libopencv_java4100.so
├── linux-aarch64/             # Linux ARM64
│   └── libopencv_java4100.so
├── linux-arm/                 # Linux ARMv7
│   └── libopencv_java4100.so
├── win32-x86-64/              # Windows x86-64
│   └── opencv_java4100.dll
└── win32-x86/                 # Windows x86-32
    └── opencv_java4100.dll
```

At runtime, the JNA-based loader inspects the current OS and architecture and
picks the correct subdirectory automatically. This is why it is essential that
every native library is placed in the exact directory expected by JNA —
misplacing a `.dylib` under `darwin/` when it was built for ARM64 will lead
to cryptic `UnsatisfiedLinkError` failures on Apple Silicon machines.

---

## Common prerequisites

Regardless of the target platform, you will need:

- **Java 17** (JDK, not JRE). The CI uses Temurin 17. Newer versions
  (21, 22, 25) also work for consuming Apertix, but the build itself is
  tested against 17.
- **Maven 3.8+** for the final `mvn install` step that packages the fat JAR.
- **CMake 3.16+** to configure the OpenCV build.
- **Git** to clone this repository.
- Approximately **10 GB of free disk space** for the full OpenCV source tree,
  build directory, and resulting binaries.
- A working internet connection (the build downloads both the OpenCV source
  archive and, on Windows, the prebuilt OpenCV installer).

Platform-specific requirements are listed in each section below.

---

## Manual build — Linux x86-64

**Tested on:** Ubuntu 22.04 LTS with GCC 11.

Install the system packages:

```bash
sudo apt-get update
sudo apt-get install -y build-essential cmake git unzip wget \
  libgtk-3-dev libavcodec-dev libavformat-dev libswscale-dev \
  libv4l-dev libxvidcore-dev libx264-dev libjpeg-dev libpng-dev \
  libtiff-dev libatlas-base-dev gfortran python3-dev
```

Download and extract the OpenCV source:

```bash
wget https://github.com/opencv/opencv/archive/4.10.0.zip
unzip 4.10.0.zip
cd opencv-4.10.0
mkdir build && cd build
```

Configure with CMake:

```bash
cmake \
  -D OPENCV_FORCE_3RDPARTY_BUILD=ON \
  -D BUILD_JAVA=ON \
  -D BUILD_FAT_JAVA_LIB=ON \
  -D OPENCV_ENABLE_NONFREE=ON \
  -D BUILD_SHARED_LIBS=OFF \
  -D BUILD_PERF_TESTS=OFF \
  -D BUILD_TESTS=OFF \
  -D BUILD_EXAMPLES=OFF \
  -D BUILD_DOCS=OFF \
  -D BUILD_PACKAGE=OFF \
  -D BUILD_opencv_python2=OFF \
  -D BUILD_opencv_python3=OFF \
  -D BUILD_opencv_apps=OFF \
  -D BUILD_opencv_gapi=OFF \
  -D CMAKE_BUILD_TYPE=RELEASE \
  ..
```

Build:

```bash
make -j$(nproc)
```

The resulting `libopencv_java4100.so` will be in `opencv-4.10.0/build/lib/`.
Copy it into the Apertix tree:

```bash
cp opencv-4.10.0/build/lib/libopencv_java4100.so \
   /path/to/Apertix/src/main/resources/linux-x86-64/
```

Then build the Apertix JAR:

```bash
cd /path/to/Apertix
mvn -B install -DskipTests
```

---

## Manual build — Linux ARM64 (aarch64)

**Tested on:** Ubuntu 22.04 LTS on a native ARM64 machine (Raspberry Pi 4,
Apple Silicon under a Linux VM, or AWS Graviton). Cross-compilation from
x86-64 works but is significantly more fragile and is not covered here.

Install the same packages as for Linux x86-64, then follow the exact same
cmake and make commands. The OpenCV build system auto-detects the aarch64
architecture and emits the correct object code.

Copy the resulting `libopencv_java4100.so` into
`src/main/resources/linux-aarch64/`.

---

## Manual build — Linux ARMv7 (arm)

**Tested on:** Ubuntu 22.04 LTS 32-bit on a Raspberry Pi 3, or via
cross-compilation from x86-64 using a `gcc-arm-linux-gnueabihf` toolchain.

The native ARMv7 build is slow (expect 45 to 90 minutes on a Pi 3) but
straightforward. The CI uses a native ARM runner to avoid QEMU-related
instability; cross-compilation from x86-64 also works but requires
additional CMake toolchain file configuration that is out of scope here.

Commands identical to Linux x86-64. Copy the output to
`src/main/resources/linux-arm/`.

---

## Manual build — macOS Intel (x86-64)

**Tested on:** macOS 13 Ventura on Intel hardware with Xcode 14 and
Homebrew-provided `cmake`.

Install prerequisites via Homebrew:

```bash
brew install cmake wget
```

Then follow the standard OpenCV build commands:

```bash
wget https://github.com/opencv/opencv/archive/4.10.0.zip
unzip 4.10.0.zip
cd opencv-4.10.0
mkdir build && cd build
cmake \
  -D OPENCV_FORCE_3RDPARTY_BUILD=ON \
  -D BUILD_JAVA=ON \
  -D BUILD_FAT_JAVA_LIB=ON \
  -D OPENCV_ENABLE_NONFREE=ON \
  -D BUILD_SHARED_LIBS=OFF \
  -D BUILD_PERF_TESTS=OFF \
  -D BUILD_TESTS=OFF \
  -D BUILD_EXAMPLES=OFF \
  -D BUILD_DOCS=OFF \
  -D BUILD_PACKAGE=OFF \
  -D BUILD_opencv_python2=OFF \
  -D BUILD_opencv_python3=OFF \
  -D BUILD_opencv_apps=OFF \
  -D BUILD_opencv_gapi=OFF \
  -D CMAKE_BUILD_TYPE=RELEASE \
  ..
make -j$(sysctl -n hw.ncpu)
```

Copy the resulting `libopencv_java4100.dylib` into
`src/main/resources/darwin/`.

---

## Manual build — macOS Apple Silicon (aarch64)

**Tested on:** macOS 14 Sonoma on M1 / M2 / M3 with Xcode 15 and
Homebrew-provided `cmake`. Also independently validated by
[@RaiMan](https://github.com/RaiMan) (Raimund Hocke) on Apple Silicon —
see the credits section for the minimal cmake recipe he contributed.

### Option A — Full build (matches the CI pipeline exactly)

This is the configuration used by the `build_mac_aarch64` CI job and is the
recommended path if you want a binary identical to the one published in the
Apertix release. It enables the full set of OpenCV modules so that any
downstream consumer gets the same feature coverage as on other platforms:

```bash
brew install cmake wget
wget https://github.com/opencv/opencv/archive/4.10.0.zip
unzip 4.10.0.zip
cd opencv-4.10.0
mkdir build && cd build
cmake \
  -D OPENCV_FORCE_3RDPARTY_BUILD=ON \
  -D BUILD_JAVA=ON \
  -D BUILD_FAT_JAVA_LIB=ON \
  -D OPENCV_ENABLE_NONFREE=ON \
  -D BUILD_SHARED_LIBS=OFF \
  -D BUILD_PERF_TESTS=OFF \
  -D BUILD_TESTS=OFF \
  -D BUILD_EXAMPLES=OFF \
  -D BUILD_DOCS=OFF \
  -D BUILD_PACKAGE=OFF \
  -D BUILD_opencv_python2=OFF \
  -D BUILD_opencv_python3=OFF \
  -D BUILD_opencv_apps=OFF \
  -D BUILD_opencv_gapi=OFF \
  -D CMAKE_BUILD_TYPE=RELEASE \
  -D CMAKE_APPLE_SILICON_PROCESSOR=arm64 \
  -D CMAKE_CXX_STANDARD=17 \
  ..
make -j$(sysctl -n hw.ncpu)
```

The key flags that differentiate this from the Intel build are
`CMAKE_APPLE_SILICON_PROCESSOR=arm64` (which forces the target architecture)
and `CMAKE_CXX_STANDARD=17` (which is required by some of the Apple Silicon
codepaths).

Copy the resulting `libopencv_java4100.dylib` into
`src/main/resources/darwin-aarch64/`.

### Option B — Minimal build ([@RaiMan](https://github.com/RaiMan)'s recipe)

If your goal is a lightweight binary that covers only the core computer
vision modules (calibration, features, image I/O, image processing, object
detection), [@RaiMan](https://github.com/RaiMan) contributed a minimal cmake
configuration that builds only eight OpenCV modules. This produces a smaller
binary and avoids a number of build errors that can occur on Apple Silicon
when compiling the full OpenCV tree:

```bash
cmake \
  -DCMAKE_OSX_ARCHITECTURES=arm64 \
  -DCMAKE_BUILD_TYPE=Release \
  -DBUILD_SHARED_LIBS:BOOL=OFF \
  -DBUILD_opencv_java=ON \
  -DBUILD_opencv_python3=OFF \
  -DBUILD_TESTS=OFF \
  -DBUILD_PERF_TESTS=OFF \
  -DBUILD_EXAMPLES=OFF \
  -DBUILD_DOCS=OFF \
  -DWITH_IPP=OFF \
  -DJAVA_AWT_INCLUDE_PATH="$JAVA_HOME/include" \
  -DJAVA_AWT_LIBRARY="$JAVA_HOME/lib/libawt.dylib" \
  -DJAVA_INCLUDE_PATH="$JAVA_HOME/include" \
  -DJAVA_INCLUDE_PATH2="$JAVA_HOME/include/darwin" \
  -DJAVA_JVM_LIBRARY="$JAVA_HOME/lib/server/libjvm.dylib" \
  -DBUILD_LIST:STRING=calib3d,core,features2d,highgui,imgcodecs,imgproc,java,objdetect \
  ..
```

Note the explicit `JAVA_HOME`-based paths for the JNI headers and libraries,
and the `BUILD_LIST` restricting the build to eight core modules
(`calib3d`, `core`, `features2d`, `highgui`, `imgcodecs`, `imgproc`, `java`,
`objdetect`). `WITH_IPP=OFF` disables Intel Performance Primitives, which
are neither relevant nor reliably buildable on Apple Silicon.

Set `JAVA_HOME` to your JDK root before running cmake (tested with
Java 22 / OpenJDK). The resulting binary loads cleanly under the JNA
loader and is sufficient for any consumer that only needs the core
computer vision modules listed above.

### Which option should I use?

- If you are rebuilding the Apertix release binary or contributing back to
  the project, use **Option A** for consistency with CI.
- If you only need the core computer vision modules, want a smaller binary,
  or if Option A fails with module-specific errors on your machine, use
  **Option B**.

Both options produce a `libopencv_java4100.dylib` that loads correctly under
the JNA loader.

---

## Manual build — Windows (x86 and x64)

Unlike the Linux and macOS targets, the Windows binaries are **not**
recompiled from source. The CI pipeline downloads the official OpenCV
Windows installer from the upstream GitHub releases and extracts the
prebuilt `opencv_java4100.dll` files directly. This matches the approach
used upstream by `nu.pattern.opencv` and avoids maintaining a second build
toolchain for Windows.

To reproduce manually:

1. Download `opencv-4.10.0-windows.exe` from
   <https://github.com/opencv/opencv/releases/tag/4.10.0>
2. Extract it (it is a self-extracting 7-Zip archive — use `7z x` or
   double-click on Windows).
3. Locate the two DLLs:
   - `opencv/build/java/x64/opencv_java4100.dll`
   - `opencv/build/java/x86/opencv_java4100.dll`
4. Copy them into the Apertix resources:
   - `src/main/resources/win32-x86-64/opencv_java4100.dll`
   - `src/main/resources/win32-x86/opencv_java4100.dll`

Both files come directly from the upstream OpenCV release and are therefore
bit-for-bit identical to what any other OpenCV Java consumer on Windows
would use.

---

## The CI pipeline

The full build pipeline lives in `.github/workflows/build.yml` and is
triggered exclusively by manual `workflow_dispatch`. It is structured as a
fan-out / fan-in DAG:

```
  build_linux_arm         ┐
  build_linux_arm64       ├──► build_dist ──► upload-release
  build_mac_linux_x64     │
  build_mac_aarch64       │
  build_windows           ┘
```

Each leaf job produces a GitHub Actions artifact containing the native
library and (where applicable) the upstream `opencv-4100.jar`. The
`build_dist` job downloads every artifact, assembles the Apertix fat JAR
containing all platforms, and runs the JNA-based smoke tests to verify that
each native library loads correctly on its matching runner.

Finally, `upload-release` pushes every native library and the fat JAR as
assets on the GitHub release matching the current `pom.xml` version (for
example, `v4.10.0-0`). Assets are uploaded with `gh release upload --clobber`,
which means that re-running the workflow will overwrite the existing assets
in place rather than creating duplicates. Note that `--clobber` updates the
asset content but does **not** update the release's `published_at` date —
this is a GitHub behavior, not a bug.

To trigger a new build manually:

```bash
gh workflow run build.yml --repo julienmerconsulting/Apertix
```

To watch the progress:

```bash
gh run watch --repo julienmerconsulting/Apertix
```

---

## Release process

Cutting a new Apertix release involves three steps:

1. **Bump the version in `pom.xml`** (for example from `4.10.0-0` to
   `4.10.0-1` for a rebuild of the same OpenCV version, or to `4.11.0-0`
   for a new upstream OpenCV). Commit and push the change.

2. **Create an empty GitHub release** with the matching tag:

   ```bash
   gh release create v4.10.0-1 \
     --repo julienmerconsulting/Apertix \
     --title "Apertix 4.10.0-1" \
     --notes "See RELEASE_NOTES.md"
   ```

   This step is necessary because `gh release upload` in the workflow
   expects the target release to already exist.

3. **Trigger the build workflow**:

   ```bash
   gh workflow run build.yml --repo julienmerconsulting/Apertix
   ```

   The workflow will produce every native library and attach them as assets
   to the release created in step 2.

When the workflow completes successfully, verify that all expected assets
are present:

```bash
gh release view v4.10.0-1 --repo julienmerconsulting/Apertix \
  --json assets --jq ".assets[] | .name"
```

You should see all seven native files plus the fat JAR.

---

## Troubleshooting

### The CI build is green but the release has no new assets

The most common cause is that the release tag expected by the
`upload-release` job does not exist yet. Create the empty release first with
`gh release create`, then re-run the workflow.

Another possibility: the release was already published, `--clobber` replaced
the assets in place, and the `published_at` date in the GitHub UI still
shows the original publication date. This is expected behavior — check the
`updated_at` date on individual assets instead:

```bash
gh release view v4.10.0-0 --repo julienmerconsulting/Apertix \
  --json assets --jq ".assets[] | \"\(.name) updated=\(.updatedAt)\""
```

### `UnsatisfiedLinkError` at runtime on Apple Silicon

Check that `src/main/resources/darwin-aarch64/` exists and contains the
ARM64 dylib — not the Intel one. A common mistake is to accidentally place
the Intel dylib in the aarch64 directory or vice versa. Verify the
architecture with:

```bash
file src/main/resources/darwin-aarch64/libopencv_java4100.dylib
```

You should see `Mach-O 64-bit dynamically linked shared library arm64`.

### OpenCV full build fails on Apple Silicon with module errors

Use the minimal cmake recipe in Option B above. It disables the OpenCV
modules that are known to fail on Apple Silicon (in particular anything
depending on Intel Performance Primitives via `WITH_IPP`) and limits the
build to a smaller, reliably compilable set of core modules.

### The build completes but the JAR is missing native libraries

Run `mvn -B install -DskipTests=false` to execute the JNA smoke tests. They
will list exactly which platforms are missing from the JAR and fail the
build early, which is usually faster than discovering the problem at
runtime in a consumer project.

---

## Credits

The Apertix build pipeline stands on the shoulders of several contributions:

- **`openpnp/opencv`** — the original multi-platform OpenCV repackaging
  project that Apertix forked from. The directory layout, JNA-based loading
  strategy, and the general approach of using CI runners to build per-platform
  binaries all come from there.

- **The OpenCV project** — for the upstream source code, the official
  Windows installer, and the well-maintained cmake build system that makes
  cross-platform compilation possible at all.

- **[@RaiMan](https://github.com/RaiMan)** (Raimund Hocke), maintainer
  emeritus of SikuliX, who independently built `libopencv_java4100.dylib`
  on Apple Silicon using the minimal cmake recipe documented in
  [Option B](#option-b--minimal-build-raimans-recipe) above. This
  contribution is the first independent validation that the Apertix
  Apple Silicon pipeline produces a correct binary, and the minimal recipe
  itself has been adopted as the reference procedure for contributors who
  want a lighter core-modules-only build or need to avoid the failure modes
  of the full OpenCV module set on macOS ARM64.

- **All contributors** to Apertix and to the upstream SikuliX lineage whose
  prior work on native loading, JNA integration, and build stability made
  the current pipeline possible.

---

*Last updated: April 2026. If you find an error in this document or have
a cleaner procedure for a platform we already support, please open a pull
request — this file is the authoritative reference for anyone rebuilding
Apertix from source.*
