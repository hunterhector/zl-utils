package edu.cmu.cs.lti.learning.model;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * Date: 8/20/15
 * Time: 10:48 PM
 *
 * @author Zhengzhong Liu
 */
public class Alphabet implements Serializable {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final long serialVersionUID = 8684276781666018103L;

    private HashFunction hasher = Hashing.murmur3_32();

//    private TObjectIntHashMap<String> hashValueStore;

    private TObjectIntMap[] featureCounters;

    private boolean storeReadable;

    private int alphabetSize = 0;

    private int hashMask;

    public Alphabet(int alphabet_size) {
        this(alphabet_size, false);
    }

    /**
     * Create a alphabet that also stores all feature names to integer id count. This will make the training about
     * 25% slower.
     *
     * @param alphabet_size
     * @param storeReadable
     */
    public Alphabet(int alphabet_size, boolean storeReadable) {
        if (alphabet_size >= Math.pow(2, 31)) {
            throw new IllegalArgumentException("Alphabet size exceed the power of current Murmur");
        }
        this.storeReadable = true;
        this.alphabetSize = alphabet_size;
        this.hashMask = alphabet_size - 1;
        logger.info(String.format("Feature Alphabet initialized with size %d", alphabet_size));

        if (storeReadable) {
            logger.info("Alphabet will store feature name to hash value mappings.");
//            hashValueStore = new TObjectIntHashMap<>();
            featureCounters = new TObjectIntMap[alphabet_size];
        }
    }

    public int hash(String feature) {
        // It is murmur32, which can produce a maximum 4 byte element.
        int hashVal = hasher.hashString(feature, Charsets.UTF_8).asInt() & hashMask;

//        logger.info("Hashing " + feature);
//        logger.info("Result " + hashVal);

        if (storeReadable) {
//            hashValueStore.put(feature, hashVal);
            TObjectIntMap<String> counter = featureCounters[hashVal];
            if (counter == null) {
                counter = new TObjectIntHashMap<>();
                featureCounters[hashVal] = counter;
            }
            counter.adjustOrPutValue("[" + feature + "]", 1, 1);
        }
        return hashVal;
    }

    public String getFeatureNames(int featureIndex) {
        if (storeReadable) {
            return featureCounters[featureIndex].toString();
        } else {
            return "";
        }
    }

    public boolean isStoreReadable() {
        return storeReadable;
    }

    public int getAlphabetSize() {
        return alphabetSize;
    }

    public void write(File outputFile) throws FileNotFoundException {
        SerializationUtils.serialize(this, new FileOutputStream(outputFile));
    }
}