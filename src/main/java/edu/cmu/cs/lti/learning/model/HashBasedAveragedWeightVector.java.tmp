package edu.cmu.cs.lti.learning.model;

import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
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
public class HashBasedAveragedWeightVector extends AveragedWeightVector {
    private static final long serialVersionUID = 7646416117744167293L;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private int updateCounts;

    private TIntDoubleMap weights;
    private TIntDoubleMap averagedWeights;

    private boolean consolidated;

    public HashBasedAveragedWeightVector() {
        weights = new TIntDoubleHashMap();
        averagedWeights = new TIntDoubleHashMap();
        updateCounts = 0;
    }

    @Override
    public void updateWeightsBy(FeatureVector fv, double multiplier) {
        for (FeatureVector.FeatureIterator iter = fv.featureIterator(); iter.hasNext(); ) {
            iter.next();
            int index = iter.featureIndex();
            double updateAmount = iter.featureValue() * multiplier;
            double updateResult = weights.adjustOrPutValue(index, updateAmount, updateAmount);
            averagedWeights.adjustOrPutValue(index, updateResult, updateResult);
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
            sum += weights.get(iter.featureIndex()) * iter.featureValue();
        }
        return sum;
    }

    @Override
    public double dotProdAver(FeatureVector fv) {
        double sum = 0;
        for (FeatureVector.FeatureIterator iter = fv.featureIterator(); iter.hasNext(); ) {
            iter.next();
            sum += averagedWeights.get(iter.featureIndex()) * iter.featureValue();
        }
        return sum;
    }

    @Override
    void consolidate() {
        if (!consolidated) {
            logger.info("Consolidating weights.");

            for (TIntDoubleIterator iter = averagedWeights.iterator(); iter.hasNext(); ) {
                iter.advance();
                if (iter.value() == 0) {
                    iter.remove();
                } else {
                    iter.setValue(iter.value() / updateCounts);
                }
            }

            consolidated = true;
        }
    }

    @Override
    void deconsolidate() {
        if (consolidated) {
            logger.info("Deconsolidating weights.");
            for (TIntDoubleIterator iter = averagedWeights.iterator(); iter.hasNext(); ) {
                iter.advance();
                iter.setValue(iter.value() * updateCounts);
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
}
