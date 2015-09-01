package edu.cmu.cs.lti.learning.cache;

/**
 * Created with IntelliJ IDEA.
 * Date: 8/25/15
 * Time: 2:21 PM
 *
 * @author Zhengzhong Liu
 */
public class CrfState extends FeatureCacheKey {
    private String documentKey;
    private int sequenceId;
    private int tokenId;

    public void setDocumentKey(String documentKey) {
        this.documentKey = documentKey;
    }

    public void setSequenceId(int sequenceId) {
        this.sequenceId = sequenceId;
    }

    public void setTokenId(int tokenId) {
        this.tokenId = tokenId;
    }

    public String getDocumentKey() {
        return documentKey;
    }

    public int getSequenceId() {
        return sequenceId;
    }

    public int getTokenId() {
        return tokenId;
    }

    public String toString() {
        return String.format("[%s @ (%d, %d)]", documentKey, sequenceId, tokenId);
    }

}
