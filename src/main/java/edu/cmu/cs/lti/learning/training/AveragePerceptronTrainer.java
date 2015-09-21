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
 *
 * @author Zhengzhong Liu
 */
public class AveragePerceptronTrainer {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private SequenceDecoder decoder;
    private GraphWeightVector weightVector;
    private double stepSize;

    private ClassAlphabet classAlphabet;

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
        this.classAlphabet = classAlphabet;
    }

    public double trainNext(SequenceSolution goldSolution, GraphFeatureVector goldFv, ChainFeatureExtractor extractor,
                            double lagrangian, CrfState key) {
        decoder.decode(extractor, weightVector, goldSolution.getSequenceLength(), lagrangian, key);
        SequenceSolution prediction = decoder.getDecodedPrediction();
        double loss = goldSolution.loss(prediction);

//        logger.debug("Gold:");
//        logger.debug(goldSolution.toString());
//        logger.debug("Prediction:");
//        logger.debug(prediction.toString());
//        logger.debug("Loss is " + loss);
//        DebugUtils.pause();

        if (loss != 0) {
            GraphFeatureVector bestDecodingFeatures = decoder.getBestDecodingFeatures();
            updateWeights(goldFv, bestDecodingFeatures);
        }
        return loss;
    }

    private void updateWeights(GraphFeatureVector goldFv, GraphFeatureVector predictedFv) {
//        logger.info("######################");
//        logger.info("Gold feature (node) is:");
//        logger.info(goldFv.readableNodeVector());
//
//        logger.info("######################");
//        logger.info("Prediction feature (node) is:");
//        logger.info(predictedFv.readableNodeVector());


//        logger.info("######################");
//        logger.info("Gold feature (edge) is:");
//        logger.info(goldFv.readableEdgeVector());
//
//        logger.info("######################");
//        logger.info("Prediction feature (edge) is:");
//        logger.info(predictedFv.readableEdgeVector());

//        GraphFeatureVector diffVector = goldFv.diff(predictedFv);
//        logger.debug("\n######################");
//        logger.debug("Update feature (node) is:");
//        logger.debug(diffVector.readableNodeVector());
//        logger.debug("######################\n");

//        logger.debug("\n######################");
//        logger.debug("Update by gold vector:");
        weightVector.updateWeightBy(goldFv, stepSize);
//        logger.debug("\n######################");
//        logger.debug("Update by prediction vector:");
        weightVector.updateWeightBy(predictedFv, -stepSize);

        weightVector.updateAverageWeights();
//        for (Iterator<Pair<Integer, AveragedWeightVector>> iter = weightVector.nodeWeightIterator(); iter.hasNext()
// ; ) {
//            Pair<Integer, AveragedWeightVector> r = iter.next();
//            logger.info("Features for class " + classAlphabet.getClassName(r.getValue0()));
//            for (int i = 0; i < r.getValue1().getFeatureSize(); i++) {
//                double w = r.getValue1().getWeightAt(i);
//                if (w > 0) {
//                    logger.info(goldFv.getClassAlphabet().getClassName(r.getValue0()) + "_" +
//                            goldFv.getFeatureAlphabet().getFeatureNameRepre(i) + "\t" + w);
//                }
//            }
//        }

//        DebugUtils.pause();
    }

    public void write(File outputFile) throws FileNotFoundException {
        weightVector.write(outputFile);
    }
}
