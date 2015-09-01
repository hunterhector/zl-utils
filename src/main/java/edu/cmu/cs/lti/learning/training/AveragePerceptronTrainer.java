package edu.cmu.cs.lti.learning.training;

import edu.cmu.cs.lti.learning.cache.CrfState;
import edu.cmu.cs.lti.learning.model.*;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created with IntelliJ IDEA.
 * Date: 8/20/15
 * Time: 10:44 PM
 *
 * @author Zhengzhong Liu
 */
public class AveragePerceptronTrainer {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private SequenceDecoder decoder;
    private AveragedWeightVector averagedWeightVector;
    private double stepSize;

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

    public double trainNext(SequenceSolution goldSolution, HashedFeatureVector goldFv, ChainFeatureExtractor
            extractor, double lagrangian, CrfState key) {
        decoder.decode(extractor, averagedWeightVector, goldSolution.getSequenceLength(), lagrangian, key);
        SequenceSolution prediction = decoder.getDecodedPrediction();
        double loss = goldSolution.loss(prediction);

        if (loss != 0) {
            HashedFeatureVector bestDecodingFeatures = decoder.getBestDecodingFeatures();
            updateWeights(goldFv, bestDecodingFeatures);
        }
        return loss;
    }

    private void updateWeights(HashedFeatureVector goldFv, HashedFeatureVector predictedFv) {
        HashedFeatureVector diffVector;
        if (goldFv instanceof RealValueFeatureVector) {
            diffVector = new RealValueFeatureVector(goldFv.getAlphabet());
        } else {
            diffVector = new BinaryFeatureVector(goldFv.getAlphabet());
        }
        goldFv.diff(predictedFv, diffVector);

        averagedWeightVector.updateWeightsBy(diffVector, stepSize);

//        logger.info("Gold feature is:");
//        logger.info(goldFv.readableString());
//
//        logger.info("Prediction feature is:");
//        logger.info(predictedFv.readableString());
//
//        logger.info("Difference is :");
//        logger.info(diffVector.readableString());
//        DebugUtils.pause();

//        averagedWeightVector.updateWeightsBy(goldFv, stepSize);
//        averagedWeightVector.updateWeightsBy(predictedFv, -stepSize);
    }

    public void consolidate() {
        averagedWeightVector.consolidate();
    }

    public void write(File outputFile) throws FileNotFoundException {
        if (!averagedWeightVector.isReady()) {
            consolidate();
        }
        SerializationUtils.serialize(averagedWeightVector, new FileOutputStream(outputFile));
    }
}
