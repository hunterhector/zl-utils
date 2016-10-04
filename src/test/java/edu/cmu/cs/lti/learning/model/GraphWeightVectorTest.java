package edu.cmu.cs.lti.learning.model;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * Date: 9/23/16
 * Time: 6:39 PM
 *
 * @author Zhengzhong Liu
 */
public class GraphWeightVectorTest {
    @Test
    public void consolidate() throws IOException {
        System.out.println("Testing graph weight vector.");

        FeatureAlphabet featureAlphabet = new HashAlphabet(10, true);
        ClassAlphabet classAlphabet = new ClassAlphabet();
        classAlphabet.addClass("class1");
        classAlphabet.addClass("class2");

        GraphWeightVector fullWeight = new GraphWeightVector(classAlphabet, featureAlphabet, "");
        GraphFeatureVector gfv = new GraphFeatureVector(classAlphabet, featureAlphabet);

        FeatureVector fv1 = new RealValueHashFeatureVector(featureAlphabet);
        fv1.addFeature("feature1", 1);
        fv1.addFeature("feature2", 2);
        fv1.addFeature("feature1", -1);
        fv1.addFeature("feature3", 0);
        fv1.addFeature("feature4", 2.0);

        FeatureVector fv2 = new RealValueHashFeatureVector(featureAlphabet);
        fv2.addFeature("feature2", -2);
        fv2.addFeature("feature4", 1);

        gfv.extend(fv1, "class1");
        gfv.extend(fv2, "class1");

        fullWeight.updateWeightsBy(gfv, 1);

        Assert.assertEquals(4, fullWeight.getNodeWeights("class1").getFeatureSize());

        File tempFile = File.createTempFile("weights", ".ser");
        fullWeight.write(tempFile);
        GraphWeightVector modelBack = SerializationUtils.deserialize(new FileInputStream(tempFile));

        Assert.assertEquals(1, modelBack.getNodeWeights("class1").getFeatureSize());

    }
}