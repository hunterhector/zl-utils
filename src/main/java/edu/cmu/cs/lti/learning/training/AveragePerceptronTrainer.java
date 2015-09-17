package edu.cmu.cs.lti.learning.training;

import edu.cmu.cs.lti.learning.cache.CrfState;
import edu.cmu.cs.lti.learning.model.*;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;

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
    private BiKeyWeightVector weightVector;
    private double stepSize;


    private StopWatch decodeStopwatch = new StopWatch();

    /**
     * A vanilla average perceptron, with a fixed step size.
     *
     * @param decoder
     * @param stepSize
     */
    public AveragePerceptronTrainer(SequenceDecoder decoder, ClassAlphabet classAlphabet, double stepSize, int
            featureDimension) {
        this.decoder = decoder;
        weightVector = new BiKeyWeightVector(classAlphabet, featureDimension);
        this.stepSize = stepSize;

        decodeStopwatch.start();
        decodeStopwatch.suspend();
    }

    public double trainNext(SequenceSolution goldSolution, BiKeyFeatureVector goldFv, ChainFeatureExtractor extractor,
                            double lagrangian, CrfState key) {
        decodeStopwatch.resume();
        decoder.decode(extractor, weightVector, goldSolution.getSequenceLength(), lagrangian, key);
        decodeStopwatch.suspend();
//        logger.info("Cumulative decode time is " + decodeStopwatch.getTime());
        SequenceSolution prediction = decoder.getDecodedPrediction();
        double loss = goldSolution.loss(prediction);

        logger.debug(goldSolution.toString());
        logger.debug(prediction.toString());
//
//        DebugUtils.pause();

        if (loss != 0) {
            BiKeyFeatureVector bestDecodingFeatures = decoder.getBestDecodingFeatures();
            updateWeights(goldFv, bestDecodingFeatures);
        }
        return loss;
    }

    private void updateWeights(BiKeyFeatureVector goldFv, BiKeyFeatureVector predictedFv) {
        weightVector.updateWeightBy(goldFv, stepSize);
        weightVector.updateWeightBy(predictedFv, -stepSize);


//        logger.info("Gold feature is:");
//        logger.info(goldFv.readableString());
//
//        logger.info("Prediction feature is:");
//        logger.info(predictedFv.readableString());
//
//        logger.info("Difference is :");
//        logger.info(diffVector.readableString());
//        DebugUtils.pause();
    }

    public void write(File outputFile) throws FileNotFoundException {
        weightVector.write(outputFile);
    }
}
