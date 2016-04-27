package edu.cmu.cs.lti.learning.update;

/**
 * Created with IntelliJ IDEA.
 * Date: 4/26/16
 * Time: 3:44 PM
 *
 * @author Zhengzhong Liu
 */
public class NoneHammingLoss extends SeqLoss {
    double nonePenalty;

    public NoneHammingLoss() {
        this(2);
    }

    public NoneHammingLoss(double nonePenalty) {
        this.nonePenalty = nonePenalty;
    }

    @Override
    public <T> double computeInternal(T[] gold, T[] sys, T noneValue) {
        double hamming = 0;
        for (int i = 0; i < gold.length; i++) {
            if (!gold[i].equals(sys[i])) {
                if (sys[i] == noneValue || gold[i] == noneValue) {
                    hamming += nonePenalty;
                } else {
                    hamming++;
                }
            }
        }
        return hamming;
    }
}
