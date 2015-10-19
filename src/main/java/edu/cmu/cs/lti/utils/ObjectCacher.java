package edu.cmu.cs.lti.utils;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * Date: 9/28/15
 * Time: 10:12 PM
 *
 * @author Zhengzhong Liu
 */
public class ObjectCacher {
    Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private File cachingDirectory;

    public ObjectCacher(File cachingDirectory) throws IOException {
        this.cachingDirectory = cachingDirectory;
        FileUtils.ensureDirectory(cachingDirectory);
        invalidate();
        logger.info("Cacher initialized at " + cachingDirectory.getAbsolutePath());
    }

    public <T extends Serializable> T loadCachedObject(String... keys) throws FileNotFoundException {
        File cachedFile = getCacheFile(keys);
        if (cachedFile.exists()) {
            return (T) SerializationUtils.deserialize(new FileInputStream(cachedFile));
        } else {
            return null;
        }
    }

    public <T extends Serializable> void writeCacheObject(T object, String... keys) throws FileNotFoundException {
        SerializationUtils.serialize(object, new FileOutputStream(getCacheFile(keys)));
    }

    private File getCacheFile(String... keys) {
        return new File(cachingDirectory, Joiner.on("_").join(keys));
    }

    public void invalidate() throws IOException {
        logger.info("Invalidating cache at " + cachingDirectory.getAbsolutePath());
        org.apache.commons.io.FileUtils.cleanDirectory(cachingDirectory);
    }
}
