package edu.cmu.cs.lti.learning.model;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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

    private TObjectIntMap<String>[] featureCounters;

    private boolean storeReadable;

    private int alphabetSize = 0;

    private int hashMask;

    public Alphabet(int alphabetBits) {
        this(alphabetBits, false);
    }

    /**
     * Create a alphabet that also stores all feature names to integer id count. This will make the training about
     * 25% slower.
     *
     * @param alphabetBits  The power of 2 of this is the alphabet size, i.e. number of bits for feature.
     * @param storeReadable If this is true, it will create a alphabet that also stores all feature names to integer id
     *                      count. This will make the training about25% slower.
     */
    public Alphabet(int alphabetBits, boolean storeReadable) {
        if (alphabetBits >= 31) {
            throw new IllegalArgumentException("Alphabet size exceed the power of current Murmur");
        }

        int alphabetSize = (int) Math.pow(2, alphabetBits);

        this.storeReadable = true;
        this.alphabetSize = alphabetSize;
        this.hashMask = alphabetSize - 1;
        logger.info(String.format("Feature Alphabet initialized with size %d", alphabetSize));
        logger.info(String.format("Feature Mask is %s", Integer.toBinaryString(hashMask)));

        if (storeReadable) {
            logger.info("Alphabet will store feature name to hash value mappings.");
//            hashValueStore = new TObjectIntHashMap<>();
            featureCounters = new TObjectIntMap[alphabetSize];
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

    public String getMappedFeatureCounters(int featureIndex) {
        if (storeReadable) {
            TObjectIntMap counter = featureCounters[featureIndex];
            if (counter == null) {
                return null;
            }
            return counter.toString();
        } else {
            return null;
        }
    }

    public String[] getMappedFeatureNames(int featureIndex) {
        if (storeReadable) {
            TObjectIntMap<String> counter = featureCounters[featureIndex];
            if (featureCounters[featureIndex] == null) {
                return null;
            }
            return counter.keys(new String[counter.size()]);
        } else {
            return null;
        }
    }

    public void computeConflictRates() {
        if (storeReadable) {
            int actualFeatures = 0;
            int occupied = 0;
            for (TObjectIntMap featureCounter : featureCounters) {
                if (featureCounter != null) {
                    actualFeatures += featureCounter.size();
                    occupied += 1;
                }
            }
            logger.info(String.format("Actual features : %d, actual occupied,: %d, alphabet size : %d",
                    actualFeatures, occupied, alphabetSize));
        } else {
            throw new NotImplementedException();
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