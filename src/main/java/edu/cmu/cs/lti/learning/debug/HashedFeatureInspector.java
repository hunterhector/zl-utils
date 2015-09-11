package edu.cmu.cs.lti.learning.debug;

import edu.cmu.cs.lti.learning.model.HashAlphabet;
import edu.cmu.cs.lti.learning.model.AveragedWeightVector;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.function.Function;
import java.util.function.ToIntFunction;

/**
 * Created with IntelliJ IDEA.
 * Date: 9/3/15
 * Time: 4:14 PM
 *
 * @author Zhengzhong Liu
 */
public class HashedFeatureInspector {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private HashAlphabet featureAlphabet;
    private AveragedWeightVector averagedWeightVector;
    private ReverseFeatureComparator comp;

    public HashedFeatureInspector(HashAlphabet featureAlphabet,
                                  AveragedWeightVector averagedWeightVector) {
        this.featureAlphabet = featureAlphabet;
        this.averagedWeightVector = averagedWeightVector;
        comp = new ReverseFeatureComparator();
    }

    public void hashDistributionTester(ToIntFunction<String> customMapper) {
        TIntSet slotsOccupied = new TIntHashSet();

        int actualFeatures = 0;

        for (int i = 0; i < averagedWeightVector.getFeatureSize(); i++) {
            String[] featureNames = featureAlphabet.getFeatureNames(i);
            if (featureNames != null) {
                actualFeatures += featureNames.length;
                for (String featureName : featureNames) {
                    slotsOccupied.add(customMapper.applyAsInt(featureName));
                }
            }
        }

        logger.info(String.format("Testing results : Actual features : %d, actual occupied,: %d",
                actualFeatures, slotsOccupied.size()));
    }

    public PriorityQueue<Triple<Integer, String, Double>> loadTopKAverageFeatures(int k) {
        PriorityQueue<Triple<Integer, String, Double>> topK = new PriorityQueue<>(k, comp);
        loadFeatures(averagedWeightVector::getAverageWeightAt, topK, k);
        return topK;
    }

    public PriorityQueue<Triple<Integer, String, Double>> loadTopKFinalFeatures(int k) {
        PriorityQueue<Triple<Integer, String, Double>> topK = new PriorityQueue<>(k, comp);
        loadFeatures(averagedWeightVector::getWeightAt, topK, k);
        return topK;
    }

    public PriorityQueue<Triple<Integer, String, Double>> loadAllAverageFeatures() {
        PriorityQueue<Triple<Integer, String, Double>> all = new PriorityQueue<>();
        loadFeatures(averagedWeightVector::getAverageWeightAt, all, -1);
        return all;
    }

    public PriorityQueue<Triple<Integer, String, Double>> loadAllFinalFeatures() {
        PriorityQueue<Triple<Integer, String, Double>> all = new PriorityQueue<>();
        loadFeatures(averagedWeightVector::getWeightAt, all, -1);
        return all;
    }

    private void loadFeatures(Function<Integer, Double> getWeights,
                              PriorityQueue<Triple<Integer, String, Double>> features, int k) {
        for (int i = 0; i < averagedWeightVector.getFeatureSize(); i++) {
            double weight = getWeights.apply(i);
            if (featureAlphabet.getMappedFeatureCounters(i) != null) {
                features.add(Triple.of(i, featureAlphabet.getMappedFeatureCounters(i), weight));
            }
            if (k > 0 && features.size() > k) {
                features.poll();
            }
        }
    }

    private class ReverseFeatureComparator implements Comparator<Triple<Integer, String, Double>> {
        @Override
        public int compare(Triple<Integer, String, Double> f1, Triple<Integer, String, Double> f2) {
            if (f1.getRight() > f2.getRight()) {
                return 1;
            } else if (f2.getRight() > f1.getRight()) {
                return -1;
            } else {
                return f2.getLeft() - f1.getLeft();
            }
        }
    }

    public void writeInspects(File outputFile, PriorityQueue<Triple<Integer, String, Double>> features) throws
            IOException {
        LinkedList<String> lines = new LinkedList<>();
        while (!features.isEmpty()) {
            Triple<Integer, String, Double> feature = features.poll();
//            if (feature.getRight() > 0) {
            lines.addFirst(String.format("%d\t%s\t%.8f",
                    feature.getLeft(), feature.getMiddle(), feature.getRight()));
//            }
        }
        lines.addFirst("Feature Id\tFeature Name\tFeature Weight");
        FileUtils.writeLines(outputFile, lines);
    }

    public static void main(String[] args) throws IOException {
        String modelDirectory = args[0];
        String outputDirectory = args[1];


        HashAlphabet featureAlphabet = SerializationUtils.deserialize(new FileInputStream(new File(modelDirectory,
                "alphabet")));
        AveragedWeightVector averagedWeightVector = SerializationUtils.deserialize(new FileInputStream(new File
                (modelDirectory, "crfWeights")));

        HashedFeatureInspector inspector = new HashedFeatureInspector(featureAlphabet, averagedWeightVector);

        featureAlphabet.computeConflictRates();

//        HashFunction hasher = Hashing.murmur3_32();
//
//        int k = (int) Math.pow(2, 23);
//
//        System.out.println("K - 1 = " + (k - 1) + " " + Integer.toBinaryString(k - 1));
//
//        System.out.println("Testing hash dist of modulo with k");
//
//        inspector.hashDistributionTester(operand -> {
//            int v = hasher.hashString(operand, Charsets.UTF_8).asInt() % (k);
//            if (v < 0) {
//                v += k;
//            }
//            return v;
//        });
//
//        System.out.println("Testing hash dist of modulo with k-1");
//
//        inspector.hashDistributionTester(operand -> {
//            int v = hasher.hashString(operand, Charsets.UTF_8).asInt() % (k - 1);
//            if (v < 0) {
//                v += (k - 1);
//            }
//            return v;
//        });
//
//        System.out.println("Testing hash dist of AND with k-1");
//
//        inspector.hashDistributionTester(operand -> hasher.hashString(operand, Charsets.UTF_8).asInt() & (k - 1));

        inspector.writeInspects(new File(outputDirectory, "top100Aver"), inspector.loadTopKAverageFeatures(100));
        inspector.writeInspects(new File(outputDirectory, "top100Final"), inspector.loadTopKFinalFeatures(100));
        inspector.writeInspects(new File(outputDirectory, "allAver"), inspector.loadAllAverageFeatures());
        inspector.writeInspects(new File(outputDirectory, "allFinal"), inspector.loadAllFinalFeatures());
    }
}
