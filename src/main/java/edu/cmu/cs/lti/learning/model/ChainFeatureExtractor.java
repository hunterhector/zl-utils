package edu.cmu.cs.lti.learning.model;

/**
 * Created with IntelliJ IDEA.
 * Date: 8/21/15
 * Time: 1:47 PM
 *
 * @author Zhengzhong Liu
 */
public abstract class ChainFeatureExtractor {
    Alphabet alphabet;

    public ChainFeatureExtractor(Alphabet alphabet) {
        this.alphabet = alphabet;
    }

    public abstract HashedFeatureVector extract(int focus, int previousStateValue);

    public int getFeatureDimension() {
        return alphabet.getAlphabetSize();
    }
}
