package edu.cmu.cs.lti.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 2/25/14
 * Time: 1:17 AM
 */
public class Configuration {
    private final static Logger LOGGER = Logger.getLogger(Configuration.class.getName());

    private File configFile;
    private Properties properties;


    public Configuration(String configurationFilePath) throws IOException {
        this(new File(configurationFilePath));
    }

    public Configuration(File configurationFile) throws IOException {
        configFile = configurationFile;
        if (!configFile.exists()) {
            throw new IOException("Cannot read config file at : " + configFile.getCanonicalPath());
        }
        properties = new Properties();
        properties.load(new FileInputStream(configurationFile));
    }

    public String getOrElse(String key, String defaultValue) {
        String value = properties.getProperty(key, defaultValue);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public String get(String key) {
        return getOrElse(key, null);
    }

    public boolean getBoolean(String key, boolean defaultVal) {
        String value = getOrElse(key, null);
        if (value == null) {
            return defaultVal;
        }
        return value.equals("true");
    }

    public int getInt(String key, int defaultVal) {
        String value = get(key);
        if (value == null) {
            return defaultVal;
        }
        return Integer.parseInt(value);
    }

    public double getDouble(String key, double defaultVal) {
        String val = get(key);
        if (val != null) {
            return Double.parseDouble(get(key));
        } else {
            return defaultVal;
        }
    }

    public File getFile(String key) {
        String val = get(key);
        return new File(val);
    }

    public String[] getList(String key) {
        String value = get(key);
        if (value == null) {
            return new String[0];
        }

        return value.split("[,\\s]+");
    }

    public int[] getIntList(String key) {
        String value = get(key);
        if (value == null) {
            return new int[0];
        }

        String[] strs = value.split("[,\\s]+");

        int[] results = new int[strs.length];

        for (int i = 0; i < strs.length; i++) {
            results[i] = Integer.parseInt(strs[i]);
        }

        return results;
    }

}