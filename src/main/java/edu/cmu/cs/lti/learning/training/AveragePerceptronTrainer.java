package edu.cmu.cs.lti.learning.training;

import edu.cmu.cs.lti.learning.cache.CrfState;
import edu.cmu.cs.lti.learning.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created with IntelliJ IDEA.
 * Date: 8/20/15
 * Time: 10:44 PM
 * <p>
 * Train an average perceptron model given a sequence decoder.
 *
 * @author Zhengzhong Liu
 */
public class AveragePerceptronTrainer {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private SequenceDecoder decoder;
    private GraphWeightVector weightVector;
    private double stepSize;


    /**
     * A vanilla average perceptron, with a fixed step size.
     *
     * @param decoder  The sequence decoder.
     * @param stepSize A fixed step size for each update.
     */
    public AveragePerceptronTrainer(SequenceDecoder decoder, ClassAlphabet classAlphabet, double stepSize,
                                    FeatureAlphabet alphabet) {
        this.decoder = decoder;
        weightVector = new GraphWeightVector(classAlphabet, alphabet);
        this.stepSize = stepSize;
    }

    public double trainNext(SequenceSolution goldSolution, GraphFeatureVector goldFv, ChainFeatureExtractor extractor,
                            double lagrangian, CrfState key) {
        decoder.decode(extractor, weightVector, goldSolution.getSequenceLength(), lagrangian, key);
        SequenceSolution prediction = decoder.getDecodedPrediction();
        double loss = goldSolution.loss(prediction);

        if (loss != 0) {
            GraphFeatureVector bestDecodingFeatures = decoder.getBestDecodingFeatures();
            updateWeights(goldFv, bestDecodingFeatures);
        }
        return loss;
    }

    private void updateWeights(GraphFeatureVector goldFv, GraphFeatureVector predictedFv) {
        weightVector.updateWeightsBy(goldFv, stepSize);
        weightVector.updateWeightsBy(predictedFv, -stepSize);

        weightVector.updateAverageWeights();
    }

    public void write(File outputFile) throws FileNotFoundException {
        weightVector.write(outputFile);
    }
}
