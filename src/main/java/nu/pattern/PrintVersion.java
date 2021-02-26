package nu.pattern;

import org.opencv.core.Core;

public class PrintVersion {
    static {
        OpenCV.loadLocally();
    }
    
    static void main(String[] args) {
        System.out.println(Core.getVersionString());
    }
}
