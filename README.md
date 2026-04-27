<div align="center">

<br>

<br>
<h1>
  <code>&nbsp;A&nbsp;P&nbsp;E&nbsp;R&nbsp;T&nbsp;I&nbsp;X&nbsp;</code>
</h1>
<p><em>From the Latin <strong>apertura</strong> — opening, aperture.</em><br>
<em>Because every pixel is a window into automation.</em></p>

**OpenCV 4.10.0 Java bindings — packaged with native libraries**

<br>

<p>
  <a href="https://github.com/julienmerconsulting/Apertix/releases"><img src="https://img.shields.io/badge/version-4.10.0--2-blue?style=flat-square" alt="Version"></a>
  <a href="https://github.com/julienmerconsulting/Apertix/blob/master/LICENSE"><img src="https://img.shields.io/badge/license-MIT-green?style=flat-square" alt="License"></a>
  <a href="https://opencv.org/"><img src="https://img.shields.io/badge/OpenCV-4.10.0-red?style=flat-square&logo=opencv" alt="OpenCV"></a>
  <a href="https://github.com/julienmerconsulting/Apertix/actions"><img src="https://img.shields.io/badge/CI-GitHub%20Actions-blue?style=flat-square&logo=githubactions" alt="CI"></a>
</p>

<p>
  <code>OpenCV 4.10.0</code> &middot; <code>Java 17</code> &middot; <code>JNA</code> &middot; <code>Cross-Platform</code> &middot; <code>Zero Config</code>
</p>

<br>

> **A maintained fork of [openpnp/opencv](https://github.com/openpnp/opencv), picking up where they left off.**
>
> openpnp stopped updating Java bindings at 4.3.0 and native binaries at 4.9.0.
> Apertix compiles OpenCV 4.10.0 from source — bindings and natives — and delivers them as a single Maven dependency.

</div>

---

<br>

## Why Apertix

The OpenCV Java ecosystem has a gap:

| Project | Java Bindings | Native Binaries | Last Update |
|---|---|---|---|
| **opencv/opencv** (upstream) | 4.10.0 | Source only | Active |
| **openpnp/opencv** | 4.3.0 | 4.9.0 | Feb 2024 |
| **Apertix** | **4.10.0** | **4.10.0** | **Active** |

Apertix fills that gap. One dependency, all platforms, up-to-date bindings and natives.

<br>

## How it works

```
opencv/opencv (C++ source)
    ↓ compiled from source
Apertix (Java bindings + native libraries)
    ↓ loaded via JNA
Your application (or OculiX)
```

The native library is extracted at runtime from the JAR and loaded automatically via `nu.pattern.OpenCV.loadShared()`. No manual `System.loadLibrary()`, no environment variables, no native library path configuration.

<br>

## Platform Support

| Platform | Architecture | Native Library | Status |
|---|---|---|---|
| **Windows** | x86_64 | `opencv_java4100.dll` | Available |
| **Windows** | x86 (32-bit) | `opencv_java4100.dll` | Available |
| **macOS** | x86_64 | `libopencv_java4100.dylib` | Available |
| **macOS** | ARM64 (Apple Silicon) | `libopencv_java4100.dylib` | Available |
| **Linux** | x86_64 | `libopencv_java4100.so` | Available |
| **Linux** | ARM64 (aarch64) | `libopencv_java4100.so` | Available |
| **Linux** | ARM (armv7) | `libopencv_java4100.so` | Available |

<br>

## Quick Start

### Maven

```xml
<dependency>
  <groupId>io.github.julienmerconsulting.apertix</groupId>
  <artifactId>opencv</artifactId>
  <version>4.10.0-2</version>
</dependency>
```

### Usage

```java
import nu.pattern.OpenCV;
import org.opencv.core.Core;

public class Main {
    public static void main(String[] args) {
        OpenCV.loadShared();
        System.out.println("OpenCV version: " + Core.VERSION);
        // → OpenCV version: 4.10.0
    }
}
```

### Backwards Compatibility

Apertix keeps the same `nu.pattern` package structure as openpnp/opencv. Switching is a one-line change in your `pom.xml` — replace the `groupId` from `org.openpnp` to `io.github.julienmerconsulting.apertix`. No code changes required.

<br>

## What changed from openpnp

| Change | Details |
|---|---|
| **OpenCV version** | 4.3.0 bindings → **4.10.0** bindings compiled from source |
| **Native binaries** | 4.9.0 → **4.10.0** compiled from source |
| **Java version** | Java 8 → **Java 17** recommended |
| **CI/CD** | Added GitHub Actions workflows for multi-platform builds |
| **Windows DLL** | `opencv_java4100.dll` compiled with MSVC from OpenCV 4.10.0 source |
| **macOS dylib** | Build workflow via GitHub Actions runners (x86_64 + ARM64 Apple Silicon) |
| **Linux .so** | Build workflow via GitHub Actions runners (x86_64 + ARM64 + ARMv7) |
| **Maven coordinates** | `org.openpnp:opencv` → `io.github.julienmerconsulting.apertix:opencv` |
| **Package structure** | `nu.pattern` preserved — drop-in replacement |

<br>

## Building from Source

```bash
# Clone
git clone https://github.com/julienmerconsulting/Apertix.git
cd Apertix

# Build and test
mvn clean test

# Install locally
mvn clean install -DskipTests
```

The CI/CD pipeline compiles OpenCV native libraries from source on each platform using GitHub Actions runners. The `build_dist` job assembles all native binaries into a single fat JAR.

<br>

## Used by

Apertix is the computer vision engine behind [**OculiX**](https://github.com/julienmerconsulting/Oculix) — the actively maintained continuation of SikuliX, providing visual automation for desktops, POS terminals, kiosks, and Android devices.

<br>

---

<div align="center">
  <br>
  <p>
    <strong>Apertix</strong> &mdash; OpenCV for Java, maintained and up-to-date
  </p>
  <p>
    Maintained by <a href="https://github.com/julienmerconsulting">Julien MER</a> &middot;
    Original package by <a href="https://github.com/PatternConsulting/opencv">Pattern Consulting</a> &middot;
    Forked from <a href="https://github.com/openpnp/opencv">openpnp</a> &middot;
    <a href="https://github.com/julienmerconsulting/Apertix/blob/master/LICENSE">MIT License</a>
  </p>
  <br>
</div>
