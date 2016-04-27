package edu.cmu.cs.lti.learning.update;

/**
 * Created with IntelliJ IDEA.
 * Date: 3/13/16
 * Time: 10:53 PM
 *
 * @author Zhengzhong Liu
 */
public class F1Loss extends SeqLoss {
    private double tp;
    private double fp;
    private double numGold;

    @Override
    public <T> double computeInternal(T[] gold, T[] sys, T noneValue) {
        int tp = 0;

        for (int i = 0; i < gold.length; i++) {
            T g = gold[i];
            T s = sys[i];
            if (g.equals(s)) {
                tp += 1;
            }
        }

        double prec = (double) tp / sys.length;
        double recall = (double) tp / gold.length;

        return 2 * prec * recall / (prec + recall);
    }
}
