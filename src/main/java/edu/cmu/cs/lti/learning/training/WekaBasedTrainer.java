package edu.cmu.cs.lti.learning.training;

import edu.cmu.cs.lti.learning.model.BiMapAlphabet;
import edu.cmu.cs.lti.learning.model.ClassAlphabet;
import edu.cmu.cs.lti.learning.model.FeatureAlphabet;
import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Classifier;
import weka.core.*;
import weka.core.converters.ArffSaver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Date: 9/9/15
 * Time: 8:44 PM
 *
 * @author Zhengzhong Liu
 */
public abstract class WekaBasedTrainer {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String FEATURE_ALPHABET_BASENAME = "featureNames";

    public static final String CLASS_LABEL_BASENAME = "labelNames";

    public static final String DATASET_BASENAME = "dataset.arff";

    private ArrayList<Attribute> featureConfiguration;

    private ArffSaver saver = new ArffSaver();

    private FeatureAlphabet alphabet = new BiMapAlphabet();

    private ClassAlphabet classAlphabet = new ClassAlphabet();

    protected abstract Map<String, Classifier> getClassifiers() throws Exception;

    // Implementation should fill the features and feature name mapping.
    protected abstract void getFeatures(List<Pair<TIntDoubleMap, String>> instances,
                                        FeatureAlphabet alphabet,
                                        ClassAlphabet classAlphabet);

    private void configFeatures(TObjectIntMap<String> featureNameMap, ClassAlphabet classAlphabet) throws Exception {
        featureConfiguration = new ArrayList<>();
        declareFeatures(featureNameMap, featureConfiguration);
        declareClass(classAlphabet, featureConfiguration);
        logger.info("Number of features : " + featureNameMap.size() + ". Number of classes : " + classAlphabet.size());
    }

    private void writeModel(String modelOutputDir, String classifierName, Classifier cls) throws Exception {
        if (modelOutputDir != null) {
            logger.info("Saving feature alphabet.");
            alphabet.write(new File(modelOutputDir, FEATURE_ALPHABET_BASENAME));
            logger.info("Saving class alphabet.");
            classAlphabet.write(new File(modelOutputDir, CLASS_LABEL_BASENAME));
            SerializationHelper.write(new File(modelOutputDir, classifierName).getPath(), cls);
        }
    }

    private void declareClass(ClassAlphabet classAlphabet, List<Attribute> featureVector) {
        List<String> fixedClasses = new ArrayList<>();
        //a bug related to the sparse vector
        fixedClasses.add("dummy_class");
        for (int i = 0; i < classAlphabet.size(); i++) {
            fixedClasses.add(classAlphabet.getClassName(i));
        }
        featureVector.add(new Attribute("event_types", fixedClasses));
    }

    private void declareFeatures(TObjectIntMap<String> featureNames, List<Attribute> featureVector) {
        Attribute[] featureArray = new Attribute[featureNames.size()];

        for (TObjectIntIterator<String> iter = featureNames.iterator(); iter.hasNext(); ) {
            iter.advance();
            featureArray[iter.value()] = new Attribute(iter.key());
        }

        Collections.addAll(featureVector, featureArray);
    }

    private Instances prepareDataSet(List<Pair<TIntDoubleMap, String>> featuresAndClass, String dataSetOutputPath)
            throws Exception {
        Instances dataSet = new Instances("event_type_detection", featureConfiguration, featuresAndClass.size());
        dataSet.setClass(featureConfiguration.get(featureConfiguration.size() - 1));

        logger.info("Adding instances");
        double[] emptyVector = new double[featureConfiguration.size()];

        for (Pair<TIntDoubleMap, String> rawData : featuresAndClass) {
            //initialize the sparse vector to be empty
            Instance trainingInstance = new SparseInstance(1, emptyVector);
            TIntDoubleMap featureValues = rawData.getValue0();
            trainingInstance.setDataset(dataSet);
            String classValue = rawData.getValue1();

            for (TIntDoubleIterator fIter = featureValues.iterator(); fIter.hasNext(); ) {
                fIter.advance();
                int featureId = fIter.key();
                double featureVal = fIter.value();
                trainingInstance.setValue(featureConfiguration.get(featureId), featureVal);
            }
            //set class
            trainingInstance.setClassValue(classValue);
            dataSet.add(trainingInstance);
        }

        logger.info("Number of instances stored : " + dataSet.numInstances());

        if (dataSetOutputPath != null) {
            logger.info("Saving data set to : " + dataSetOutputPath);
            saveDataSet(dataSet, dataSetOutputPath);
        }
        return dataSet;
    }


    private void saveDataSet(Instances dataset, String path) throws IOException {
        saver.setInstances(dataset);
        saver.setFile(new File(path));
        saver.writeBatch();
    }

    public void buildModels(String modelOutputDir) throws Exception {
        List<Pair<TIntDoubleMap, String>> rawInstances = new ArrayList<>();
        TObjectIntMap<String> featureNameMap = new TObjectIntHashMap<>();
        ClassAlphabet classAlphabet = new ClassAlphabet();

        logger.info("Running feature generation.");
        getFeatures(rawInstances, alphabet, classAlphabet);

        configFeatures(featureNameMap, classAlphabet);

        Instances trainingDataset = prepareDataSet(rawInstances, new File(modelOutputDir, DATASET_BASENAME).getPath());

        for (Map.Entry<String, Classifier> nameAndCls : getClassifiers().entrySet()) {
            Classifier cls = nameAndCls.getValue();
            String name = nameAndCls.getKey();
            cls.buildClassifier(trainingDataset);
            writeModel(modelOutputDir, name, cls);
        }
    }

}
