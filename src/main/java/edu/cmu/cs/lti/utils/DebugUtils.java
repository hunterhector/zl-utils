package edu.cmu.cs.lti.utils;

import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 11/20/14
 * Time: 3:43 PM
 */
public class DebugUtils {
    public static void printMemInfo(Logger logger) {
        printMemInfo(logger, "");
    }

    public static void printMemInfo(Logger logger, String msg) {
        // Get current size of heap in bytes
        double heapSize = Runtime.getRuntime().totalMemory() / (double) (1024 * 1024);

        //Get maximum size of heap in bytes. The heap cannot grow beyond this size.
        //Any attempt will result in an OutOfMemoryException.
        double heapMaxSize = Runtime.getRuntime().maxMemory() / (double) (1024 * 1024);

        // Get amount of free memory within the heap in bytes. This size will increase
        // after garbage collection and decrease as new objects are created.
        double heapFreeSize = Runtime.getRuntime().freeMemory() / (double) (1024 * 1024);

        logger.info(String.format("%s. Heap size: %.2f MB, Max Heap Size: %.2f MB, Free Heap Size: %.2f MB, Used Memory: %.2f MB", msg, heapSize, heapMaxSize, heapFreeSize, heapSize - heapFreeSize));
    }

    public static void pause() {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter to continue");
        in.nextLine();
    }
}