package edu.cmu.cs.lti.learning.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * Date: 9/7/16
 * Time: 11:14 AM
 *
 * @author Zhengzhong Liu
 */
public class WeightVectorTest {

    @Test
    public void consolidate() {
        // Make this a little bit large to avoid test time collision.
        FeatureAlphabet alphabet = new HashAlphabet(10, true);

        HashBasedAveragedWeightVector weightVector = new HashBasedAveragedWeightVector();

        FeatureVector fv1 = new RealValueHashFeatureVector(alphabet);
        fv1.addFeature("feature1", 1);
        fv1.addFeature("feature2", 2);
        fv1.addFeature("feature1", -1);
        fv1.addFeature("feature3", 0);
        fv1.addFeature("feature4", 2.0);

        FeatureVector fv2 = new RealValueHashFeatureVector(alphabet);
        fv2.addFeature("feature2", -2);
        fv2.addFeature("feature4", 1);

        weightVector.updateWeightsBy(fv1, 1);
        weightVector.updateWeightsBy(fv2, 1);
        weightVector.updateAverageWeight();

        Assert.assertEquals(4, weightVector.getFeatureSize());
        weightVector.consolidate();
        Assert.assertEquals(1, weightVector.getFeatureSize());
    }
}
