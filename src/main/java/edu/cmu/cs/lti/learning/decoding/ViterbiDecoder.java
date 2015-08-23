package edu.cmu.cs.lti.learning.decoding;

import edu.cmu.cs.lti.learning.model.*;
import edu.cmu.cs.lti.learning.training.SequenceDecoder;

/**
 * Created with IntelliJ IDEA.
 * Date: 8/20/15
 * Time: 10:41 PM
 *
 * @author Zhengzhong Liu
 */
public class ViterbiDecoder extends SequenceDecoder {
    ClassAlphabet classAlphabet;

    Alphabet featureAlphabet;

    SequenceSolution solution;

    HashedFeatureVector bestVector;

    boolean useBinary;

    public ViterbiDecoder(Alphabet featureAlphabet, ClassAlphabet classAlphabet) {
        this(featureAlphabet, classAlphabet, false);
    }

    public ViterbiDecoder(Alphabet featureAlphabet, ClassAlphabet classAlphabet, boolean binaryFeature) {
        this.featureAlphabet = featureAlphabet;
        this.classAlphabet = classAlphabet;
        this.useBinary = binaryFeature;
    }

    @Override
    public void decode(ChainFeatureExtractor extractor, AveragedWeightVector averagedWeightVector, int
            sequenceLength, double lagrangian) {
        solution = new SequenceSolution(classAlphabet, sequenceLength);

        HashedFeatureVector[] bestVectors = new HashedFeatureVector[classAlphabet.size()];
        for (int i = 0; i < bestVectors.length; i++) {
            if (useBinary) {
                bestVectors[i] = new RealValueFeatureVector(featureAlphabet);
            } else {
                bestVectors[i] = new BinaryFeatureVector(extractor.getFeatureDimension());
            }
        }

        for (; !solution.finished(); solution.advance()) {
            int sequenceIndex = solution.getCurrentPosition();

            // Initial start position.
            if (sequenceIndex == -1) {
                solution.setCurrentScoreAt(0, 0);
                continue;
            }

            // Fill up lattice.
            for (int classIndex = 0; classIndex < solution.getNumClasses(); classIndex++) {
                double maxNewSequenceScore = -Double.NEGATIVE_INFINITY;
                int argmaxSequencePrevious = -1;
                HashedFeatureVector bestNewFeatures = null;
                for (int prevState = 0; prevState < classAlphabet.size(); prevState++) {
                    HashedFeatureVector newFeatures = extractor.extract(sequenceIndex, prevState);
                    double newScore = averagedWeightVector.dotProd(newFeatures);
                    double newSequenceScore = solution.getPreviousScore(prevState) + newScore;
                    if (newSequenceScore > maxNewSequenceScore) {
                        maxNewSequenceScore = newSequenceScore;
                        argmaxSequencePrevious = prevState;
                        bestNewFeatures = newFeatures;
                    }
                }

                bestVectors[classIndex].extend(bestNewFeatures);
                solution.setCurrentScoreAt(classIndex, maxNewSequenceScore);
                solution.setBackpointer(classIndex, argmaxSequencePrevious);
            }
        }
        solution.backTrace();
        bestVector = bestVectors[solution.getClassAt(solution.getSequenceLength() - 1)];
    }

    @Override
    public SequenceSolution getDecodedPrediction() {
        return solution;
    }

    @Override
    public HashedFeatureVector getBestDecodingFeatures() {
        return bestVector;
    }
}
