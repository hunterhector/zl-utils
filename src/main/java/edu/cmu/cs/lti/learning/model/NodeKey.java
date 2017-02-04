package edu.cmu.cs.lti.learning.model;

import edu.cmu.cs.lti.learning.model.graph.EdgeType;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represent possible node that can be associated on a mention candidate.
 */
public class NodeKey implements Comparable<NodeKey>, Serializable {
    private static final long serialVersionUID = 3461806692881235291L;

    private int begin;
    private int end;
    private String realis;
    private String mentionType;
    private final int nodeIndex;
    private final int keyIndex;

    private static final Map<EdgeType, NodeKey> rootKeys = new HashMap<>();

    static {
        for (EdgeType edgeType : EdgeType.values()) {
            if (!edgeType.equals(EdgeType.Root)) {
                rootKeys.put(edgeType, new NodeKey(0, 0, 0, edgeType.name(), "REALIS_" + edgeType.name(), 0));
            }
        }
    }

    public NodeKey(int begin, int end, int keyIndex, String mentionType, String realis, int nodeIndex) {
        this.begin = begin;
        this.end = end;
        this.mentionType = mentionType;
        this.realis = realis;
        this.nodeIndex = nodeIndex;
        this.keyIndex = keyIndex;
    }

    public static NodeKey getRootKey(EdgeType type) {
        return rootKeys.get(type);
    }

    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(begin).append(end).append(realis).append(mentionType)
                .toHashCode();
    }

    public boolean equals(Object o) {
        if (!(o instanceof NodeKey)) {
            return false;
        }
        NodeKey otherKey = (NodeKey) o;

        return new EqualsBuilder().append(begin, otherKey.begin).append(end, otherKey.end)
                .append(realis, otherKey.realis).append(mentionType, otherKey.mentionType).build();
    }

    public String getRealis() {
        return realis;
    }

    public int getBegin() {
        return begin;
    }

    public int getEnd() {
        return end;
    }

    public String getMentionType() {
        return mentionType;
    }

    public int getNodeIndex() {
        return nodeIndex;
    }

    public int getKeyIndex() {
        return keyIndex;
    }

    public Pair<Integer, Integer> getFullIndex() {
        return Pair.of(nodeIndex, keyIndex);
    }

    public Pair<Integer, String> getTypedIndex() {
        return Pair.of(nodeIndex, mentionType);
    }

    public String toString() {
        return String.format("<Node>_[%d:%d]_[%s,%s]@%d", begin, end, realis, mentionType, nodeIndex);
    }

    @Override
    public int compareTo(NodeKey o) {
        return new CompareToBuilder().append(nodeIndex, o.nodeIndex).append(keyIndex, o.keyIndex).build();
    }

    public boolean isRoot() {
        return nodeIndex == 0;
    }
}
