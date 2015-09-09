package edu.cmu.cs.lti.learning.decoding;

import edu.cmu.cs.lti.learning.cache.CrfFeatureCacher;
import edu.cmu.cs.lti.learning.cache.CrfState;
import edu.cmu.cs.lti.learning.model.*;
import edu.cmu.cs.lti.learning.training.SequenceDecoder;
import gnu.trove.iterator.TObjectDoubleIterator;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
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

    private HashedFeatureVector bestVector;

    private CrfFeatureCacher cacher;

    private int kBest;

    public ViterbiDecoder(Alphabet featureAlphabet, ClassAlphabet classAlphabet, CrfFeatureCacher cacher) {
        this(featureAlphabet, classAlphabet, cacher, false);
    }

    public ViterbiDecoder(Alphabet featureAlphabet, ClassAlphabet classAlphabet, CrfFeatureCacher cacher, boolean
            binaryFeature) {
        this(featureAlphabet, classAlphabet, cacher, binaryFeature, 1);
    }

    public ViterbiDecoder(Alphabet featureAlphabet, ClassAlphabet classAlphabet, CrfFeatureCacher cacher, boolean
            binaryFeature, int kBest) {
        super(featureAlphabet, classAlphabet, binaryFeature);
        this.cacher = cacher;
        this.kBest = kBest;
    }

    private HashedFeatureVector newFeatureVector() {
        return useBinary ? new BinaryFeatureVector(featureAlphabet) : new RealValueFeatureVector(featureAlphabet);
    }

    @Override
    public void decode(ChainFeatureExtractor extractor, AveragedWeightVector averagedWeightVector, int
            sequenceLength, double lagrangian, CrfState key) {
        solution = new SequenceSolution(classAlphabet, sequenceLength, kBest);

        final HashedFeatureVector[] currentFeatureVectors = new HashedFeatureVector[classAlphabet.size()];
        final HashedFeatureVector[] previousColFeatureVectors = new HashedFeatureVector[classAlphabet.size()];

        for (int i = 0; i < currentFeatureVectors.length; i++) {
            currentFeatureVectors[i] = newFeatureVector();
        }

        for (; !solution.finished(); solution.advance()) {
            int sequenceIndex = solution.getCurrentPosition();
            if (sequenceIndex == -1) {
                continue;
            }

//            logger.info("Decoding " + sequenceIndex);

            key.setTokenId(sequenceIndex);

            TObjectDoubleMap<String> featuresNoState;
            TObjectDoubleMap<String> featuresNeedForState;

            TObjectDoubleMap<String>[] allBaseFeatures = null;
            if (cacher != null) {
                allBaseFeatures = cacher.getCachedFeatures(key);
            }
            if (allBaseFeatures == null) {
                featuresNoState = new TObjectDoubleHashMap<>();
                featuresNeedForState = new TObjectDoubleHashMap<>();
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
                currentFeatureVectors[i] = newFeatureVector();
            }

            // Fill up lattice score for each of class in the current column.
            solution.getCurrentPossibleClassIndices().forEach(classIndex -> {
//                logger.info("Decoding class index " + classIndex);
                String currentClass = classAlphabet.getClassName(classIndex);

                // Features that only depend on the current state.
                HashedFeatureVector localStateFeatures = newFeatureVector();
                for (TObjectDoubleIterator<String> iter = featuresNoState.iterator(); iter.hasNext(); ) {
                    iter.advance();
                    localStateFeatures.addFeature(iter.key(), currentClass, iter.value());
                }
                double currentStateFeatureScore = averagedWeightVector.dotProd(localStateFeatures);
                currentFeatureVectors[classIndex].extend(localStateFeatures);

                MutableInt argmaxPreviousState = new MutableInt(-1);
                final HashedFeatureVector[] bestStateDependentFeatures = {null};

                // Check which previous state gives the best score.
                solution.getPreviousPossibleClassIndices().forEach(prevState -> {
//                    logger.info("Possible previous state is " + prevState + " for sequence " + sequenceIndex);

                    HashedFeatureVector prevStateDependFeatures = newFeatureVector();

                    String prevStateName = classAlphabet.getClassName(prevState); // more readable.

                    for (TObjectDoubleIterator<String> iter = featuresNeedForState.iterator(); iter.hasNext(); ) {
                        iter.advance();
                        prevStateDependFeatures.addFeature(iter.key() + "_Si-1=" + prevStateName, currentClass, iter
                                .value());
                    }

                    for (SequenceSolution.LatticeCell previousBest : solution.getPreviousBests(prevState)) {
                        double newEdgeScore = averagedWeightVector.dotProd(prevStateDependFeatures) +
                                currentStateFeatureScore;

                        int addResult = solution.scoreNewEdge(classIndex, previousBest, newEdgeScore);
                        if (addResult == 1) {
                            // The new score is the best.
                            bestStateDependentFeatures[0] = prevStateDependFeatures;
                            argmaxPreviousState.setValue(prevState);
//                            logger.info("Best prev state is now " + prevStateName);
                        } else if (addResult == -1) {
                            // The new score is worse than the worst, i.e. rejected by the heap. We don't
                            // need to check any scores that is worse than this.
                            break;
                        }
                    }
                });

                // Add feature vector from previous state, also added new features that depend on previous
                // state.
                currentFeatureVectors[classIndex].extend(previousColFeatureVectors[argmaxPreviousState.getValue()]);
                currentFeatureVectors[classIndex].extend(bestStateDependentFeatures[0]);
//                logger.info("Setting current feature vector at " + sequenceIndex);
            });
        }
        solution.backTrace();

//        for (HashedFeatureVector currentFeatureVector : currentFeatureVectors) {
//            logger.info(currentFeatureVector.toString());
//        }

        bestVector = currentFeatureVectors[classAlphabet.getOutsideClassIndex()];

//        logger.info(bestVector.toString());

//        System.out.println(solution.showBestBackPointerMap());
//        DebugUtils.pause();
    }

    @Override
    public SequenceSolution getDecodedPrediction() {
        return solution;
    }

    @Override
    public HashedFeatureVector getBestDecodingFeatures() {
        return bestVector;
    }

    @Override
    public HashedFeatureVector getSolutionFeatures(ChainFeatureExtractor extractor, SequenceSolution solution) {
        HashedFeatureVector fv = newFeatureVector();

        for (int i = 0; i < solution.getSequenceLength(); i++) {
            TObjectDoubleMap<String> featuresNoState = new TObjectDoubleHashMap<>();
            TObjectDoubleMap<String> featuresNeedForState = new TObjectDoubleHashMap<>();

            String currentClass = classAlphabet.getClassName(solution.getClassAt(i));

            String previousState = i == 0 ? classAlphabet.getOutsideClass() : classAlphabet.getClassName(solution
                    .getClassAt(i - 1));
            extractor.extract(i, featuresNoState, featuresNeedForState);

            for (TObjectDoubleIterator<String> iter = featuresNoState.iterator(); iter.hasNext(); ) {
                iter.advance();
                fv.addFeature(iter.key(), currentClass, iter.value());
            }

            for (TObjectDoubleIterator<String> iter = featuresNeedForState.iterator(); iter.hasNext(); ) {
                iter.advance();
                fv.addFeature(iter.key() + "_Si-1" + previousState, currentClass, iter.value());
            }
        }
        return fv;
    }
}
