## Apertix 4.10.0-0

Fork de openpnp/opencv - OpenCV 4.10.0 packagé avec loader JNA pour Java.

### Changements majeurs
- OpenCV 4.10.0 compilé from scratch sur Windows MSVC
- Migration JNA complète (compatible JDK 16+, zéro --add-opens)
- Fix bug libopencv_java stale manifest Linux/macOS
- Fix ARMv8 : arm -> aarch64
- Suppression TemporaryDirectory et finalize() (deprecated)

### Plateformes
- Windows x86-64 : opencv_java4100.dll
- Linux x86-64 : placeholder (à compiler)
- macOS : placeholder (à compiler)

### Base
- Fork de openpnp/opencv @ BSD License (Jason von Nieda)