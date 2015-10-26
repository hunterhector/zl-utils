package edu.cmu.cs.lti.learning.decoding;

import edu.cmu.cs.lti.learning.cache.CrfSequenceKey;
import edu.cmu.cs.lti.learning.model.*;
import edu.cmu.cs.lti.learning.training.SequenceDecoder;
import edu.cmu.cs.lti.utils.Functional;
import edu.cmu.cs.lti.utils.MultiStringDiskBackedCacher;
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

    private MultiStringDiskBackedCacher<FeatureVector[]> featureCacher;

    private int kBest;

    public ViterbiDecoder(FeatureAlphabet featureAlphabet, ClassAlphabet classAlphabet,
                          MultiStringDiskBackedCacher<FeatureVector[]> featureCacher) {
        this(featureAlphabet, classAlphabet, featureCacher, false);
    }

    public ViterbiDecoder(FeatureAlphabet featureAlphabet, ClassAlphabet classAlphabet,
                          MultiStringDiskBackedCacher<FeatureVector[]> featureCacher, boolean
                                  binaryFeature) {
        this(featureAlphabet, classAlphabet, featureCacher, binaryFeature, 1);
    }

    public ViterbiDecoder(FeatureAlphabet featureAlphabet, ClassAlphabet classAlphabet,
                          MultiStringDiskBackedCacher<FeatureVector[]> featureCacher, boolean
                                  binaryFeature, int kBest) {
        super(featureAlphabet, classAlphabet, binaryFeature);
        this.featureCacher = featureCacher;
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
            sequenceLength, double lagrangian, CrfSequenceKey key, boolean useAverage) {
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

        for (; !solution.finished(); solution.advance()) {
            int sequenceIndex = solution.getCurrentPosition();
            if (sequenceIndex == -1) {
                continue;
            }

            key.setTokenId(sequenceIndex);

            String[] multiKey = new String[]{key.getDocumentKey(), String.valueOf(key.getSequenceId()),
                    String.valueOf(sequenceIndex)};

            // Feature vector to be extracted or loaded from cache.
            FeatureVector nodeFeature;
            FeatureVector edgeFeature;

            FeatureVector[] allBaseFeatures = null;
            if (featureCacher != null) {
                allBaseFeatures = featureCacher.get(multiKey);
            }

            if (allBaseFeatures == null) {
                nodeFeature = newFeatureVector();
                edgeFeature = newFeatureVector();
                extractor.extract(sequenceIndex, nodeFeature, edgeFeature);
                if (featureCacher != null) {
                    featureCacher.addWithMultiKey(new FeatureVector[]{nodeFeature, edgeFeature}, multiKey);
                }
            } else {
                nodeFeature = allBaseFeatures[0];
                edgeFeature = allBaseFeatures[1];
            }

            // Before move on to calculate the features of current index, copy the vector of the previous column,
            // which are all candidates for the final feature of the prediction.
            System.arraycopy(currentFeatureVectors, 0, previousColFeatureVectors, 0, previousColFeatureVectors.length);

            for (int i = 0; i < currentFeatureVectors.length; i++) {
                currentFeatureVectors[i] = newGraphFeatureVector();
            }

            // Fill up lattice score for each of class in the current column.
            // TODO currently this creates a IllegalThreadState, some threads didn't exits.
            solution.getCurrentPossibleClassIndices().parallel().forEach(classIndex -> {
                double newNodeScore = nodeDotProd.apply(nodeFeature, classIndex);
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
            });
        }
        solution.backTrace();

        bestVector = currentFeatureVectors[classAlphabet.getOutsideClassIndex()];
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

        return fv;
    }
}
