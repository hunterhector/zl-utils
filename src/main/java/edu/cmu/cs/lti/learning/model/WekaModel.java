package edu.cmu.cs.lti.learning.model;

import gnu.trove.iterator.TObjectDoubleIterator;
import gnu.trove.map.TObjectDoubleMap;
import org.javatuples.Pair;
import weka.classifiers.Classifier;
import weka.core.*;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Date: 9/14/15
 * Time: 3:19 PM
 *
 * @author Zhengzhong Liu
 */
public class WekaModel implements Serializable {
    private static final long serialVersionUID = -7122596301161728470L;

    public static final String MODEL_BASENAME = "model";

    private FeatureAlphabet alphabet;

    private ClassAlphabet classAlphabet;

    private Map<String, Classifier> classifiers;

    private ArrayList<Attribute> featureConfiguration;

    private Instances emptyDataSet;

    private double[] emptyVector;

    public WekaModel(File modelDir) throws Exception {
        load(modelDir);
    }

    public FeatureAlphabet getAlphabet() {
        return alphabet;
    }

    public WekaModel(FeatureAlphabet alphabet, ClassAlphabet classAlphabet, Map<String, Classifier> classifiers,
                     ArrayList<Attribute> featureConfiguration) {
        this.alphabet = alphabet;
        this.classAlphabet = classAlphabet;
        this.classifiers = classifiers;
        this.featureConfiguration = featureConfiguration;
        emptyDataSet = new Instances("dummy", featureConfiguration, 0);
    }

    public void load(File modelDir) throws Exception {
        WekaModel model = (WekaModel) SerializationHelper.read(new File(modelDir, MODEL_BASENAME).getPath());
        this.alphabet = model.alphabet;
        this.classAlphabet = model.classAlphabet;
        this.classifiers = model.classifiers;
        this.featureConfiguration = model.featureConfiguration;
    }

    public void write(File modelDir) throws Exception {
        SerializationHelper.write(new File(modelDir, MODEL_BASENAME).getPath(), this);
    }

    public Pair<Double, String> classify(String classifierName, TObjectDoubleMap<String> features) throws Exception {
        return classify(classifierName, createInstance(features));
    }

    public Pair<Double, String> classify(String classifierName, Instance instance) throws Exception {
        Classifier cls = classifiers.get(classifierName);
        double[] dist = cls.distributionForInstance(instance);

        double best = Double.NEGATIVE_INFINITY;
        int bestIndex = -1;
        for (int i = 1; i < dist.length; i++) {
            if (dist[i] > best) {
                best = dist[i];
                bestIndex = i;
            }
        }
        return Pair.with(best, classAlphabet.getClassName(bestIndex));
    }

    private Instance createInstance(TObjectDoubleMap<String> features) {
        Instance instance = new SparseInstance(1, emptyVector);
        instance.setDataset(emptyDataSet);
        instance.setClassMissing();
        for (TObjectDoubleIterator<String> fIter = features.iterator(); fIter.hasNext(); ) {
            fIter.advance();
            int featureId = alphabet.getFeatureId(fIter.key());
            double featureVal = fIter.value();
            instance.setValue(featureConfiguration.get(featureId), featureVal);
        }
        return instance;
    }
}
