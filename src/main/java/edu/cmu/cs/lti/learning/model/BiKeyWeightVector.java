package edu.cmu.cs.lti.learning.model;

import com.google.common.collect.Table;
import gnu.trove.iterator.TIntObjectIterator;
import org.apache.commons.lang3.SerializationUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * Created with IntelliJ IDEA.
 * Date: 9/16/15
 * Time: 8:28 PM
 *
 * @author Zhengzhong Liu
 */
public class BiKeyWeightVector implements Serializable {
    private static final long serialVersionUID = 5181873403599233786L;

    private AveragedWeightVector[] unikeyWeights;

    private AveragedWeightVector[][] bikeyWeights;

    private int featureDimension;


    public BiKeyWeightVector(ClassAlphabet classAlphabet, int featureDimension) {
        unikeyWeights = new AveragedWeightVector[classAlphabet.size()];

        // These are the largest objects that stores feature weights. Initialize them first then we will probably be
        // fine with memory usage.
        for (int i = 0; i < unikeyWeights.length; i++) {
            unikeyWeights[i] = new ArrayBasedAveragedWeightVector(featureDimension);
        }

        bikeyWeights = new AveragedWeightVector[classAlphabet.size()][classAlphabet.size()];
        this.featureDimension = featureDimension;
    }

    public void updateWeightsBy(FeatureVector fv, int currentKey, double multiplier) {
        if (unikeyWeights[currentKey] == null) {
            unikeyWeights[currentKey] = new ArrayBasedAveragedWeightVector(featureDimension);
//            System.out.println("New uni weights for " + currentKey);
        }
        unikeyWeights[currentKey].updateWeightsBy(fv, multiplier);
    }

    public void updateWeightsBy(FeatureVector fv, int currentKey, int previousKey, double multiplier) {
        if (bikeyWeights[currentKey][previousKey] == null) {
            bikeyWeights[currentKey][previousKey] = new HashBasedAveragedWeightVector();
//            System.out.println("New bi weights for " + currentKey + " " + previousKey);
        }
        bikeyWeights[currentKey][previousKey].updateWeightsBy(fv, multiplier);
    }

    public void updateWeightBy(BiKeyFeatureVector updateVector, double multiplier) {
        for (TIntObjectIterator<FeatureVector> iter = updateVector.unikeyIter(); iter.hasNext(); ) {
            iter.advance();
            updateWeightsBy(iter.value(), iter.key(), multiplier);
        }

        for (Iterator<Table.Cell<Integer, Integer, FeatureVector>> iter = updateVector.bikeyIter(); iter.hasNext(); ) {
            Table.Cell<Integer, Integer, FeatureVector> v = iter.next();
            updateWeightsBy(v.getValue(), v.getRowKey(), v.getColumnKey(), multiplier);
        }

    }

    public double dotProd(FeatureVector fv, int currentKey) {
        if (unikeyWeights[currentKey] != null) {
//            System.out.println("Get " + currentKey + " success");
//            System.out.println(unikeyWeights[currentKey].dotProd(fv));
            return unikeyWeights[currentKey].dotProd(fv);
        } else {
            return 0;
        }
    }

    public double dotProd(FeatureVector fv, int currentKey, int previousKey) {
        if (bikeyWeights[currentKey][previousKey] != null) {
//            System.out.println("Get " + currentKey + " " + previousKey + " success");
//            System.out.println(bikeyWeights[currentKey][previousKey].dotProd(fv));
            return bikeyWeights[currentKey][previousKey].dotProd(fv);
        } else {
            return 0;
        }
    }

    public double dotProdAver(FeatureVector fv, int currentKey) {
        if (unikeyWeights[currentKey] != null) {
            return unikeyWeights[currentKey].dotProdAver(fv);
        } else {
            return 0;
        }
    }

    public double dotProdAver(FeatureVector fv, int currentKey, int previousKey) {
        if (bikeyWeights[currentKey][previousKey] != null) {
            return bikeyWeights[currentKey][previousKey].dotProdAver(fv);
        } else {
            return 0;
        }
    }


    public void write(File outputFile) throws FileNotFoundException {
        consolidate();
        SerializationUtils.serialize(this, new FileOutputStream(outputFile));
        deconsolidate();
    }

    private void consolidate() {
        applyToAll(AveragedWeightVector::consolidate);
    }


    public void deconsolidate() {
        applyToAll(AveragedWeightVector::deconsolidate);
    }


    private void applyToAll(Consumer<AveragedWeightVector> oper) {
        for (AveragedWeightVector unikeyWeight : unikeyWeights) {
            oper.accept(unikeyWeight);
        }

        for (AveragedWeightVector[] bikeyWeight : bikeyWeights) {
            for (AveragedWeightVector aBikeyWeight : bikeyWeight) {
                oper.accept(aBikeyWeight);
            }
        }
    }
}
