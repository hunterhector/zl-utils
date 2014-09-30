/**
 * 
 */
package edu.cmu.cs.lti.utils;

import java.io.File;

/**
 * @author zhengzhongliu
 * 
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
}
