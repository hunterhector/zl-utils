package edu.cmu.cs.lti.learning.cache;

import edu.cmu.cs.lti.learning.model.FeatureVector;

/**
 * Created with IntelliJ IDEA.
 * Date: 8/25/15
 * Time: 1:34 PM
 *
 * @author Zhengzhong Liu
 */
public abstract class FeatureCacher {

    public abstract FeatureVector[] getCachedFeatures(FeatureCacheKey key);

    public abstract void addFeaturesToCache(FeatureCacheKey key, FeatureVector... features);
}
