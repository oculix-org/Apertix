# linux-aarch64 (ARM64) Native Library

The `libopencv_java490.so` binary for Linux ARM64 (aarch64) is provided by the
upstream `org.openpnp:opencv:4.9.0-0` JAR on Maven Central. JNA automatically
extracts the correct native binary at runtime from the classpath, so no manual
compilation is required.

If you need to override the bundled binary, place a custom-built
`libopencv_java490.so` in this directory.
