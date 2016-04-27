package edu.cmu.cs.lti.learning.update;

/**
 * Created with IntelliJ IDEA.
 * Date: 4/26/16
 * Time: 3:34 PM
 *
 * @author Zhengzhong Liu
 */
public class HammingLoss extends SeqLoss{
    @Override
    public <T> double computeInternal(T[] gold, T[] sys, T noneValue) {
        double diff = 0;
        for (int i = 0; i < gold.length; i++) {
            if (!gold[i].equals(sys[i])){
                diff += 1;
            }
        }
        return diff;
    }
}
