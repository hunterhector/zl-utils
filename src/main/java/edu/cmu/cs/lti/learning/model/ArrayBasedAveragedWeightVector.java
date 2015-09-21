package edu.cmu.cs.lti.learning.model;

import org.apache.commons.lang3.SerializationUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created with IntelliJ IDEA.
 * Date: 8/21/15
 * Time: 9:19 PM
 *
 * @author Zhengzhong Liu
 */
public class ArrayBasedAveragedWeightVector extends AveragedWeightVector {
    private static final long serialVersionUID = 7646416117744167293L;

    private double[] weights;
    private double[] averagedWeights;
    private int averageUpdateCount;

    private boolean consolidated;

    public ArrayBasedAveragedWeightVector(int featureSize, int initialAverageUpdateCount) {
        weights = new double[featureSize];
        averagedWeights = new double[featureSize];
        averageUpdateCount = initialAverageUpdateCount;
        consolidated = false;
    }

    @Override
    public void updateWeightsBy(FeatureVector fv, double multiplier) {
        for (FeatureVector.FeatureIterator iter = fv.featureIterator(); iter.hasNext(); ) {
            iter.next();
            int index = iter.featureIndex();
//            double before = weights[index];
            weights[index] += iter.featureValue() * multiplier;

//            System.out.println("Updating " + fv.getAlphabet().getFeatureNameRepre(index) + " from " + before + " to "
//                    + weights[index]);
        }
    }

    @Override
    public void updateAverageWeight() {
        for (int i = 0; i < weights.length; i++) {
            averagedWeights[i] += weights[i];
        }
        averageUpdateCount++;
    }

    @Override
    public void write(File outputFile) throws FileNotFoundException {
        consolidate();
        SerializationUtils.serialize(this, new FileOutputStream(outputFile));
        deconsolidate();
    }

    @Override
    public double dotProd(FeatureVector fv) {
        double sum = 0;
        for (FeatureVector.FeatureIterator iter = fv.featureIterator(); iter.hasNext(); ) {
            iter.next();
            sum += weights[iter.featureIndex()] * iter.featureValue();
        }
        return sum;
    }

    // TODO temporary verbose version
    public double dotProd(FeatureVector fv, FeatureAlphabet alphabet) {
        double sum = 0;
        for (FeatureVector.FeatureIterator iter = fv.featureIterator(); iter.hasNext(); ) {
            iter.next();
            sum += weights[iter.featureIndex()] * iter.featureValue();
            if (weights[iter.featureIndex()] != 0) {
                System.out.println("Feature : " + alphabet.getFeatureNameRepre(iter.featureIndex()) + " has weight "
                        + weights[iter.featureIndex()] + " multiply by value " + iter.featureValue()
                        + " result is " + weights[iter.featureIndex()] * iter.featureValue());
            }
        }
        if (sum != 0) {
            System.out.println("Result Sum is " + sum);
        }
        return sum;
    }

    @Override
    public double dotProdAver(FeatureVector fv) {
        double sum = 0;
        for (FeatureVector.FeatureIterator iter = fv.featureIterator(); iter.hasNext(); ) {
            iter.next();
            sum += averagedWeights[iter.featureIndex()] * iter.featureValue();
        }
        return sum;
    }

    @Override
    void consolidate() {
        if (!consolidated) {
            if (this.averageUpdateCount != 0) {
                for (int i = 0; i < averagedWeights.length; i++) {
                    averagedWeights[i] /= averageUpdateCount;
                }
            }
            consolidated = true;
        }
    }

    @Override
    void deconsolidate() {
        if (consolidated) {
            if (this.averageUpdateCount != 0) {
                for (int i = 0; i < averagedWeights.length; i++) {
                    averagedWeights[i] *= averageUpdateCount;
                }
            }
            consolidated = false;
        }
    }

    @Override
    public double getWeightAt(int i) {
        return weights[i];
    }

    @Override
    public double getAverageWeightAt(int i) {
        return averagedWeights[i];
    }

    @Override
    public int getFeatureSize() {
        return weights.length;
    }
}
