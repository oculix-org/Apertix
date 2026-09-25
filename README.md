<div align="center">

<br>

<h1>
  <code>&nbsp;A&nbsp;P&nbsp;E&nbsp;R&nbsp;T&nbsp;I&nbsp;X&nbsp;</code>
</h1>
<p><em>From the Latin <strong>apertura</strong>, opening, aperture.</em><br>
<em>Because every pixel is a window into automation.</em></p>

**OpenCV Java bindings and native libraries, one Maven dependency, nine platforms.**

<br>

<p>
  <a href="https://github.com/oculix-org/Apertix/releases"><img src="https://img.shields.io/badge/version-4.10.0--3-blue?style=flat-square&logo=github" alt="Version"></a>
  <a href="https://opencv.org/"><img src="https://img.shields.io/badge/OpenCV-4.10.0-red?style=flat-square&logo=opencv" alt="OpenCV"></a>
  <a href="https://github.com/oculix-org/Apertix/blob/master/LICENSE"><img src="https://img.shields.io/badge/license-BSD--3--Clause-green?style=flat-square" alt="License"></a>
  <a href="https://github.com/oculix-org/Apertix/actions"><img src="https://img.shields.io/badge/natives-built%20by%20CI%2C%20never%20committed-8250df?style=flat-square&logo=githubactions" alt="CI"></a>
</p>

<p>
  <code>Java 17</code> &middot; <code>Windows</code> &middot; <code>macOS</code> &middot; <code>Linux</code> &middot; <code>ARM</code> &middot; <code>zero configuration</code>
</p>

</div>

---

## 🧭 What it is

OpenCV publishes its Java bindings as source only. Getting them into a JVM application means compiling OpenCV yourself on every platform you ship to, then finding a way to load the right native at runtime. [openpnp/opencv](https://github.com/openpnp/opencv) solved that for years and stopped in February 2024, with bindings at 4.3.0 and natives at 4.9.0.

Apertix picks up from there. It compiles OpenCV 4.10.0 from the official sources, bindings and natives together, on GitHub Actions runners, and ships the result as a single jar. Add the dependency, call one method, use OpenCV.

| Project | Java bindings | Native binaries | Status |
|---|---|---|---|
| opencv/opencv | 4.10.0 | source only | upstream |
| openpnp/opencv | 4.3.0 | 4.9.0 | stopped February 2024 |
| Apertix | 4.10.0 | 4.10.0 | maintained here |

The `nu.pattern` package and the `OpenCV.loadShared()` entry point are kept exactly as openpnp consumers know them. Switching is a change of Maven coordinates, not of code.

## 🚀 Quick start

```xml
<dependency>
  <groupId>io.github.oculix-org</groupId>
  <artifactId>apertix</artifactId>
  <version>4.10.0-4</version>
</dependency>
```

```java
import nu.pattern.OpenCV;
import org.opencv.core.Core;

public class Main {
    public static void main(String[] args) {
        OpenCV.loadShared();
        System.out.println("OpenCV " + Core.VERSION);
    }
}
```

Versions up to `4.10.0-3` were published as `io.github.julienmerconsulting.apertix:opencv` and stay available on Maven Central under those coordinates. From `4.10.0-4` on, Apertix lives in the [oculix-org](https://github.com/oculix-org) organisation next to [Legerix](https://github.com/oculix-org/Legerix) and [Octachorix](https://github.com/oculix-org/Octachorix), and publishes under `io.github.oculix-org`.

Versioning is `<opencv version>-<build number>`. A build number bump re-ships the same OpenCV with a packaging or CI fix; an OpenCV upgrade resets the build number to `0`.

## 🔌 How it works

```
opencv/opencv 4.10.0 sources
    │  compiled on one CI runner per platform, Java bindings enabled
    ▼
Apertix jar: org.opencv.* classes + one native per platform directory
    │  nu.pattern.OpenCV.loadShared() picks the directory for the running OS and CPU
    ▼
your application
```

The native is extracted from the jar to a temporary directory and loaded with `System.load`. No `java.library.path`, no environment variable, no installer.

## 🖥️ Platforms

| Platform | CPU | Directory in the jar | Built by |
|---|---|---|---|
| Windows | x86-64 | `win32-x86-64/opencv_java4100.dll` | official OpenCV Windows distribution |
| Windows | x86 | `win32-x86/opencv_java4100.dll` | official OpenCV Windows distribution |
| macOS | Intel | `darwin/libopencv_java4100.dylib` | compiled on `macos` Intel runner |
| macOS | Apple Silicon | `darwin-aarch64/libopencv_java4100.dylib` | compiled on `macos-14` |
| Linux | x86-64, glibc 2.35 or newer | `linux-x86-64/libopencv_java4100.so` | compiled on `ubuntu-latest` |
| Linux | x86-64, glibc 2.28 or newer | `linux-x86-64-legacy/libopencv_java4100.so` | compiled in `manylinux_2_28_x86_64` |
| Linux | aarch64, glibc 2.35 or newer | `linux-aarch64/libopencv_java4100.so` | compiled on `ubuntu-22.04-arm` |
| Linux | aarch64, glibc 2.28 or newer | `linux-aarch64-legacy/libopencv_java4100.so` | compiled in `manylinux_2_28_aarch64` |
| Linux | ARMv7 | `linux-arm/libopencv_java4100.so` | compiled on `ubuntu-22.04-arm` |

The `-legacy` Linux natives reference no glibc symbol newer than 2.28, so they run on RHEL, Rocky and Alma 8 and 9, Ubuntu 22.04, Debian 12 and anything newer. CI refuses to ship a legacy native that references a newer symbol. `OpenCV.loadShared()` loads the modern native by default; a consumer running on an older glibc reads the `-legacy` resource itself with `ClassLoader.getResourceAsStream` and loads it with `System.load`.

Every native is built with the same module set: no Python, no apps, no G-API, and no `obsensor`, whose Orbbec camera support linked the macOS dylib against a library nobody ships.

## 🧪 Build and release

The repository holds no native binary. `git clone` gives you the loader, the tests and the upstream `opencv-4100.jar` the pom unpacks; the natives exist only in CI artifacts and in GitHub releases. Cloning is a few megabytes, not a few hundred.

| Step | What happens |
|---|---|
| bump `<version>` in `pom.xml`, commit, push | nothing runs yet |
| `git tag v4.10.0-4 && git push origin v4.10.0-4` | `build-release-publish.yml` starts: seven platform builds in parallel, then `build_dist` |
| `build_dist` | wipes `src/main/resources/*`, stages the nine fresh natives, refuses to continue unless exactly nine are present and every name carries `4100`, then `mvn deploy -P release` to Maven Central |
| `release` | creates the GitHub release for the tag if it does not exist and uploads the jar and every native as assets |

Rebuilding a single native by hand, for example to inspect the Apple Silicon dylib on real hardware, is documented step by step in [BUILDING.md](BUILDING.md).

## 🦎 Used by

Apertix is the vision engine of [OculiX](https://github.com/oculix-org/Oculix), the continuation of SikuliX: template matching on desktops, point-of-sale terminals, kiosks, mainframe emulators and Android devices. OculiX calls `core` and `imgproc` only, which is why Apertix tracks OpenCV upgrades on its own schedule rather than chasing every release.

## 📜 Lineage and license

The `nu.pattern` loader was written by Michael Ahlers at [Pattern Consulting](https://github.com/PatternConsulting/opencv). Jason von Nieda carried it at [openpnp/opencv](https://github.com/openpnp/opencv) from 2.4.9 to 4.9.0, and the commit history here starts with his. Apertix continues that work under a new name so that nobody mistakes it for an openpnp release.

OpenCV and this packaging are distributed under the [3-clause BSD license](LICENSE) of the OpenCV project.

<div align="center">
  <br>
  <p>Maintained by <a href="https://github.com/julienmerconsulting">Julien Mer</a> for <a href="https://github.com/oculix-org">oculix-org</a>.</p>
  <br>
</div>
