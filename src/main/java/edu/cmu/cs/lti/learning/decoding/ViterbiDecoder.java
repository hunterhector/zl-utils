package edu.cmu.cs.lti.learning.decoding;

import edu.cmu.cs.lti.learning.cache.CrfFeatureCacher;
import edu.cmu.cs.lti.learning.cache.CrfState;
import edu.cmu.cs.lti.learning.model.*;
import edu.cmu.cs.lti.learning.training.SequenceDecoder;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private BiKeyFeatureVector bestVector;

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

    private BiKeyFeatureVector newBikeyFeatureVector() {
        return new BiKeyFeatureVector(classAlphabet, featureAlphabet, useBinary);
    }

    @Override
    public void decode(ChainFeatureExtractor extractor, BiKeyWeightVector weightVector, int
            sequenceLength, double lagrangian, CrfState key, boolean useAverage) {
        solution = new SequenceSolution(classAlphabet, sequenceLength, kBest);

        final BiKeyFeatureVector[] currentFeatureVectors = new BiKeyFeatureVector[classAlphabet.size()];
        final BiKeyFeatureVector[] previousColFeatureVectors = new BiKeyFeatureVector[classAlphabet.size()];

        for (int i = 0; i < currentFeatureVectors.length; i++) {
            currentFeatureVectors[i] = newBikeyFeatureVector();
        }

        for (; !solution.finished(); solution.advance()) {
            int sequenceIndex = solution.getCurrentPosition();
            if (sequenceIndex == -1) {
                continue;
            }

            key.setTokenId(sequenceIndex);

            // Feature vector to be extracted or loaded from cache.
            FeatureVector featuresNoState;
            FeatureVector featuresNeedForState;

            FeatureVector[] allBaseFeatures = null;
            if (cacher != null) {
                allBaseFeatures = cacher.getCachedFeatures(key);
            }
            if (allBaseFeatures == null) {
                featuresNoState = newFeatureVector();
                featuresNeedForState = newFeatureVector();
                extractor.extract(sequenceIndex, featuresNoState, featuresNeedForState);
                if (cacher != null) {
                    cacher.addFeaturesToCache(key, featuresNoState, featuresNeedForState);
                }
            } else {
                featuresNoState = allBaseFeatures[0];
                featuresNeedForState = allBaseFeatures[1];
            }

            System.arraycopy(currentFeatureVectors, 0, previousColFeatureVectors, 0, previousColFeatureVectors.length);

            for (int i = 0; i < currentFeatureVectors.length; i++) {
//                logger.info("Current features are : ");
//                logger.info(currentFeatureVectors[i].readableUniVector());
                currentFeatureVectors[i] = newBikeyFeatureVector();
            }

//            DebugUtils.pause();

            // Fill up lattice score for each of class in the current column.
            solution.getCurrentPossibleClassIndices().forEach(classIndex -> {
                double currentStateFeatureScore = useAverage
                        ? weightVector.dotProdAver(featuresNoState, classIndex)
                        : weightVector.dotProd(featuresNoState, classIndex);

//                if (currentStateFeatureScore != 0) {
//                    logger.info("Score at class " + classIndex + " at " + sequenceIndex + " is " +
//                            currentStateFeatureScore);
//                }

                MutableInt argmaxPreviousState = new MutableInt(-1);

                // Check which previous state gives the best score.
                solution.getPreviousPossibleClassIndices().forEach(prevState -> {
                    for (SequenceSolution.LatticeCell previousBest : solution.getPreviousBests(prevState)) {
                        double newEdgeScore = useAverage ?
                                weightVector.dotProdAver(featuresNeedForState, classIndex, prevState) +
                                        currentStateFeatureScore :
                                weightVector.dotProd(featuresNeedForState, classIndex, prevState) +
                                        currentStateFeatureScore;

                        int addResult = solution.scoreNewEdge(classIndex, previousBest, newEdgeScore);
                        if (addResult == 1) {
                            // The new score is the best.
                            argmaxPreviousState.setValue(prevState);
//                            logger.info("Best prev state is now " + classAlphabet.getClassName(prevState) + " for " +
//                                    sequenceIndex);
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
                currentFeatureVectors[classIndex].extend(featuresNoState, classIndex);

                // Adding features for the edge.
                currentFeatureVectors[classIndex].extend(featuresNeedForState, classIndex, bestPrev);

                // Taking features from previous best cell.
                currentFeatureVectors[classIndex].extend(previousColFeatureVectors[bestPrev]);

//                if (sequenceIndex == 1) {
//                    logger.info("Setting ");
//                }

//                logger.info("Setting current feature vector at " + sequenceIndex + " for class " + classIndex);
//                if (sequenceIndex == sequenceLength) {
//                    logger.info(currentFeatureVectors[classIndex].readableUniVector());
//                }

//                if (sequenceIndex == sequenceLength) {
//                    logger.info("Last previous features are assigned form " + bestPrev + " these are : ");
//                    logger.info(previousColFeatureVectors[bestPrev].readableUniVector());
//                }

//                logger.info(currentFeatureVectors[classIndex].readableUniVector());
//                DebugUtils.pause();
            });
        }
        solution.backTrace();

//        for (BiKeyFeatureVector currentFeatureVector : currentFeatureVectors) {
//            logger.info(currentFeatureVector.readableUniVector());
//        }

//        logger.info("Getting best feature vector from " + classAlphabet.getOutsideClassIndex());
        bestVector = currentFeatureVectors[classAlphabet.getOutsideClassIndex()];

//        logger.info(bestVector.readableUniVector());

//        System.out.println(solution.showBestBackPointerMap());
//        DebugUtils.pause();
    }

    @Override
    public SequenceSolution getDecodedPrediction() {
        return solution;
    }

    @Override
    public BiKeyFeatureVector getBestDecodingFeatures() {
        return bestVector;
    }

    @Override
    public BiKeyFeatureVector getSolutionFeatures(ChainFeatureExtractor extractor, SequenceSolution solution) {
//        logger.info("Extracting solution features from data");

        BiKeyFeatureVector fv = newBikeyFeatureVector();

        for (int i = 0; i < solution.getSequenceLength(); i++) {
            FeatureVector featuresNoState = newFeatureVector();
            FeatureVector featuresNeedForState = newFeatureVector();

            extractor.extract(i, featuresNoState, featuresNeedForState);

            int classIndex = solution.getClassAt(i);
            int previousStateIndex = i == 0 ? classAlphabet.getOutsideClassIndex() : solution.getClassAt(i - 1);

            fv.extend(featuresNoState, classIndex);
            fv.extend(featuresNeedForState, previousStateIndex, classIndex);
        }

//        logger.info("Done extracting solution feature");
        return fv;
    }
}
