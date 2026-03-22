package nu.pattern;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.sun.jna.NativeLibrary;
import org.opencv.core.Core;

public class OpenCV {

  private final static Logger logger = Logger.getLogger(OpenCV.class.getName());

  static enum OS {
    OSX("^[Mm]ac OS X$"),
    LINUX("^[Ll]inux$"),
    WINDOWS("^[Ww]indows.*");

    private final Set<Pattern> patterns;

    private OS(final String... patterns) {
      this.patterns = new HashSet<Pattern>();

      for (final String pattern : patterns) {
        this.patterns.add(Pattern.compile(pattern));
      }
    }

    private boolean is(final String id) {
      for (final Pattern pattern : patterns) {
        if (pattern.matcher(id).matches()) {
          return true;
        }
      }
      return false;
    }

    public static OS getCurrent() {
      final String osName = System.getProperty("os.name");

      for (final OS os : OS.values()) {
        if (os.is(osName)) {
          logger.log(Level.FINEST, "Current environment matches operating system descriptor \"{0}\".", os);
          return os;
        }
      }

      throw new UnsupportedOperationException(String.format("Operating system \"%s\" is not supported.", osName));
    }
  }

  static enum Arch {
    X86_32("i386", "i686", "x86"),
    X86_64("amd64", "x86_64"),
    ARMv7("arm"),
    ARMv8("aarch64");

    private final Set<String> patterns;

    private Arch(final String... patterns) {
      this.patterns = new HashSet<String>(Arrays.asList(patterns));
    }

    private boolean is(final String id) {
      return patterns.contains(id);
    }

    public static Arch getCurrent() {
      final String osArch = System.getProperty("os.arch");

      for (final Arch arch : Arch.values()) {
        if (arch.is(osArch)) {
          logger.log(Level.FINEST, "Current environment matches architecture descriptor \"{0}\".", arch);
          return arch;
        }
      }

      throw new UnsupportedOperationException(String.format("Architecture \"%s\" is not supported.", osArch));
    }
  }

  private static class UnsupportedPlatformException extends RuntimeException {
    private UnsupportedPlatformException(final OS os, final Arch arch) {
      super(String.format("Operating system \"%s\" and architecture \"%s\" are not supported.", os, arch));
    }
  }

  /**
   * Cleans up temporary directories left behind by previous versions of this
   * library that used the custom extraction approach (prefix "opencv_openpnp").
   * Safe to call on any platform; errors are logged and swallowed.
   */
  static void cleanupOldTempDirs() {
    final String OPENCV_PREFIX = "opencv_openpnp";
    try {
      final Path tempDirectory = new File(System.getProperty("java.io.tmpdir")).toPath();
      final File[] files = tempDirectory.toFile().listFiles();
      if (files == null) {
        return;
      }
      for (final File file : files) {
        if (file.isDirectory() && file.getName().startsWith(OPENCV_PREFIX)) {
          try {
            deleteRecursively(file.toPath());
          } catch (final RuntimeException e) {
            if (e.getCause() instanceof AccessDeniedException) {
              logger.fine("Could not delete old OpenCV temp directory (likely in use): " + file);
            }
          }
        }
      }
    } catch (final Exception e) {
      logger.log(Level.FINE, "Error during old temp directory cleanup.", e);
    }
  }

  private static void deleteRecursively(final Path path) {
    if (!Files.exists(path)) {
      return;
    }
    try {
      Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException e) throws IOException {
          Files.deleteIfExists(dir);
          return super.postVisitDirectory(dir, e);
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
          Files.deleteIfExists(file);
          return super.visitFile(file, attrs);
        }
      });
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Exactly once per {@link ClassLoader}, attempt to load the native library.
   * First tries the system-wide installation via {@link System#loadLibrary(String)}.
   * If that fails, uses JNA to locate and extract the native binary from classpath
   * resources, then loads it via {@link System#load(String)} so it is available to
   * all code in the same class loader. Spurious calls are safe.
   */
  public static void loadShared() {
    SharedLoader.getInstance();
  }

  /**
   * @see <a href="http://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom">Initialization-on-demand holder idiom</a>
   */
  private static class SharedLoader {

    private SharedLoader() {
      // First, try the system-wide installation.
      try {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        logger.log(Level.FINEST, "Loaded existing OpenCV library \"{0}\" from library path.", Core.NATIVE_LIBRARY_NAME);
        return;
      } catch (final UnsatisfiedLinkError ule) {
        // Only fall through to JNA extraction if the library is simply not on the path.
        final String errorFragment = String.format("no %s in java.library.path", Core.NATIVE_LIBRARY_NAME);
        if (ule.getMessage() == null || !ule.getMessage().contains(errorFragment)) {
          logger.log(Level.FINEST, "Encountered unexpected loading error.", ule);
          throw ule;
        }
      }

      // Clean up temp dirs left by previous versions of this library.
      cleanupOldTempDirs();

      // Use JNA to locate/extract the native binary, then register it with the JVM.
      final NativeLibrary library = NativeLibrary.getInstance(Core.NATIVE_LIBRARY_NAME);
      System.load(library.getFile().getAbsolutePath());

      logger.log(Level.FINEST, "OpenCV library \"{0}\" loaded via JNA from \"{1}\".",
          new Object[]{Core.NATIVE_LIBRARY_NAME, library.getFile().getAbsolutePath()});
    }

    private static class Holder {
      private static final SharedLoader INSTANCE = new SharedLoader();
    }

    public static SharedLoader getInstance() {
      return Holder.INSTANCE;
    }
  }

  /**
   * Exactly once per {@link ClassLoader}, extract the native binary from the
   * classpath using JNA and load it via {@link System#load(String)}.
   * Spurious calls are safe.
   */
  public static void loadLocally() {
    LocalLoader.getInstance();
  }

  private static class LocalLoader {

    private LocalLoader() {
      // Clean up temp dirs left by previous versions of this library.
      cleanupOldTempDirs();

      // Use JNA to locate/extract the native binary, then load it directly.
      final NativeLibrary library = NativeLibrary.getInstance(Core.NATIVE_LIBRARY_NAME);
      System.load(library.getFile().getAbsolutePath());

      logger.log(Level.FINEST, "OpenCV library \"{0}\" loaded locally via JNA from \"{1}\".",
          new Object[]{Core.NATIVE_LIBRARY_NAME, library.getFile().getAbsolutePath()});
    }

    private static class Holder {
      private static final LocalLoader INSTANCE = new LocalLoader();
    }

    public static LocalLoader getInstance() {
      return Holder.INSTANCE;
    }
  }
}
