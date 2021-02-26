package nu.pattern;

import org.opencv.core.Core;

public class PrintVersion {
    static {
        OpenCV.loadLocally();
    }
    
    public static void main(String[] args) {
        System.out.println(Core.getVersionString());
    }
}
