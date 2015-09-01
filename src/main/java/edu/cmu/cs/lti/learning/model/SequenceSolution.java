package edu.cmu.cs.lti.learning.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * Date: 8/21/15
 * Time: 3:44 PM
 *
 * @author Zhengzhong Liu
 */
public class SequenceSolution extends Solution {
    private static final long serialVersionUID = 4963833442738553688L;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ClassAlphabet classAlphabet;
    private int sequenceLength;
    private int[] solution;
    private double[] previousColumnScores;
    private double[] currColumnScores;
    private int[][] backPointers;

    int currentPosition;

    public SequenceSolution(ClassAlphabet classAlphabet, int[] sequence) {
        this.classAlphabet = classAlphabet;
        this.sequenceLength = sequence.length;
        solution = new int[sequenceLength];
        System.arraycopy(sequence, 0, solution, 0, solution.length);
    }

    public SequenceSolution(ClassAlphabet classAlphabet, int sequenceLength) {
        this.classAlphabet = classAlphabet;
        this.sequenceLength = sequenceLength;
        solution = new int[sequenceLength];
        previousColumnScores = new double[classAlphabet.size()];
        currColumnScores = new double[classAlphabet.size()];
        backPointers = new int[sequenceLength + 1][classAlphabet.size()];
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
        previousColumnScores = currColumnScores;
        currColumnScores = new double[classAlphabet.size()];
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

    public String showBackpointerMap() {
        StringBuilder sb = new StringBuilder();
        String rowSep;
        String colSep = "";
        for (int[] backPointer : backPointers) {
            sb.append(colSep);
            colSep = "\n";
            rowSep = "";
            for (int aBackPointer : backPointer) {
                sb.append(rowSep);
                rowSep = "\t";
                sb.append(aBackPointer);
            }
        }
        return sb.toString();
    }

    public void backTrace() {
        int previousClass = 0;

        for (int backCol = sequenceLength; backCol > 0; backCol--) {
            previousClass = backPointers[backCol][previousClass];
            solution[backCol - 1] = previousClass;
        }
    }

    public int getSequenceLength() {
        return sequenceLength;
    }

    public String toString() {
        return Arrays.stream(solution).mapToObj(classAlphabet::getClassName).collect(Collectors.joining(", "));
    }

    public ClassAlphabet getClassAlphabet() {
        return classAlphabet;
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

    @Override
    public double loss(Solution s) {
        if (getClass() != s.getClass())
            throw new IllegalArgumentException("Must compare with a sequence solution.");

        SequenceSolution otherSolution = (SequenceSolution) s;
        if (otherSolution.sequenceLength != sequenceLength) {
            throw new IllegalArgumentException("Cannot compare two solutions on difference sequences.");
        } else {
            int mismatch = 0;
            int tp = 0;
            int numGold = 0;
            int numSys = 0;
            for (int i = 0; i < sequenceLength; i++) {
                int otherClass = classAlphabet.getNoneOfTheAboveClassIndex();
                if (solution[i] != otherClass) {
                    numGold += 1;
                    if (otherSolution.getClassAt(i) == solution[i]) {
                        tp += 1;
                    }
                }

                if (otherSolution.getClassAt(i) != otherClass) {
                    numSys += 1;
                }

                if (otherSolution.getClassAt(i) != solution[i]) {
                    mismatch += 1;
                }
            }

            double precision = numSys > 0 ? tp * 1.0 / numSys : 1;
            double recall = numGold > 0 ? tp * 1.0 / numGold : 1;

            double f1 = precision + recall == 0 ? 0 : 2 * precision * recall / (precision + recall);

            return 1 - f1;
//            return mismatch * 1.0 / sequenceLength;
        }
    }
}
