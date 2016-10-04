package edu.cmu.cs.lti.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Date: 9/26/16
 * Time: 6:07 PM
 *
 * @author Zhengzhong Liu
 */
public class ResourceUtils {
    public static Map<String, String> readCilin(File cilinFile) {
        Map<String, String> word2CilinId = new HashMap<>();
        try {
            for (String line : org.apache.commons.io.FileUtils.readLines(cilinFile, "UTF-8")) {
                String[] fields = line.split("\\s");
                String entryId = fields[0];
                for (int i = 1; i < fields.length; i++) {
                    word2CilinId.put(fields[i], entryId);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return word2CilinId;
    }

}
