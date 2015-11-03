package edu.cmu.cs.lti.learning.model;

import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

/**
 * Created with IntelliJ IDEA.
 * Date: 8/20/15
 * Time: 10:54 PM
 *
 * @author Zhengzhong Liu
 */
public class BinaryHashFeatureVector extends FeatureVector {
    private static final long serialVersionUID = -5564329295580504903L;

    TIntIntMap fv;

    public BinaryHashFeatureVector(FeatureAlphabet alphabet) {
        super(alphabet);
        fv = new TIntIntHashMap();
    }

    @Override
    public boolean addFeatureInternal(int featureIndex, double featureValue) {
        int adjustedValue = fv.adjustOrPutValue(featureIndex, 1, 1);
        return adjustedValue == featureValue;
    }

    @Override
    public double getFeatureValue(int featureIndex) {
        // Note that the no entry default of trove is 0.
        return fv.get(featureIndex);
    }

    public FeatureVector newVector() {
        return new BinaryHashFeatureVector(alphabet);
    }

    @Override
    public FeatureIterator featureIterator() {
        final TIntIntIterator iter = fv.iterator();
        return new FeatureIterator() {
            @Override
            public int featureIndex() {
                return iter.key();
            }

            @Override
            public double featureValue() {
                return iter.value();
            }

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public void next() {
                iter.advance();
            }
        };
    }
}