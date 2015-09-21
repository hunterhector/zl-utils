package edu.cmu.cs.lti.learning.training;

import edu.cmu.cs.lti.learning.cache.CrfState;
import edu.cmu.cs.lti.learning.model.*;

/**
 * Created with IntelliJ IDEA.
 * Date: 8/20/15
 * Time: 11:23 PM
 *
 * @author Zhengzhong Liu
 */
public abstract class SequenceDecoder {
    protected ClassAlphabet classAlphabet;

    protected FeatureAlphabet featureAlphabet;

    protected boolean useBinary;

    private CrfState dummyKey = new CrfState();

    public SequenceDecoder(HashAlphabet featureAlphabet, ClassAlphabet classAlphabet) {
        this(featureAlphabet, classAlphabet, false);
    }

    public boolean usingBinaryFeature() {
        return useBinary;
    }

    public ClassAlphabet getClassAlphabet() {
        return classAlphabet;
    }

    public FeatureAlphabet getFeatureAlphabet() {
        return featureAlphabet;
    }

    public SequenceDecoder(FeatureAlphabet featureAlphabet, ClassAlphabet classAlphabet, boolean binaryFeature) {
        this.featureAlphabet = featureAlphabet;
        this.classAlphabet = classAlphabet;
        this.useBinary = binaryFeature;
    }

    public void decode(ChainFeatureExtractor extractor, GraphWeightVector weightVector, int
            sequenceLength, double lagrangian, boolean useAverage) {
        decode(extractor, weightVector, sequenceLength, lagrangian, dummyKey, useAverage);
    }

    public void decode(ChainFeatureExtractor extractor, GraphWeightVector weightVector, int
            sequenceLength, double lagrangian, CrfState key) {
        decode(extractor, weightVector, sequenceLength, lagrangian, key, false);
    }

    public abstract void decode(ChainFeatureExtractor extractor, GraphWeightVector weightVector, int
            sequenceLength, double lagrangian, CrfState key, boolean useAverage);

    public abstract SequenceSolution getDecodedPrediction();

    public abstract GraphFeatureVector getBestDecodingFeatures();

    public abstract GraphFeatureVector getSolutionFeatures(ChainFeatureExtractor extractor, SequenceSolution
            solution);
}