package edu.cmu.cs.lti.learning.training;

import edu.cmu.cs.lti.learning.model.*;

/**
 * Created with IntelliJ IDEA.
 * Date: 8/20/15
 * Time: 10:44 PM
 *
 * @author Zhengzhong Liu
 */
public class AveragePerceptronTrainer {
    SequenceDecoder decoder;
    AveragedWeightVector averagedWeightVector;
    double stepSize;

    /**
     * A vanilla average perceptron, with a fixed step size.
     *
     * @param decoder
     * @param stepSize
     */
    public AveragePerceptronTrainer(SequenceDecoder decoder, double stepSize, int featureDimension) {
        this.decoder = decoder;
        averagedWeightVector = new AveragedWeightVector(featureDimension);
        this.stepSize = stepSize;
    }

    public void trainNext(Solution goldSolution, HashedFeatureVector goldFv, ChainFeatureExtractor extractor,
                          int sequenceLength) {
        decoder.decode(extractor, averagedWeightVector, sequenceLength);
        SequenceSolution prediction = decoder.getDecodedPrediction();
        if (goldSolution.equals(prediction)) {
            updateWeights(goldFv, decoder.getBestDecodingFeatures());
        }
    }

    private void updateWeights(HashedFeatureVector goldFv, HashedFeatureVector predictedFv) {
        averagedWeightVector.updateWeightsBy(goldFv, 1);
        averagedWeightVector.updateWeightsBy(predictedFv, -1);
    }

    public void consolidate() {
        averagedWeightVector.consolidate();
    }
}
