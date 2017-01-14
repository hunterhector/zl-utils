package edu.cmu.cs.lti.learning.model;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Represent possible node that can be associated on a mention candidate.
 */
public class NodeKey implements Comparable<NodeKey>, Serializable {
    private static final long serialVersionUID = 3461806692881235291L;

    private int begin;
    private int end;
    private String realis;
    private String mentionType;
    private final int candidateIndex;

    public NodeKey(int begin, int end, String mentionType, String realis, int candidateIndex) {
        this.begin = begin;
        this.end = end;
        this.mentionType = mentionType;
        this.realis = realis;
        this.candidateIndex = candidateIndex;
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

    public int getCandidateIndex() {
        return candidateIndex;
    }

    public String toString() {
        return String.format("<Node>_[%d:%d]_[%s,%s]@%d", begin, end, realis, mentionType, candidateIndex);
    }

    @Override
    public int compareTo(NodeKey o) {
        return new CompareToBuilder().append(candidateIndex, o.candidateIndex).build();
    }

    public boolean isRoot() {
        return candidateIndex == -1;
    }
}
