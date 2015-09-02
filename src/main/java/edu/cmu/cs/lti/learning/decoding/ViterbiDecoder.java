package edu.cmu.cs.lti.learning.decoding;

import edu.cmu.cs.lti.learning.cache.CrfFeatureCacher;
import edu.cmu.cs.lti.learning.cache.CrfState;
import edu.cmu.cs.lti.learning.model.*;
import edu.cmu.cs.lti.learning.training.SequenceDecoder;
import gnu.trove.iterator.TObjectDoubleIterator;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
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


    public ViterbiDecoder(Alphabet featureAlphabet, ClassAlphabet classAlphabet, CrfFeatureCacher cacher) {
        this(featureAlphabet, classAlphabet, cacher, false);
    }

    public ViterbiDecoder(Alphabet featureAlphabet, ClassAlphabet classAlphabet, CrfFeatureCacher cacher, boolean
            binaryFeature) {
        super(featureAlphabet, classAlphabet, binaryFeature);
        this.featureAlphabet = featureAlphabet;
        this.classAlphabet = classAlphabet;
        this.useBinary = binaryFeature;
        this.cacher = cacher;
    }

    private HashedFeatureVector newFeatureVector() {
        return useBinary ? new BinaryFeatureVector(featureAlphabet) : new RealValueFeatureVector(featureAlphabet);
    }

    @Override
    public void decode(ChainFeatureExtractor extractor, AveragedWeightVector averagedWeightVector, int
            sequenceLength, double lagrangian, CrfState key) {
        solution = new SequenceSolution(classAlphabet, sequenceLength);

        HashedFeatureVector[] currentFeatureVectors = new HashedFeatureVector[classAlphabet.size()];
        HashedFeatureVector[] previousColFeatureVectors = new HashedFeatureVector[currentFeatureVectors.length];

        for (int i = 0; i < currentFeatureVectors.length; i++) {
            currentFeatureVectors[i] = newFeatureVector();
        }

        for (; !solution.finished(); solution.advance()) {
            int sequenceIndex = solution.getCurrentPosition();
            key.setTokenId(sequenceIndex);

            // Initial start position.
            if (sequenceIndex == -1) {
                solution.setCurrentScoreAt(0, 0);
                continue;
            }

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
            currentFeatureVectors = new HashedFeatureVector[currentFeatureVectors.length];
            for (int i = 0; i < currentFeatureVectors.length; i++) {
                currentFeatureVectors[i] = newFeatureVector();
            }

            // Fill up lattice score for each of class in the current column.
            for (int classIndex = 0; classIndex < solution.getNumClasses(); classIndex++) {
                String currentClass = classAlphabet.getClassName(classIndex);

                double maxSequenceScoreTillHere = Double.NEGATIVE_INFINITY;
                int argmaxPreviousState = -1;
                HashedFeatureVector bestStateDependentFeatures = null;

                // Check which previous state gives the best score.
                // Note that only features depend on previous states will affect the results here.
                for (int prevState = 0; prevState < classAlphabet.size(); prevState++) {
                    HashedFeatureVector stateDependentFeatures = newFeatureVector();

                    String prevStateName = classAlphabet.getClassName(prevState); // more readable.

                    for (TObjectDoubleIterator<String> iter = featuresNeedForState.iterator(); iter.hasNext(); ) {
                        iter.advance();
                        stateDependentFeatures.addFeature(iter.key() + "_Si-1=" + prevStateName, currentClass, iter
                                .value());
                    }

                    // Current feature values do not affect choosing the best till here.
                    double newSequenceScoreTillHere = averagedWeightVector.dotProd(stateDependentFeatures) + solution
                            .getPreviousScore(prevState);

                    if (newSequenceScoreTillHere > maxSequenceScoreTillHere) {
                        maxSequenceScoreTillHere = newSequenceScoreTillHere;
                        argmaxPreviousState = prevState;
                        bestStateDependentFeatures = stateDependentFeatures;
                    }
                }

                // Features that are labelled with current class.
                HashedFeatureVector localStateFeatures = newFeatureVector();
                for (TObjectDoubleIterator<String> iter = featuresNoState.iterator(); iter.hasNext(); ) {
                    iter.advance();
                    localStateFeatures.addFeature(iter.key(), currentClass, iter.value());
                }

                double currentStateFeatureScore = averagedWeightVector.dotProd(localStateFeatures);

                currentFeatureVectors[classIndex].extend(previousColFeatureVectors[argmaxPreviousState]);
                currentFeatureVectors[classIndex].extend(bestStateDependentFeatures);
                currentFeatureVectors[classIndex].extend(localStateFeatures);

                solution.setCurrentScoreAt(classIndex, maxSequenceScoreTillHere + currentStateFeatureScore);
                solution.setBackpointer(classIndex, argmaxPreviousState);
            }
        }
        solution.backTrace();

        bestVector = currentFeatureVectors[solution.getClassAt(solution.getSequenceLength() - 1)];
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
