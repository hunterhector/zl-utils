package edu.cmu.cs.lti.learning.model;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private double[] weights;
    private double[] averagedWeights;
    private int updateCounts;

    private boolean consolidated;

    public ArrayBasedAveragedWeightVector(int featureSize) {
        weights = new double[featureSize];
        averagedWeights = new double[featureSize];
        updateCounts = 0;
    }

    @Override
    public void updateWeightsBy(FeatureVector fv, double multiplier) {
        for (FeatureVector.FeatureIterator iter = fv.featureIterator(); iter.hasNext(); ) {
            iter.next();
            int index = iter.featureIndex();
            weights[index] += iter.featureValue() * multiplier;
            averagedWeights[index] += weights[index];
        }
        updateCounts++;
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
            logger.info("Consolidating weights.");
            for (int i = 0; i < averagedWeights.length; i++) {
                averagedWeights[i] /= updateCounts;
            }
            consolidated = true;
        }
    }

    @Override
    void deconsolidate() {
        if (consolidated) {
            logger.info("Deconsolidating weights.");
            for (int i = 0; i < averagedWeights.length; i++) {
                averagedWeights[i] *= updateCounts;
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
