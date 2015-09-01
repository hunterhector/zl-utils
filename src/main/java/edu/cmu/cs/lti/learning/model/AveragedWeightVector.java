package edu.cmu.cs.lti.learning.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * Date: 8/21/15
 * Time: 9:19 PM
 *
 * @author Zhengzhong Liu
 */
public class AveragedWeightVector implements Serializable {
    private static final long serialVersionUID = 7646416117744167293L;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    double[] weights;
    double[] averagedWeights;
    int updateCounts;

    boolean isReady;

    public AveragedWeightVector(int featureSize) {
        weights = new double[featureSize];
        averagedWeights = new double[featureSize];
        updateCounts = 0;
    }

    public void updateWeightsBy(HashedFeatureVector fv, double multiplier) {
        if (isReady) {
            throw new IllegalStateException("Model is consolidated, weights cannot be updated.");
        }

        for (HashedFeatureVector.FeatureIterator iter = fv.featureIterator(); iter.hasNext(); ) {
            iter.next();
            int index = iter.featureIndex();
            weights[index] = iter.featureValue() * multiplier;
            averagedWeights[index] += weights[index];
        }
        updateCounts++;
    }

    public void consolidate() {
        for (int i = 0; i < averagedWeights.length; i++) {
            averagedWeights[i] /= updateCounts;
        }
        isReady = true;
    }

    public double dotProd(HashedFeatureVector fv) {
        double sum = 0;
        for (HashedFeatureVector.FeatureIterator iter = fv.featureIterator(); iter.hasNext(); ) {
            iter.next();
            sum += averagedWeights[iter.featureIndex()] * iter.featureValue();
        }
        return sum;
    }

    public boolean isReady() {
        return isReady;
    }

}
