package edu.cmu.cs.lti.learning.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created with IntelliJ IDEA.
 * Date: 4/25/16
 * Time: 2:24 PM
 *
 * @author Zhengzhong Liu
 */
public class MultiNodeKey implements Iterable<NodeKey> {
    private List<NodeKey> keys;

    private boolean isRoot;

    private String combinedType;

    public static final String REALIS_ROOT = "ROOT";

    public static final String TYPE_ROOT = "ROOT";

    private static MultiNodeKey root;

    static {
        NodeKey singleRoot = new NodeKey(0, 0, TYPE_ROOT, REALIS_ROOT, -1);
        root = new MultiNodeKey(TYPE_ROOT);
        root.isRoot = true;
        root.keys.add(singleRoot);
    }

    public MultiNodeKey(String combinedType, NodeKey... ks) {
        keys = new ArrayList<>();
        for (NodeKey k : ks) {
            keys.add(k);
        }
        this.combinedType = combinedType;
    }

    public static MultiNodeKey rootKey() {
        return root;
    }

    public Stream<NodeKey> stream() {
        return keys.stream();
    }

    @Override
    public Iterator<NodeKey> iterator() {
        return keys.iterator();
    }

    public boolean isRoot() {
        return isRoot;
    }

    public NodeKey takeFirst() {
        return keys.get(0);
    }

    public int size() {
        return keys.size();
    }

    public List<NodeKey> getKeys() {
        return keys;
    }

    public String getCombinedType() {
        return combinedType;
    }
}
