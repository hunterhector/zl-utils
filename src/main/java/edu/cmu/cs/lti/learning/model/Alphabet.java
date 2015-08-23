package edu.cmu.cs.lti.learning.model;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * Created with IntelliJ IDEA.
 * Date: 8/20/15
 * Time: 10:48 PM
 *
 * @author Zhengzhong Liu
 */
public class Alphabet {
    static HashFunction hasher = Hashing.murmur3_32();

    private int alphabetSize = 0;

    public Alphabet(int alphabet_size) {
        this.alphabetSize = alphabet_size;
    }

    public int hash(String feature) {
        return hasher.hashString(feature, Charsets.UTF_8).asInt(); // it is murmur32, we have 4 bytes, hence an Int.
    }

    public int getAlphabetSize() {
        return alphabetSize;
    }
}
