package edu.cmu.cs.lti.learning.model;

import edu.cmu.cs.lti.learning.model.graph.EdgeType;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * Created with IntelliJ IDEA.
 * Date: 4/25/16
 * Time: 6:06 PM
 *
 * @author Zhengzhong Liu
 */
public class EdgeKey implements Comparable<EdgeKey> {
    private NodeKey govNode;

    private NodeKey depNode;

    private EdgeType type;

    public EdgeKey(NodeKey depNode, NodeKey govNode, EdgeType type) {
        if (depNode == null || govNode == null || type == null) {
            throw new IllegalArgumentException("Keys and types cannot be null");
        }

        this.depNode = depNode;
        this.govNode = govNode;
        this.type = type;
    }

    public NodeKey getDepNode() {
        return depNode;
    }

    public NodeKey getGovNode() {
        return govNode;
    }

    public EdgeType getType() {
        return type;
    }

    @Override
    public int compareTo(EdgeKey o) {
        return CompareToBuilder.reflectionCompare(this, o);
    }

    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }

    public String toString() {
        return String.format("%s --%s--> %s", govNode, type.name(), depNode);
    }

}
