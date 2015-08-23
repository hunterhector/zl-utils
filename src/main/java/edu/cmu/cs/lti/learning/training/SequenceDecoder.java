package edu.cmu.cs.lti.learning.training;

import edu.cmu.cs.lti.learning.model.AveragedWeightVector;
import edu.cmu.cs.lti.learning.model.ChainFeatureExtractor;
import edu.cmu.cs.lti.learning.model.HashedFeatureVector;
import edu.cmu.cs.lti.learning.model.SequenceSolution;

/**
 * Created with IntelliJ IDEA.
 * Date: 8/20/15
 * Time: 11:23 PM
 *
 * @author Zhengzhong Liu
 */
public abstract class SequenceDecoder {
    public void decode(ChainFeatureExtractor extractor, AveragedWeightVector weightVector, int sequenceLength) {
        decode(extractor, weightVector, sequenceLength, 0);
    }

    public abstract void decode(ChainFeatureExtractor extractor, AveragedWeightVector weightVector, int
            sequenceLength, double lagrangian);

    public abstract SequenceSolution getDecodedPrediction();

    public abstract HashedFeatureVector getBestDecodingFeatures();
}