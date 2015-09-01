package edu.cmu.cs.lti.learning.model;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

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

    boolean consolidated;

    public AveragedWeightVector(int featureSize) {
        weights = new double[featureSize];
        averagedWeights = new double[featureSize];
        updateCounts = 0;
    }

    public void updateWeightsBy(HashedFeatureVector fv, double multiplier) {
        for (HashedFeatureVector.FeatureIterator iter = fv.featureIterator(); iter.hasNext(); ) {
            iter.next();
            int index = iter.featureIndex();
            weights[index] += iter.featureValue() * multiplier;
            averagedWeights[index] += weights[index];
        }
        updateCounts++;
    }

    public void write(File outputFile) throws FileNotFoundException {
        if (!consolidated) {
            consolidate();
            SerializationUtils.serialize(this, new FileOutputStream(outputFile));
        }
    }

    public double dotProd(HashedFeatureVector fv) {
        double sum = 0;
        for (HashedFeatureVector.FeatureIterator iter = fv.featureIterator(); iter.hasNext(); ) {
            iter.next();
            sum += weights[iter.featureIndex()] * iter.featureValue();
        }
        return sum;
    }

    public double dotProdAver(HashedFeatureVector fv) {
        double sum = 0;
        for (HashedFeatureVector.FeatureIterator iter = fv.featureIterator(); iter.hasNext(); ) {
            iter.next();
            sum += averagedWeights[iter.featureIndex()] * iter.featureValue();
        }
        return sum;
    }

    private void consolidate() {
        for (int i = 0; i < averagedWeights.length; i++) {
            averagedWeights[i] /= updateCounts;
        }
        consolidated = true;
    }

    private void deconsolidate() {
        for (int i = 0; i < averagedWeights.length; i++) {
            averagedWeights[i] *= updateCounts;
        }
        consolidated = false;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        deconsolidate();
    }

}
