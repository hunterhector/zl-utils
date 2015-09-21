package edu.cmu.cs.lti.learning.model;

import com.google.common.collect.Table;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.linked.TIntLinkedList;
import org.apache.commons.lang3.SerializationUtils;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created with IntelliJ IDEA.
 * Date: 9/16/15
 * Time: 8:28 PM
 *
 * @author Zhengzhong Liu
 */
public class GraphWeightVector implements Serializable {
    private static final long serialVersionUID = 5181873403599233786L;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private AveragedWeightVector[] nodeWeights;

    private AveragedWeightVector[][] edgeWeights;

    private TIntList activedNodeKeys;

    private List<Pair<Integer, Integer>> activedEdgeKeys;

    private ClassAlphabet classAlphabet;

    private FeatureAlphabet featureAlphabet;

    private int averageUpdateCount;

    public GraphWeightVector(ClassAlphabet classAlphabet, FeatureAlphabet featureAlphabet) {
        nodeWeights = new AveragedWeightVector[classAlphabet.size()];

        activedNodeKeys = new TIntLinkedList();
        activedEdgeKeys = new ArrayList<>();

        this.featureAlphabet = featureAlphabet;
        this.classAlphabet = classAlphabet;

        averageUpdateCount = 0;

        // These are the largest objects that stores feature weights. Initialize them first then we will probably be
        // fine with memory usage.
        // Here all these weight vectors are arrays, which are fast but very space consuming.
        for (int i = 0; i < nodeWeights.length; i++) {
            addWeightVector(i);
        }

        edgeWeights = new AveragedWeightVector[classAlphabet.size()][classAlphabet.size()];
    }

    public AveragedWeightVector getNodeWeights(int nodeIndex) {
        return nodeWeights[nodeIndex];
    }

    public AveragedWeightVector getEdgeWeights(int endNodeIndex, int fromNodeIndex) {
//        logger.debug("Successfully get weight at " + fromNodeIndex + " to " + endNodeIndex);
        return edgeWeights[endNodeIndex][fromNodeIndex];
    }

    private void addWeightVector(int classIndex) {
        nodeWeights[classIndex] = new ArrayBasedAveragedWeightVector(featureAlphabet.getAlphabetSize(),
                averageUpdateCount);
        activedNodeKeys.add(classIndex);
    }

    private void addWeightVector(int rowIndex, int colIndex) {
        edgeWeights[rowIndex][colIndex] = new HashBasedAveragedWeightVector(averageUpdateCount);
        activedEdgeKeys.add(Pair.with(rowIndex, colIndex));
    }

    public Iterator<Pair<Integer, AveragedWeightVector>> nodeWeightIterator() {
        return new Iterator<Pair<Integer, AveragedWeightVector>>() {
            int activeKeyIndex = 0;

            @Override
            public boolean hasNext() {
                return activeKeyIndex < activedNodeKeys.size();
            }

            @Override
            public Pair<Integer, AveragedWeightVector> next() {
                int activeKey = activedNodeKeys.get(activeKeyIndex);
                Pair<Integer, AveragedWeightVector> result = Pair.with(activeKey, nodeWeights[activeKey]);
                activeKeyIndex++;
                return result;
            }
        };
    }

    public Iterator<Triplet<Integer, Integer, AveragedWeightVector>> edgeWeightIterator() {
        return new Iterator<Triplet<Integer, Integer, AveragedWeightVector>>() {

            Iterator<Pair<Integer, Integer>> bikey = activedEdgeKeys.iterator();

            @Override
            public boolean hasNext() {
                return bikey.hasNext();
            }

            @Override
            public Triplet<Integer, Integer, AveragedWeightVector> next() {
                Pair<Integer, Integer> key = bikey.next();
                return Triplet.with(key.getValue0(), key.getValue1(), edgeWeights[key.getValue0()][key.getValue1()]);
            }
        };
    }

    public void updateWeightsBy(FeatureVector fv, int currentKey, double multiplier) {
        if (nodeWeights[currentKey] == null) {
            addWeightVector(currentKey);
        }

//        logger.debug("Updating features for " + classAlphabet.getClassName(currentKey) + " by " + multiplier);
//        logger.debug(fv.readableString());

        nodeWeights[currentKey].updateWeightsBy(fv, multiplier);
    }

    public void updateWeightsBy(FeatureVector fv, int currentKey, int previousKey, double multiplier) {
        if (edgeWeights[currentKey][previousKey] == null) {
            addWeightVector(currentKey, previousKey);
        }

//        logger.debug("Updating features for " + classAlphabet.getClassName(currentKey) + " and " + classAlphabet
//                .getClassName(previousKey) + " by " + multiplier);
//        logger.debug(fv.readableString());

        edgeWeights[currentKey][previousKey].updateWeightsBy(fv, multiplier);
    }

    public void updateWeightBy(GraphFeatureVector updateVector, double multiplier) {
        for (TIntObjectIterator<FeatureVector> iter = updateVector.nodeFvIter(); iter.hasNext(); ) {
            iter.advance();
            updateWeightsBy(iter.value(), iter.key(), multiplier);
        }

        for (Iterator<Table.Cell<Integer, Integer, FeatureVector>> iter = updateVector.edgeFvIter(); iter.hasNext(); ) {
            Table.Cell<Integer, Integer, FeatureVector> v = iter.next();
            updateWeightsBy(v.getValue(), v.getRowKey(), v.getColumnKey(), multiplier);
        }
    }

    public double dotProd(FeatureVector fv, int currentKey) {
        AveragedWeightVector weights = nodeWeights[currentKey];
        if (weights != null) {
            return weights.dotProd(fv);
        } else {
            return 0;
        }
    }

    public double dotProdVerbose(FeatureVector fv, int currentKey) {
        ArrayBasedAveragedWeightVector weights = (ArrayBasedAveragedWeightVector) nodeWeights[currentKey];
        if (weights != null) {
//            System.out.println("Get " + currentKey + " success");
//            System.out.println(nodeWeights[currentKey].dotProd(fv));
            return weights.dotProd(fv, featureAlphabet);
        } else {
            return 0;
        }
    }

    public double dotProd(FeatureVector fv, int currentKey, int previousKey) {
        AveragedWeightVector weights = edgeWeights[currentKey][previousKey];
        if (weights != null) {
//            System.out.println("Get " + currentKey + " " + previousKey + " success");
//            System.out.println("Dot prod against " + fv.readableString());
//            System.out.println(edgeWeights[currentKey][previousKey].dotProd(fv));
            return weights.dotProd(fv);
        } else {
            return 0;
        }
    }

    public double dotProdAver(FeatureVector fv, int currentKey) {
        AveragedWeightVector weights = nodeWeights[currentKey];
        if (weights != null) {
            return weights.dotProdAver(fv);
        } else {
            return 0;
        }
    }

    public double dotProdAver(FeatureVector fv, int currentKey, int previousKey) {
        AveragedWeightVector weights = edgeWeights[currentKey][previousKey];
        if (weights != null) {
            return weights.dotProdAver(fv);
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
        logger.info("Consolidating graph weights.");
        applyToAll(AveragedWeightVector::consolidate);
    }

    public void deconsolidate() {
        logger.info("Deconsolidating graph weights.");
        applyToAll(AveragedWeightVector::deconsolidate);
    }

    private void applyToAll(Consumer<AveragedWeightVector> oper) {
        for (AveragedWeightVector nodeWeight : nodeWeights) {
            if (nodeWeight != null) {
                oper.accept(nodeWeight);
            }
        }

        for (AveragedWeightVector[] edgeWeightVectorRow : edgeWeights) {
            for (AveragedWeightVector edgeWeightVector : edgeWeightVectorRow) {
                if (edgeWeightVector != null) {
                    oper.accept(edgeWeightVector);
                }
            }
        }
    }

    // TODO: we can also save all the alphabet here, so we do not need to serialize another object.
    public FeatureAlphabet getFeatureAlphabet() {
        return featureAlphabet;
    }

    public ClassAlphabet getClassAlphabet() {
        return classAlphabet;
    }

    public int getFeatureDimension() {
        return featureAlphabet.getAlphabetSize();
    }

    public void updateAverageWeights() {
        applyToAll(AveragedWeightVector::updateAverageWeight);
        averageUpdateCount++;
    }
}
