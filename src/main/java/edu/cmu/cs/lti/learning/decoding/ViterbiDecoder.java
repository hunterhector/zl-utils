package edu.cmu.cs.lti.learning.decoding;

import edu.cmu.cs.lti.learning.cache.CrfFeatureCacher;
import edu.cmu.cs.lti.learning.cache.CrfState;
import edu.cmu.cs.lti.learning.model.*;
import edu.cmu.cs.lti.learning.training.SequenceDecoder;
import edu.cmu.cs.lti.utils.Functional;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiFunction;

/**
 * Created with IntelliJ IDEA.
 * Date: 8/20/15
 * Time: 10:41 PM
 *
 * @author Zhengzhong Liu
 */
public class ViterbiDecoder extends SequenceDecoder {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private SequenceSolution solution;

    private GraphFeatureVector bestVector;

    private CrfFeatureCacher cacher;

    private int kBest;

    public ViterbiDecoder(FeatureAlphabet featureAlphabet, ClassAlphabet classAlphabet, CrfFeatureCacher cacher) {
        this(featureAlphabet, classAlphabet, cacher, false);
    }

    public ViterbiDecoder(FeatureAlphabet featureAlphabet, ClassAlphabet classAlphabet, CrfFeatureCacher cacher, boolean
            binaryFeature) {
        this(featureAlphabet, classAlphabet, cacher, binaryFeature, 1);
    }

    public ViterbiDecoder(FeatureAlphabet featureAlphabet, ClassAlphabet classAlphabet, CrfFeatureCacher cacher, boolean
            binaryFeature, int kBest) {
        super(featureAlphabet, classAlphabet, binaryFeature);
        this.cacher = cacher;
        this.kBest = kBest;
    }

    private FeatureVector newFeatureVector() {
        return useBinary ? new BinaryHashFeatureVector(featureAlphabet) : new RealValueHashFeatureVector
                (featureAlphabet);
    }

    private GraphFeatureVector newGraphFeatureVector() {
        return new GraphFeatureVector(classAlphabet, featureAlphabet, useBinary);
    }

    @Override
    public void decode(ChainFeatureExtractor extractor, GraphWeightVector weightVector, int
            sequenceLength, double lagrangian, CrfState key, boolean useAverage) {
        solution = new SequenceSolution(classAlphabet, sequenceLength, kBest);

        // Dot product function on the node (i.e. only take features depend on current class)
        BiFunction<FeatureVector, Integer, Double> nodeDotProd = useAverage ?
                weightVector::dotProdAver :
                weightVector::dotProd;

        // Dot product function on the edge (i.e. take features depend on two classes)
        Functional.TriFunction<FeatureVector, Integer, Integer, Double> edgeDotProd = useAverage ?
                weightVector::dotProdAver :
                weightVector::dotProd;

        final GraphFeatureVector[] currentFeatureVectors = new GraphFeatureVector[classAlphabet.size()];
        final GraphFeatureVector[] previousColFeatureVectors = new GraphFeatureVector[classAlphabet.size()];

        for (int i = 0; i < currentFeatureVectors.length; i++) {
            currentFeatureVectors[i] = newGraphFeatureVector();
        }

//        int[] nodeBasedDecoding = new int[solution.getSequenceLength()];

        for (; !solution.finished(); solution.advance()) {
            int sequenceIndex = solution.getCurrentPosition();
            if (sequenceIndex == -1) {
                continue;
            }

//            logger.debug("\n######## Decoding " + sequenceIndex + "\n");

            key.setTokenId(sequenceIndex);

            // Feature vector to be extracted or loaded from cache.
            FeatureVector nodeFeature;
            FeatureVector edgeFeature;

            FeatureVector[] allBaseFeatures = null;
            if (cacher != null) {
                allBaseFeatures = cacher.getCachedFeatures(key);
            }

            if (allBaseFeatures == null) {
                nodeFeature = newFeatureVector();
                edgeFeature = newFeatureVector();
                extractor.extract(sequenceIndex, nodeFeature, edgeFeature);
                if (cacher != null) {
                    cacher.addFeaturesToCache(key, nodeFeature, edgeFeature);
                }
            } else {
                nodeFeature = allBaseFeatures[0];
                edgeFeature = allBaseFeatures[1];
            }

            // Before move on to calculate the features of current index, copy the vector of the previous column,
            // which are all candidates for the final feature of the prediction.
            System.arraycopy(currentFeatureVectors, 0, previousColFeatureVectors, 0, previousColFeatureVectors.length);

            for (int i = 0; i < currentFeatureVectors.length; i++) {
//                logger.debug("Current features are : ");
//                logger.debug(currentFeatureVectors[i].readableNodeVector());
                currentFeatureVectors[i] = newGraphFeatureVector();
            }

//            DebugUtils.pause();

//            MutableDouble debugBestNodeScore = new MutableDouble();
//            MutableBoolean debugCurrentIsBest = new MutableBoolean();
//            debugBestNodeScore.setValue(Double.NEGATIVE_INFINITY);
//            debugCurrentIsBest.setFalse();

            // Fill up lattice score for each of class in the current column.
            solution.getCurrentPossibleClassIndices().forEach(classIndex -> {
//                logger.debug("\n#######Decoding class " + classAlphabet.getClassName(classIndex) + "\n");

                double newNodeScore = nodeDotProd.apply(nodeFeature, classIndex);

//                if (newNodeScore > debugBestNodeScore.getValue()) {
//                    logger.debug("New best node is " + classAlphabet.getClassName(classIndex) + " at " +
//                            sequenceIndex);
//                    debugBestNodeScore.setValue(newNodeScore);
//                    debugCurrentIsBest.setTrue();
//                    weightVector.dotProdVerbose(nodeFeature, classIndex);
//                    if (sequenceIndex < sequenceLength) {
//                        nodeBasedDecoding[sequenceIndex] = classIndex;
//                    }
//                } else {
//                    debugCurrentIsBest.setFalse();
//                }

                MutableInt argmaxPreviousState = new MutableInt(-1);

                // Check which previous state gives the best score.
                solution.getPreviousPossibleClassIndices().forEach(prevState -> {
                    for (SequenceSolution.LatticeCell previousBest : solution.getPreviousBests(prevState)) {
                        double newEdgeScore = 0;
                        if (classIndex == prevState) {
                            newEdgeScore = edgeDotProd.apply(edgeFeature, classIndex, prevState);
                        }

                        int addResult = solution.scoreNewEdge(classIndex, previousBest, newEdgeScore, newNodeScore);
                        if (addResult == 1) {
                            // The new score is the best.
                            argmaxPreviousState.setValue(prevState);
//                            if (debugCurrentIsBest.booleanValue()) {
//                                logger.info("For the class " + classAlphabet.getClassName(classIndex) + "  with max
// " +
//                                        "current node score : " + newNodeScore);
//                                logger.info("Found a better previous state at " + classAlphabet.getClassName
//                                        (prevState));
//                                logger.info("Because new edge score is " + newEdgeScore + " , new node score is " +
//                                        newNodeScore + " , previous cell score is " + previousBest.getScore() + " " +
//                                        "which make a total of " + (previousBest.getScore() + newEdgeScore +
//                                        newNodeScore));
//                            }

//                            if (newNodeScore != 0) {
//                                logger.debug("Previous : " + classAlphabet.getClassName(prevState) + " to current : "
//                                        + classAlphabet.getClassName(classIndex));
//                                logger.debug("Best prev state is now " + classAlphabet.getClassName(prevState) + "
// for "
//                                        + sequenceIndex);
//                                logger.debug("Prev state give score " + previousBest.getScore());
//                                logger.debug("New node score is " + newNodeScore);
////                                logger.debug("New Edge score from there is " + newEdgeScore);
//                                logger.debug("New total score is then " + (previousBest.getScore() + newEdgeScore +
//                                        newNodeScore));
//                            }

//                            logger.debug("Take a look at the edge weights:");
//                            AveragedWeightVector edgeWeights = weightVector.getEdgeWeights(classIndex, prevState);
//                            if (edgeWeights != null) {
//                                for (int i = 0; i < edgeWeights.getFeatureSize(); i++) {
//                                    double w = edgeWeights.getWeightAt(i);
//                                    if (w != 0) {
//                                        logger.debug("Weight at " +
//                                                featureAlphabet.getFeatureNameRepre(i) + " is " + w);
//                                    }
//                                }
//                            } else {
//                                logger.debug("There are no edge weight here.");
//                            }
                        } else if (addResult == -1) {
                            // The new score is worse than the worst, i.e. rejected by the heap. We don't
                            // need to check any scores that is worse than this.
                            break;
                        }
                    }
                });

                // Add feature vector from previous state, also added new features of current state.
                int bestPrev = argmaxPreviousState.getValue();

                // Adding features for the new cell.
                currentFeatureVectors[classIndex].extend(nodeFeature, classIndex);
                // Taking features from previous best cell.
                currentFeatureVectors[classIndex].extend(previousColFeatureVectors[bestPrev]);

                // Adding features for the edge.
                if (bestPrev == classIndex) {
                    // Note: additional condition that we are only interested in case where previous is the same.
                    currentFeatureVectors[classIndex].extend(edgeFeature, classIndex, bestPrev);
                }

//                DebugUtils.pause();
            });
        }
        solution.backTrace();
//        System.out.println(solution.showBestBackPointerMap());

//        boolean unmatchDecoding = false;
//        for (int i = 0; i < nodeBasedDecoding.length; i++) {
//            if (solution.getClassAt(i) != nodeBasedDecoding[i]) {
//                unmatchDecoding = true;
//            }
//        }
//
//        if (unmatchDecoding) {
//            logger.error("#########Node based decoding should match with backtrace with no edge features!");
//            logger.error("Backtracing Solution is ");
//            logger.error(solution.toString());
//            logger.error("Node based decoding result is :");
//            logger.error(IntStream.range(0, sequenceLength).mapToObj(solutionIndex -> solutionIndex + ":" +
//                    classAlphabet.getClassName(nodeBasedDecoding[solutionIndex])).collect(Collectors.joining(", ")));
//            DebugUtils.pause();
//        }


//        logger.info("Getting best feature vector from " + classAlphabet.getOutsideClassIndex());
        bestVector = currentFeatureVectors[classAlphabet.getOutsideClassIndex()];

//        logger.info(bestVector.readableNodeVector());

//        System.out.println(solution.showBestBackPointerMap());
//        DebugUtils.pause();
    }

    @Override
    public SequenceSolution getDecodedPrediction() {
        return solution;
    }

    @Override
    public GraphFeatureVector getBestDecodingFeatures() {
        return bestVector;
    }

    @Override
    public GraphFeatureVector getSolutionFeatures(ChainFeatureExtractor extractor, SequenceSolution solution) {
//        logger.info("Extracting solution features from data");

        GraphFeatureVector fv = newGraphFeatureVector();

        for (int solutionIndex = 0; solutionIndex <= solution.getSequenceLength(); solutionIndex++) {
            FeatureVector nodeFeatures = newFeatureVector();
            FeatureVector edgeFeatures = newFeatureVector();

            extractor.extract(solutionIndex, nodeFeatures, edgeFeatures);

            int classIndex = solution.getClassAt(solutionIndex);
            int previousStateIndex = solutionIndex == 0 ? classAlphabet.getOutsideClassIndex() : solution.getClassAt
                    (solutionIndex - 1);

            fv.extend(nodeFeatures, classIndex);

            // TODO be careful of this equality check.
            if (previousStateIndex == classIndex) {
                // Note: additional condition that we are only interested in case where previous is the same.
                fv.extend(edgeFeatures, classIndex, previousStateIndex);
            }
        }

//        logger.info("Done extracting solution feature");
        return fv;
    }
}
