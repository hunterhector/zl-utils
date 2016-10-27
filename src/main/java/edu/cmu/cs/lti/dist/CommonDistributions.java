package edu.cmu.cs.lti.dist;

/**
 * Created with IntelliJ IDEA.
 * Date: 10/17/16
 * Time: 10:50 PM
 *
 * @author Zhengzhong Liu
 */
public class CommonDistributions {
    public static double[] softmax(double[] scores) {
        double normalizer = 0;

        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < scores.length; i++) {
            if (scores[i] > max) {
                max = scores[i];
            }
        }

        double[] dist = new double[scores.length];

        for (int i = 0; i < scores.length; i++) {
            normalizer += Math.exp(scores[i] - max);
            dist[i] = Math.exp(scores[i] - max);
        }

        if (normalizer != 0) {
            for (int i = 0; i < dist.length; i++) {
                dist[i] /= normalizer;
            }
        }

        return dist;
    }
}
