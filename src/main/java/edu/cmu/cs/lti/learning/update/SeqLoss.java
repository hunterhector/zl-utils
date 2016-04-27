package edu.cmu.cs.lti.learning.update;

/**
 * Created with IntelliJ IDEA.
 * Date: 3/13/16
 * Time: 10:52 PM
 *
 * @author Zhengzhong Liu
 */
public abstract class SeqLoss {
    public <T> double compute(T[] gold, T[] sys, T noneValue) {
        if (gold.length == sys.length) {
            return computeInternal(gold, sys, noneValue);
        } else {
            throw new IllegalArgumentException(
                    String.format("Size of the two sequence must be the same to compute loss: %d, %d",
                            gold.length, sys.length));
        }
    }

    public abstract <T> double computeInternal(T[] gold, T[] sys, T noneValue);

    public static SeqLoss getLoss(String lossType) {
        SeqLoss seqLoss;
        switch (lossType) {
            case "f1":
                seqLoss = new F1Loss();
                break;
            case "hamming":
                seqLoss = new HammingLoss();
                break;
            case "recallHamming":
                seqLoss = new RecallHammingLoss();
                break;
            case "noneHamming":
                seqLoss = new NoneHammingLoss();
                break;
            default:
                seqLoss = new HammingLoss();
        }
        return seqLoss;
    }
}
