package edu.cmu.cs.lti.learning.decoding;

import edu.cmu.cs.lti.learning.cache.CrfFeatureCacher;
import edu.cmu.cs.lti.learning.cache.CrfState;
import edu.cmu.cs.lti.learning.model.*;
import edu.cmu.cs.lti.learning.training.SequenceDecoder;
import edu.cmu.cs.lti.utils.DebugUtils;
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

            // Raw feature vector extracted.
//            TObjectDoubleMap<String> featuresNoState;
//            TObjectDoubleMap<String> featuresNeedForState;

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
                currentFeatureVectors[i] = newBikeyFeatureVector();
            }

            // Fill up lattice score for each of class in the current column.
            solution.getCurrentPossibleClassIndices().forEach(classIndex -> {
                // Features that only depend on the current state.

                // Append state labels to raw features with lazy vector.
//                FeatureVector localStateFeatures = newLazyFeatureVector();
//                addLocalFeatures(featuresNoState, localStateFeatures, classIndex);

                double currentStateFeatureScore = useAverage ? weightVector.dotProdAver(featuresNoState,
                        classIndex) : weightVector.dotProd(featuresNoState, classIndex);

                if (currentStateFeatureScore != 0) {
                    logger.info("Score at class " + classIndex + " at " + sequenceIndex + " is " +
                            currentStateFeatureScore);
                }

                MutableInt argmaxPreviousState = new MutableInt(-1);
//                final FeatureVector[] bestStateDependentFeatures = {null};

                // Check which previous state gives the best score.
                solution.getPreviousPossibleClassIndices().forEach(prevState -> {
                    // Append state labels (including prev) to raw features with laze vector.
                    // NOTE: this inner loop is expensive, so we use lazy fv, and do not consolidate it. It is actually
                    // correct because features we read from a map are unique (without considering hash collisions)
//                    FeatureVector prevStateDependFeatures = newLazyFeatureVector();

                    // TODO this particular line is taking too much time.
//                    getPreviousStateDependentFeatures(featuresNeedForState, prevStateDependFeatures, prevState,
//                            classIndex);

                    for (SequenceSolution.LatticeCell previousBest : solution.getPreviousBests(prevState)) {
                        double newEdgeScore = useAverage ?
                                weightVector.dotProdAver(featuresNeedForState, classIndex, prevState) +
                                        currentStateFeatureScore :
                                weightVector.dotProd(featuresNeedForState, classIndex, prevState) +
                                        currentStateFeatureScore;

                        int addResult = solution.scoreNewEdge(classIndex, previousBest, newEdgeScore);
                        if (addResult == 1) {
                            // The new score is the best.
//                            bestStateDependentFeatures[0] = prevStateDependFeatures;
                            argmaxPreviousState.setValue(prevState);
                            logger.info("Best prev state is now " + classAlphabet.getClassName(prevState) + " for " +
                                    sequenceIndex);
                        } else if (addResult == -1) {
                            // The new score is worse than the worst, i.e. rejected by the heap. We don't
                            // need to check any scores that is worse than this.
                            break;
                        }
                    }
                });

                // Add feature vector from previous state, also added new features of current state.
                currentFeatureVectors[classIndex].extend(featuresNoState, classIndex);
                currentFeatureVectors[classIndex].extend(previousColFeatureVectors[argmaxPreviousState.getValue()]);
                currentFeatureVectors[classIndex].extend(featuresNeedForState, classIndex, argmaxPreviousState
                        .getValue());
//                logger.info("Setting current feature vector at " + sequenceIndex);
            });
        }
        solution.backTrace();
        DebugUtils.pause();

//        for (HashedFeatureVector currentFeatureVector : currentFeatureVectors) {
//            logger.info(currentFeatureVector.toString());
//        }

        bestVector = currentFeatureVectors[classAlphabet.getOutsideClassIndex()];

//        logger.info(bestVector.toString());

//        System.out.println(solution.showBestBackPointerMap());
//        DebugUtils.pause();

//        logger.info("Cumulative feature extraction time is " + featureStopWatch.getTime());
//        logger.info("Cumulative previous state feature label time is " + previousAddToDepWatch.getTime());
//        logger.info("Cumulative current state feature label time is " + localFeatureWatch.getTime());
//        logger.info("Cumulative add to previous state time is " + addToLazyVectorWatch.getTime());
//        logger.info("Previous label method calling count is " + previousLabelMethodCallCounter);
//        logger.info("Previous state execution count is " + previousLabelLoopCounter);

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

//    private void addLocalFeatures(TObjectDoubleMap<String> featuresNoState, FeatureVector localStateFeatures,
//                                  int classIndex) {
//        localFeatureWatch.resume();
////        String currentClass = classAlphabet.getClassName(classIndex);
//        for (String className : classAlphabet.getSplittedClassName(classIndex)) {
//            for (TObjectDoubleIterator<String> iter = featuresNoState.iterator(); iter.hasNext(); ) {
//                iter.advance();
//                localStateFeatures.addFeature(iter.key(), className, iter.value());
//            }
//        }
//        localFeatureWatch.suspend();
//    }
//
//    // This part is time consuming
//    private void getPreviousStateDependentFeatures(TObjectDoubleMap<String> featuresNeedForState, FeatureVector
//            prevStateDependFeatures, int prevState, int classIndex) {
//        previousAddToDepWatch.resume();
//        List<String> allPrevStateName = classAlphabet.getSplittedClassName(prevState);
//        List<String> allCurrentStateName = classAlphabet.getSplittedClassName(classIndex);
//
//        previousLabelMethodCallCounter++;
//
//        for (String prevStateName : allPrevStateName) {
//            // Conjoin features with previous state label only when previous is not "NoneOfAbove".
//            if (!prevStateName.equals(classAlphabet.getNoneOfTheAboveClass())) {
//                for (TObjectDoubleIterator<String> iter = featuresNeedForState.iterator(); iter.hasNext(); ) {
//                    iter.advance();
//                    for (String currentClass : allCurrentStateName) {
//                        addToLazyVectorWatch.resume();
////                        prevStateDependFeatures.addFeature(iter.key() + "_Si-1=" + prevStateName,
////                                currentClass, iter.value());
//                        // Features fire when this state is equal to previous state.
////                        if (prevStateName.equals(currentClass)) {
////                            prevStateDependFeatures.addFeature(iter.key() + "_Si-1=Si", currentClass, iter.value());
////                        }
//                        previousLabelLoopCounter++;
//                        addToLazyVectorWatch.suspend();
//                    }
//                }
//            }
//        }
//
//        previousAddToDepWatch.suspend();
//    }
}
