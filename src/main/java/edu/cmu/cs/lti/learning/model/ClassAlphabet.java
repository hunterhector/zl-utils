package edu.cmu.cs.lti.learning.model;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * Date: 8/20/15
 * Time: 11:07 PM
 *
 * @author Zhengzhong Liu
 */
public class ClassAlphabet {
    // In principle, this alphabet is automatically expandable, but we skip implementing that for now.

    String noneOfTheAboveClass = "NONE";

    TObjectIntMap<String> classIndices = new TObjectIntHashMap<>();
    ArrayList<String> classes;

    public ClassAlphabet(Collection<String> classes) {
        this(classes, false);
    }

    public ClassAlphabet(Collection<String> classes, boolean noneOfTheAbove) {
        int classSize = noneOfTheAbove ? classes.size() : classes.size() + 1;
        this.classes = new ArrayList<String>();

        int index = 0;
        if (noneOfTheAbove) {
            this.classes.add(noneOfTheAboveClass);
            classIndices.put(noneOfTheAboveClass, index);
        }

        for (String c : classes) {
            this.classes.add(c);
            classIndices.put(c, index++);
        }
    }

    public int getClassIndex(String className) {
        return classIndices.get(className);
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

}
