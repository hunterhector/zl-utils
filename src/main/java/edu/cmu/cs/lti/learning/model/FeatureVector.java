package edu.cmu.cs.lti.learning.model;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * Date: 8/20/15
 * Time: 10:53 PM
 *
 * @author Zhengzhong Liu
 */
public abstract class FeatureVector implements Serializable {
    private static final long serialVersionUID = -6736949803936456446L;

    protected FeatureAlphabet alphabet;

    protected int featureSize;

    private FeatureVector emptyVector = newVector();

    public FeatureVector(FeatureAlphabet alphabet) {
        this.alphabet = alphabet;
    }

    public interface FeatureIterator {
        int featureIndex();

        double featureValue();

        boolean hasNext();

        void next();
    }

    public int addFeature(String featureName, double featureValue) {
        return addFeature(alphabet.getFeatureId(featureName), featureValue);
    }

    /**
     * Create a feature vector sharing this one's setting (such as Binary/Real, Alphabet)
     *
     * @return The Feature Vector sharing the settings.
     */
    public abstract FeatureVector newVector();

    /**
     * Add a feature to the feature vector by directly accessing the feature index. We normally do not want to access
     * it outside because feature index should come from a feature alphabet.
     *
     * @param featureIndex The feature index.
     * @param featureValue The value of the feature.
     * @return The resulting number of features in this feature vector.
     */
    protected int addFeature(int featureIndex, double featureValue) {
        if (addFeatureInternal(featureIndex, featureValue)) {
            featureSize++;
        }
        return featureSize;
    }

    /**
     * Add a feature by index.
     *
     * @param featureIndex The feature index to add.
     * @param featureValue The feature value to add.
     * @return True if the feature is a new one.
     */
    protected abstract boolean addFeatureInternal(int featureIndex, double featureValue);

    public void extend(FeatureVector vectorToAdd) {
        for (FeatureIterator iter = vectorToAdd.featureIterator(); iter.hasNext(); ) {
            iter.next();
            addFeature(iter.featureIndex(), iter.featureValue());
        }
    }

    public void diff(FeatureVector vectorToDiff, FeatureVector resultVector) {
        TIntSet overlappedFeatures = new TIntHashSet();
        for (FeatureIterator iter = vectorToDiff.featureIterator(); iter.hasNext(); ) {
            iter.next();
            double thisValue = this.getFeatureValue(iter.featureIndex()); // This will always return a value.
            if (thisValue != iter.featureValue()) {
                resultVector.addFeature(iter.featureIndex(), thisValue - iter.featureValue());
            }
            overlappedFeatures.add(iter.featureIndex());
        }

        for (FeatureIterator iter = this.featureIterator(); iter.hasNext(); ) {
            iter.next();

            if (!overlappedFeatures.contains(iter.featureIndex())) {
                resultVector.addFeature(iter.featureIndex(), iter.featureValue());
            }
        }
    }

    /**
     * Get the negation of this feature by diff it from an empty feature vector.
     *
     * @return Negated vector
     */
    public FeatureVector negation() {
        FeatureVector negatedVector = newVector();
        emptyVector.diff(this, negatedVector);
        return negatedVector;
    }

    public double getFeatureValue(String featureName) {
        return getFeatureValue(alphabet.getFeatureId(featureName));
    }

    public abstract double getFeatureValue(int featureIndex);

    public abstract FeatureIterator featureIterator();

    public String toString() {
        StringBuilder features = new StringBuilder();
        FeatureIterator iter = featureIterator();

        String sep = "";
        while (iter.hasNext()) {
            iter.next();
            features.append(sep);
            features.append(String.format("%d : %.2f", iter.featureIndex(), iter.featureValue()));
            sep = " ";
        }

        return features.toString();
    }

    public String readableString() {
        return readableString("\n");
    }

    public String readableString(String separator) {
        StringBuilder features = new StringBuilder();
        FeatureIterator iter = featureIterator();

        String sep = "";
        while (iter.hasNext()) {
            iter.next();
            features.append(sep);
            features.append(String.format("%d %s : %.2f", iter.featureIndex(), alphabet.getFeatureNameRepre(iter
                    .featureIndex()), iter.featureValue()));
            sep = separator;
        }

        return features.toString();
    }

    public int getFeatureSize() {
        return featureSize;
    }

    public FeatureAlphabet getAlphabet() {
        return alphabet;
    }

    public double dotProd(FeatureVector v) {
        double dotProd = 0;
        for (FeatureIterator iter = this.featureIterator(); iter.hasNext(); ) {
            iter.next();
            dotProd += v.getFeatureValue(iter.featureIndex()) * iter.featureValue();
        }
        return dotProd;
    }
}
