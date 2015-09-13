package edu.cmu.cs.lti.learning.model;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.apache.commons.lang3.SerializationUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
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
            index++;
        }
        return index;
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
