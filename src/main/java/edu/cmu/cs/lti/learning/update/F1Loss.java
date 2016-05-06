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
        int ng = 0;
        int ns = 0;

        for (int i = 0; i < gold.length; i++) {
            T g = gold[i];
            T s = sys[i];
            if (!g.equals(noneValue)) {
                if (g.equals(s)) {
                    tp += 1;
                }
                ng += 1;
            }

            if (!s.equals(noneValue)) {
                ns += 1;
            }
        }

        double prec = (double) tp / ns;
        double recall = (double) tp / ng;

        logger.debug("Prec is " + prec + " recall is " + recall);

        if (ng == 0) {
            return 0;
        }
        if (tp == 0) {
            return 1;
        } else {
            return 1 - 2 * prec * recall / (prec + recall);
        }
    }
}
