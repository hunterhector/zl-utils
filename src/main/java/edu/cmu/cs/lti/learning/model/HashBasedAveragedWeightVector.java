package edu.cmu.cs.lti.learning.model;

import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.hash.TIntDoubleHashMap;

/**
 * Created with IntelliJ IDEA.
 * Date: 8/21/15
 * Time: 9:19 PM
 *
 * @author Zhengzhong Liu
 */
public class HashBasedAveragedWeightVector extends AveragedWeightVector {
    private static final long serialVersionUID = 7646416117744167293L;

    // Individual update count of this vector.
    private int averageUpdateCount;

    // Weights vector hold all normal weights.
    private TIntDoubleMap weights;

    // Average weight vector actually holds sum of weights, unless "consolidated".
    private TIntDoubleMap averagedWeights;

    private boolean consolidated;

    public HashBasedAveragedWeightVector() {
        this(0);
    }

    public HashBasedAveragedWeightVector(int initialAverageUpdateCount) {
        weights = new TIntDoubleHashMap();
        averagedWeights = new TIntDoubleHashMap();
        consolidated = false;
        averageUpdateCount = initialAverageUpdateCount;
    }

    @Override
    public void updateWeightsBy(FeatureVector fv, double multiplier) {
        for (FeatureVector.FeatureIterator iter = fv.featureIterator(); iter.hasNext(); ) {
            iter.next();
            int index = iter.featureIndex();
            double updateAmount = iter.featureValue() * multiplier;
            weights.adjustOrPutValue(index, updateAmount, updateAmount);
        }
    }

    @Override
    /**
     * Add the weights of to the average weights. This should be done once weight vector is updated. The calling time
     * is to be determined by the update algorithm.
     */
    public void updateAverageWeight() {
        for (TIntDoubleIterator iter = weights.iterator(); iter.hasNext(); ) {
            iter.advance();
            int index = iter.key();
            double value = iter.value();
            averagedWeights.adjustOrPutValue(index, value, value);
        }
        averageUpdateCount++;
    }

    @Override
    void consolidate() {
//        int numRemoved = 0;
//        int total = 0;
        if (!consolidated) {
            for (TIntDoubleIterator iter = weights.iterator(); iter.hasNext(); ) {
                iter.advance();
                if (iter.value() == 0) {
                    iter.remove();
//                    numRemoved++;
                }
//                total++;
            }

            for (TIntDoubleIterator iter = averagedWeights.iterator(); iter.hasNext(); ) {
                iter.advance();
                if (iter.value() == 0) {
                    iter.remove();
                } else {
                    if (averageUpdateCount != 0) {
                        // Turn sum of weights to average of weights.
                        iter.setValue(iter.value() / averageUpdateCount);
                    }
                }
            }
            consolidated = true;
        }
//        System.out.println(String.format("Found %d zero features and removed.", numRemoved));
//        System.out.println(String.format("Total visited %d.", total));
//        System.out.println(String.format("Size after is " + averagedWeights.size()) + " " + weights.size());
    }

    @Override
    void deconsolidate() {
        if (consolidated) {
            if (averageUpdateCount != 0) {
                // Turn average weights back to sum of weights.
                for (TIntDoubleIterator iter = averagedWeights.iterator(); iter.hasNext(); ) {
                    iter.advance();
                    iter.setValue(iter.value() * averageUpdateCount);
                }
            }
            consolidated = false;
        }
    }

    @Override
    public double getWeightAt(int i) {
        return weights.get(i);
    }

    @Override
    public double getAverageWeightAt(int i) {
        return averagedWeights.get(i);
    }

    @Override
    public int getFeatureSize() {
        return weights.size();
    }

    public TIntDoubleIterator getWeightsIterator() {
        return weights.iterator();
    }

    public TIntDoubleIterator getAverageWeightsIterator() {
        return averagedWeights.iterator();
    }
}
