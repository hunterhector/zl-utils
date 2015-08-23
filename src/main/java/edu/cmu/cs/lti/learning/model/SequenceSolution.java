package edu.cmu.cs.lti.learning.model;

/**
 * Created with IntelliJ IDEA.
 * Date: 8/21/15
 * Time: 3:44 PM
 *
 * @author Zhengzhong Liu
 */
public class SequenceSolution extends Solution {
    ClassAlphabet classAlphabet;
    int sequenceLength;
    int[] solution;
    double[] previousColumnScores;
    double[] currColumnScores;
    int[][] backPointers;

    boolean isStart;
    boolean solutionFilled;

    int currentPosition;

    public SequenceSolution(ClassAlphabet classAlphabet, int[] sequence) {
        this.classAlphabet = classAlphabet;
        this.sequenceLength = sequence.length;
        System.arraycopy(sequence, 0, solution, 0, solution.length);
        solutionFilled = true;
    }

    public SequenceSolution(ClassAlphabet classAlphabet, int sequenceLength) {
        this.classAlphabet = classAlphabet;
        this.sequenceLength = sequenceLength;
        solution = new int[sequenceLength];
        previousColumnScores = new double[classAlphabet.size()];
        currColumnScores = new double[classAlphabet.size()];
        backPointers = new int[sequenceLength + 1][classAlphabet.size()];
        isStart = true;
        solutionFilled = false;
        currentPosition = -1;
    }

    public boolean finished() {
        return currentPosition == sequenceLength;
    }

    public int getNumClasses() {
        if (currentPosition == -1 || currentPosition == sequenceLength) {
            return 1;
        } else {
            return classAlphabet.size();
        }
    }

    public void advance() {
        ++currentPosition;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public int getClassAt(int classIndex) {
        return solution[classIndex];
    }

    public double getPreviousScore(int classIndex) {
        return previousColumnScores[classIndex];
    }

    public void setCurrentScoreAt(int classIndex, double score) {
        currColumnScores[classIndex] = score;
    }

    public void setBackpointer(int classIndex, int previousState) {
        backPointers[currentPosition][classIndex] = previousState;
    }

    public void backTrace() {
        int previousClass = 0;
        for (int backCol = sequenceLength; backCol > 0; backCol++) {
            previousClass = backPointers[backCol][previousClass];
            solution[backCol - 1] = previousClass;
        }
        solutionFilled = true;
    }

    public int getSequenceLength() {
        return sequenceLength;
    }

    @Override
    public boolean equals(Object s) {
        if (s == null) {
            return false;
        }

        if (getClass() != s.getClass())
            return false;

        SequenceSolution otherSolution = (SequenceSolution) s;

        if (otherSolution.sequenceLength != sequenceLength) {
            throw new IllegalArgumentException("Cannot compare two solution on difference sequences.");
        } else {
            for (int i = 0; i < sequenceLength; i++) {
                if (otherSolution.getClassAt(i) != solution[i]) {
                    return false;
                }
            }
        }

        return true;
    }
}
