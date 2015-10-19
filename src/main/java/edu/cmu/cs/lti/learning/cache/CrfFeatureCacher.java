package edu.cmu.cs.lti.learning.cache;

import edu.cmu.cs.lti.learning.model.FeatureVector;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * Date: 8/25/15
 * Time: 1:37 PM
 *
 * @author Zhengzhong Liu
 */
public class CrfFeatureCacher extends FeatureCacher {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static String normal_suffix = "_normal";

    // Layout of this :
    // Table map from <sequence id, token id> to <feature no state, features need state>.
    private HashMap<Pair<Integer, Integer>, FeatureVector[]> featuresOfDocument;

    private String currentDocumentKey = null;

    private File featureCacheDirectory;

    public CrfFeatureCacher(File cachingDirectory) throws IOException {
        featureCacheDirectory = cachingDirectory;
        featuresOfDocument = new HashMap<>();

        // I am paranoid, so always invalidate existing cache.
        invalidate();

        if (!featureCacheDirectory.exists()) {
            featureCacheDirectory.mkdirs();
        } else if (!featureCacheDirectory.isDirectory()) {
            throw new IllegalArgumentException(String.format("Caching path is not a directory : %s",
                    featureCacheDirectory.getAbsolutePath()));
        }

        logger.debug("Cache directory is " + featureCacheDirectory.getAbsolutePath());
    }

    public void flush(String documentKey) throws FileNotFoundException {
        File documentCache = new File(featureCacheDirectory, documentKey + normal_suffix);

        if (!documentCache.exists()) {
            SerializationUtils.serialize(featuresOfDocument, new FileOutputStream(documentCache));
            featuresOfDocument = new HashMap<>();
        }
    }

    @Override
    public FeatureVector[] getCachedFeatures(FeatureCacheKey key) {
//        logger.debug(String.format("Loading feature from %s", key.toString()));
        if (key instanceof CrfState) {
            CrfState k = (CrfState) key;
            return getCachedFV(k.getDocumentKey(), k.getSequenceId(), k.getTokenId());
        }
        return null;
    }

    private FeatureVector[] getCachedFV(String documentKey, int sequenceId, int tokenId) {
        if (loadDocumentFeatures(documentKey)) {
            return featuresOfDocument.get(Pair.with(sequenceId, tokenId));
        }
        return null;
    }

    @Override
    public void addFeaturesToCache(FeatureCacheKey key, FeatureVector... features) {
        if (key instanceof CrfState) {
            CrfState k = (CrfState) key;
            featuresOfDocument.put(Pair.with(k.getSequenceId(), k.getTokenId()), features);
        }
    }

    private boolean loadDocumentFeatures(String documentKey) {
        // If not loaded or loaded a different document
        if (currentDocumentKey == null || !currentDocumentKey.equals(documentKey)) {
            if (loadFeaturesFromFile(documentKey)) {
                // If loaded success.
                currentDocumentKey = documentKey;
                return true;
            } else {
                return false;
            }
        }

        // Loaded with this document.
        return true;
    }

    private boolean loadFeaturesFromFile(String documentKey) {
        File documentCache = new File(featureCacheDirectory, documentKey + normal_suffix);
//        logger.debug("Try to load from file " + documentCache.getAbsolutePath());
        if (documentCache.exists()) {
//            logger.debug("Cache exists.");
            try {
                featuresOfDocument = SerializationUtils.deserialize(new FileInputStream(documentCache));
                return true;
            } catch (FileNotFoundException e) {
                // Probably not gonna happen unless someone delete it while reading.
                return false;
            }
        }
        // Load failed because file not exists.
        return false;
    }

    public void invalidate() throws IOException {
        if (featureCacheDirectory.exists()) {
            FileUtils.deleteDirectory(featureCacheDirectory);
            logger.info("Cache invalidated.");
        }
    }
}
