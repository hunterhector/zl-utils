package edu.cmu.cs.lti.learning.model;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Date: 8/21/15
 * Time: 1:47 PM
 *
 * @author Zhengzhong Liu
 */
public abstract class ChainFeatureExtractor {
    protected FeatureAlphabet alphabet;

    public ChainFeatureExtractor(FeatureAlphabet alphabet) {
        this.alphabet = alphabet;
    }

    public void extract(int focus, FeatureVector nodeFeatures) {
        extract(focus, nodeFeatures, HashBasedTable.create());
    }

    public abstract void extract(int focus, FeatureVector nodeFeatures,
                                 Table<Integer, Integer, FeatureVector> edgeFeatures);

    public abstract void extractGlobal(int focus, FeatureVector globalFeatures, Map<Integer, String> knownStates);

}
