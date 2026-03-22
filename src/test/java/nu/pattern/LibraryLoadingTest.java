package nu.pattern;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Verifies that the OpenCV native loader handles spurious calls safely.
 *
 * @see <a href="https://github.com/PatternConsulting/opencv/issues/7">Issue 7</a>
 */
@RunWith(JUnit4.class)
public class LibraryLoadingTest {

  /**
   * {@link OpenCV#loadLocally()} is safe to call repeatedly within a single
   * {@link ClassLoader} context.
   */
  @Test
  public void spuriousLoads() {
    OpenCV.loadLocally();
    OpenCV.loadLocally();
  }
}
