package edu.cmu.cs.lti.learning.model;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.apache.commons.lang3.SerializationUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Created with IntelliJ IDEA.
 * Date: 8/20/15
 * Time: 11:07 PM
 *
 * @author Zhengzhong Liu
 */
public class ClassAlphabet implements Serializable {
    private static final long serialVersionUID = -4677347415571534910L;

    String noneOfTheAboveClass = "NONE";

    String outsideClass = "OUTSIDE";

    TObjectIntMap<String> classIndices = new TObjectIntHashMap<>();
    ArrayList<String> classes;
    ArrayList<List<String>> backoffClassNames;
    int index;

    public ClassAlphabet(boolean noneOfTheAbove, boolean withOutsideClass) {
        this(new String[0], noneOfTheAbove, withOutsideClass);
    }

    public ClassAlphabet() {
        this(new String[0]);
    }

    public ClassAlphabet(String[] classes) {
        this(classes, false, false);
    }

    public ClassAlphabet(String[] classes, boolean noneOfTheAbove, boolean withOutsideClass) {
        this.classes = new ArrayList<>();
        this.backoffClassNames = new ArrayList<>();
        index = 0;

        if (withOutsideClass) {
            addClass(outsideClass);
        }

        if (noneOfTheAbove) {
            addClass(noneOfTheAboveClass);
        }

        for (String c : classes) {
            addClass(c);
        }
    }

    public int addClass(String className) {
        if (!classIndices.containsKey(className)) {
            classes.add(className);
            classIndices.put(className, index);
            backoffClassNames.add(splitAsClassNames(className));
            index++;
        }
        return index;
    }

    /**
     * // TODO this should be placed in a more specific alphabet class.
     * <p>
     * Split class names into subclass names. The full class name might be a concatenation of multiple classes and
     * might contain hierarchy. The concatenation is represented by ";" and the hierarchy is represented as "_".
     * This method will split a class name of "A_B;C_D" in the following:
     * <p>
     * A_B;C_D The full class name itself
     * A_B  The individual class name
     * C_D  The individual class name
     * A    The highest hierarchy of each class
     * C    The highest hierarchy of each class
     *
     * @param fullClassName
     * @return
     */
    private List<String> splitAsClassNames(String fullClassName) {
        // Add multiple class features by decompose the class.
        List<String> classNames = new ArrayList<>();

//        classNames.add(fullClassName);
        String[] individualClassNames = fullClassName.split(" ; ");
        if (individualClassNames.length > 1) {
            // The current full class name itself.
            classNames.add(fullClassName);
        }
        for (String individualClassName : individualClassNames) {
            classNames.add(individualClassName);
            String[] typeSubType = individualClassName.split("_");
            if (typeSubType.length == 2) {
                classNames.add(typeSubType[0]);
            }
        }
        return classNames;
    }

    public int getClassIndex(String className) {
        if (classIndices.containsKey(className)) {
            return classIndices.get(className);
        } else {
            return addClass(className);
        }
    }

    public String getClassName(int classIndex) {
        return classes.get(classIndex);
    }

    public List<String> getSplittedClassName(int classIndex) {
        return backoffClassNames.get(classIndex);
    }

    public int size() {
        return classes.size();
    }

    public String getNoneOfTheAboveClass() {
        return noneOfTheAboveClass;
    }

    public int getNoneOfTheAboveClassIndex() {
        return classIndices.get(noneOfTheAboveClass);
    }

    public String getOutsideClass() {
        return outsideClass;
    }

    public int getOutsideClassIndex() {
        return classIndices.get(outsideClass);
    }

    public IntStream getNormalClassesRange() {
        return IntStream.range(1, classes.size());
    }

    public IntStream getOutsideClassRange() {
        return IntStream.range(0, 1);
    }

    public void write(File outputFile) throws FileNotFoundException {
        SerializationUtils.serialize(this, new FileOutputStream(outputFile));
    }
}
