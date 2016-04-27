package edu.cmu.cs.lti.learning.update;

/**
 * Created with IntelliJ IDEA.
 * Date: 4/26/16
 * Time: 3:36 PM
 *
 * @author Zhengzhong Liu
 */
public class RecallHammingLoss extends SeqLoss {

    double recallPenalty;

    public RecallHammingLoss() {
        this(2);
    }

    public RecallHammingLoss(double recallPenalty) {
        this.recallPenalty = recallPenalty;
    }

    @Override
    public <T> double computeInternal(T[] gold, T[] sys, T noneValue) {
        double hamming = 0;
        for (int i = 0; i < gold.length; i++) {
            if (!gold[i].equals(sys[i])) {
                if (sys[i] == noneValue) {
                    hamming += recallPenalty;
                } else {
                    hamming++;
                }
            }
        }
        return hamming;
    }
}
