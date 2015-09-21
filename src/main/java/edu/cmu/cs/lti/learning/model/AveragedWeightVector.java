package edu.cmu.cs.lti.learning.model;

import org.apache.commons.lang3.SerializationUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * Date: 8/21/15
 * Time: 9:19 PM
 *
 * @author Zhengzhong Liu
 */
public abstract class AveragedWeightVector implements Serializable {
    private static final long serialVersionUID = 7646416117744167293L;

    public abstract void updateWeightsBy(FeatureVector fv, double multiplier);

    public abstract void updateAverageWeight();

    public void write(File outputFile) throws FileNotFoundException {
        consolidate();
        SerializationUtils.serialize(this, new FileOutputStream(outputFile));
        deconsolidate();
    }

    public abstract double dotProd(FeatureVector fv);

    public abstract double dotProdAver(FeatureVector fv);

    abstract void consolidate();

    abstract void deconsolidate();

    public abstract double getWeightAt(int i);

    public abstract double getAverageWeightAt(int i);

    public abstract int getFeatureSize();
}
