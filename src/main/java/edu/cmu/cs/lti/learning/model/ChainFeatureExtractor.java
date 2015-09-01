package edu.cmu.cs.lti.learning.model;

import gnu.trove.map.TObjectDoubleMap;

/**
 * Created with IntelliJ IDEA.
 * Date: 8/21/15
 * Time: 1:47 PM
 *
 * @author Zhengzhong Liu
 */
public abstract class ChainFeatureExtractor {
    public static final String START_CLASS = "<START>";

    public static final String END_CLASS = "<END>";

    Alphabet alphabet;

    public ChainFeatureExtractor(Alphabet alphabet) {
        this.alphabet = alphabet;
    }

    public abstract void extract(int focus, TObjectDoubleMap<String> features,
                                 TObjectDoubleMap<String> featuresNeedForState);

    public int getFeatureDimension() {
        return alphabet.getAlphabetSize();
    }
}
