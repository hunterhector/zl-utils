/**
 *
 */
package edu.cmu.cs.lti.utils;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author zhengzhongliu
 */
public class FileUtils {
    public static boolean ensureDirectory(String path) {
        File dir = new File(path);
        return ensureDirectory(dir);
    }

    public static boolean ensureDirectory(File dir) {
        if (!dir.exists()) {
            return dir.mkdirs();
        }
        return true;
    }

    public static File[] getFilesWithSuffix(File dir, final String suffix) {
        return dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(suffix);
            }
        });
    }
}
