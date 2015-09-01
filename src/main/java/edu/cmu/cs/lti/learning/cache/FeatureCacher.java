package edu.cmu.cs.lti.learning.cache;

import gnu.trove.map.TObjectDoubleMap;

/**
 * Created with IntelliJ IDEA.
 * Date: 8/25/15
 * Time: 1:34 PM
 *
 * @author Zhengzhong Liu
 */
public abstract class FeatureCacher {

    public abstract TObjectDoubleMap<String>[] getCachedFeatures(FeatureCacheKey key);

    public abstract void addFeaturesToCache(FeatureCacheKey key, TObjectDoubleMap<String>... features);
}
