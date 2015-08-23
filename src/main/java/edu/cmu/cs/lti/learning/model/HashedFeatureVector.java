package edu.cmu.cs.lti.learning.model;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * Date: 8/20/15
 * Time: 10:53 PM
 *
 * @author Zhengzhong Liu
 */
public abstract class HashedFeatureVector implements Serializable {
    private static final long serialVersionUID = -6736949803936456446L;

    Alphabet alphabet;

    public HashedFeatureVector(Alphabet alphabet) {
        this.alphabet = alphabet;
    }

    public interface FeatureIterator {
        int featureIndex();

        double featureValue();

        boolean hasNext();

        void next();
    }

    public int addFeature(String featureName, double featureValue) {
        return addFeature(alphabet.hash(featureName), featureValue);
    }

    public abstract int addFeature(int featureIndex, double featureValue);

    public void extend(HashedFeatureVector vectorToAdd) {
        for (FeatureIterator iter = vectorToAdd.featureIterator(); iter.hasNext(); iter.next()) {
            addFeature(iter.featureIndex(), iter.featureValue());
        }
    }

    public double getFeatureValue(String featureName, double featureValue) {
        return getFeatureValue(alphabet.hash(featureName));
    }

    public abstract double getFeatureValue(int featureIndex);

    public abstract FeatureIterator featureIterator();

}
