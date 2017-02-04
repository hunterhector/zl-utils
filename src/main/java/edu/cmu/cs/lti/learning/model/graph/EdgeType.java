package edu.cmu.cs.lti.learning.model.graph;

/**
 * Created with IntelliJ IDEA.
 * Date: 4/27/16
 * Time: 4:50 PM
 *
 * @author Zhengzhong Liu
 */
public enum EdgeType {
    Root, Coreference, After, Subevent;

    public static EdgeType[] getNormalTypes() {
        return new EdgeType[]{Coreference, After, Subevent};
    }

    public static boolean isRootType(EdgeType t) {
        return t == Root;
    }
}
