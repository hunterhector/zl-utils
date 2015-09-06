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
public abstract class HashedFeatureVector implements Serializable {
    private static final long serialVersionUID = -6736949803936456446L;

    private Alphabet alphabet;

    private int featureSize;

    public HashedFeatureVector(Alphabet alphabet) {
        this.alphabet = alphabet;
    }

    public interface FeatureIterator {
        int featureIndex();

        double featureValue();

        boolean hasNext();

        void next();
    }

    public int addFeature(String featureName, String tag, double featureValue) {
        if (addFeature(alphabet.hash(featureName + ":Ti=" + tag), featureValue)) {
            featureSize++;
        }
        return featureSize;
    }

    public abstract boolean addFeature(int featureIndex, double featureValue);

    public void extend(HashedFeatureVector vectorToAdd) {
        for (FeatureIterator iter = vectorToAdd.featureIterator(); iter.hasNext(); ) {
            iter.next();
            addFeature(iter.featureIndex(), iter.featureValue());
        }
    }

    public void diff(HashedFeatureVector vectorToDiff, HashedFeatureVector resultVector) {
        TIntSet overlappedFeatures = new TIntHashSet();
        for (FeatureIterator iter = vectorToDiff.featureIterator(); iter.hasNext(); ) {
            iter.next();
            double thisValue = this.getFeatureValue(iter.featureIndex());
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

    public double getFeatureValue(String featureName) {
        return getFeatureValue(alphabet.hash(featureName));
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
        if (!alphabet.isStoreReadable()) {
            return "";
        }

        StringBuilder features = new StringBuilder();
        FeatureIterator iter = featureIterator();

        String sep = "";
        while (iter.hasNext()) {
            iter.next();
            features.append(sep);
            features.append(String.format("%d %s : %.2f", iter.featureIndex(), alphabet.getMappedFeatureCounters(iter
                    .featureIndex()), iter.featureValue()));
            sep = separator;
        }

        return features.toString();
    }


    public int getFeatureSize() {
        return featureSize;
    }

    public Alphabet getAlphabet() {
        return alphabet;
    }
}
