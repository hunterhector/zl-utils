package edu.cmu.cs.lti.learning.model;

/**
 * Created with IntelliJ IDEA.
 * Date: 8/21/15
 * Time: 3:36 PM
 *
 * @author Zhengzhong Liu
 */
public abstract class Solution implements Comparable {
    double score = Double.NEGATIVE_INFINITY;

    protected void setScore(double score) {
        this.score = score;
    }

    public abstract boolean equals(Object s);

    public int compareTo(Object s) {
        if (s == null) {
            return -1;
        }
        return ((score - ((Solution) s).score) > 0) ? 1 : -1;
    }

}
