package edu.cmu.cs.lti.learning.model;

import org.slf4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * Date: 8/23/15
 * Time: 3:43 PM
 *
 * @author Zhengzhong Liu
 */
public class TrainingStats {
    private int numInstanceProcessed = 0;

    private int averageLossOverN;

    private double recentAccumulatedLoss = 0;

    private int resetableInstanceCount = 0;

    private double overallLoss = 0;

    public TrainingStats(int averageLossOverN) {
        this.averageLossOverN = averageLossOverN;
        reset();
    }

    public void addLoss(Logger logger, double loss) {
        addLoss(loss);

        if (numInstanceProcessed % averageLossOverN == 0) {
            logger.info(String.format("Average loss of previous %d instance is %.3f", averageLossOverN,
                    recentAccumulatedLoss / averageLossOverN));
            recentAccumulatedLoss = 0;
        }
    }

    public void addLoss(double loss) {
        recentAccumulatedLoss += loss;
        overallLoss += loss;
        numInstanceProcessed++;
    }

    public void reset() {
        overallLoss = 0;
        resetableInstanceCount = 0;
    }

    public int getNumberOfInstancesProcessed() {
        return numInstanceProcessed;
    }

    public double getAverageOverallLoss() {
        return overallLoss / resetableInstanceCount;
    }
}
