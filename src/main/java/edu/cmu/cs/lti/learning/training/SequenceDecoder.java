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

    protected Alphabet featureAlphabet;

    protected boolean useBinary;

    private CrfState dummyKey = new CrfState();

    public SequenceDecoder(Alphabet featureAlphabet, ClassAlphabet classAlphabet) {
        this(featureAlphabet, classAlphabet, false);
    }

    public boolean usingBinaryFeature() {
        return useBinary;
    }

    public ClassAlphabet getClassAlphabet() {
        return classAlphabet;
    }

    public Alphabet getFeatureAlphabet() {
        return featureAlphabet;
    }

    public SequenceDecoder(Alphabet featureAlphabet, ClassAlphabet classAlphabet, boolean binaryFeature) {
        this.featureAlphabet = featureAlphabet;
        this.classAlphabet = classAlphabet;
        this.useBinary = binaryFeature;
    }


    public void decode(ChainFeatureExtractor extractor, AveragedWeightVector averagedWeightVector, int
            sequenceLength, double lagrangian) {
        decode(extractor, averagedWeightVector, sequenceLength, lagrangian, dummyKey);
    }

    public abstract void decode(ChainFeatureExtractor extractor, AveragedWeightVector weightVector, int
            sequenceLength, double lagrangian, CrfState key);

    public abstract SequenceSolution getDecodedPrediction();

    public abstract HashedFeatureVector getBestDecodingFeatures();

    public abstract HashedFeatureVector getSolutionFeatures(ChainFeatureExtractor extractor, SequenceSolution
            solution);
}