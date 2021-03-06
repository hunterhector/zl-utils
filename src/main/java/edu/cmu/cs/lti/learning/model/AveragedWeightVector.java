package edu.cmu.cs.lti.learning.model;

import gnu.trove.iterator.TIntDoubleIterator;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;

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

    public double dotProd(FeatureVector fv) {
        double sum = 0;
        for (FeatureVector.FeatureIterator iter = fv.featureIterator(); iter.hasNext(); ) {
            iter.next();
            sum += getWeightAt(iter.featureIndex()) * iter.featureValue();
//            System.out.println(iter.featureIndex() + " " + getWeightAt(iter.featureIndex()) + " " + iter
// .featureValue());
        }
//        System.out.println(sum);
        return sum;
    }

    public double dotProdAver(FeatureVector fv) {
        double sum = 0;
        for (FeatureVector.FeatureIterator iter = fv.featureIterator(); iter.hasNext(); ) {
            iter.next();
            double w = getAverageWeightAt(iter.featureIndex()) * iter.featureValue();
            sum += w;
//            if (iter.featureValue() * w != 0) {
//                System.out.println(String.format("%s score is %.2f", fv.getAlphabet().getFeatureNameRepre(iter
// .featureIndex()), iter.featureValue() * w));
//            }
        }
        return sum;
    }

    public double dotProdAverDebug(FeatureVector fv, Logger logger) {
        double sum = 0;
        StringBuilder positives = new StringBuilder();
        positives.append("Showing positive weights:\n");
        StringBuilder negatives = new StringBuilder();
        negatives.append("Showing negative weights:\n");
        for (FeatureVector.FeatureIterator iter = fv.featureIterator(); iter.hasNext(); ) {
            iter.next();
            double weight = getAverageWeightAt(iter.featureIndex());
            double r = weight * iter.featureValue();
            sum += r;
            String debugStr = String.format("%d %s : %.2f x %.4f = %.4f\n", iter.featureIndex(), fv.getAlphabet()
                    .getFeatureNameRepre(iter.featureIndex()), iter.featureValue(), weight, r);

            if (weight > 0) {
                positives.append(debugStr);
            } else if (weight < 0) {
                negatives.append(debugStr);
            }
        }

        logger.info("Showing feature weights...");
        logger.info(positives.toString());
        logger.info(negatives.toString());

        logger.info(String.format("The final score is %.4f.", sum));

        return sum;
    }

    public String toReadableString(FeatureAlphabet alphabet) {
        StringBuilder sb = new StringBuilder();

        for (TIntDoubleIterator iter = getWeightsIterator(); iter.hasNext(); ) {
            iter.advance();
            if (iter.value() != 0) {
                sb.append(String.format("%s : %.2f\n", alphabet.getFeatureNameRepre(iter.key()), iter.value()));
            }
        }
        return sb.toString();
    }

    abstract void consolidate();

    abstract void deconsolidate();

    public abstract double getWeightAt(int i);

    public abstract double getAverageWeightAt(int i);

    public abstract int getFeatureSize();

    public abstract TIntDoubleIterator getWeightsIterator();

    public abstract TIntDoubleIterator getAverageWeightsIterator();
}
