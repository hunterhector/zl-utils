package edu.cmu.cs.lti.learning.model;

import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.hash.TIntDoubleHashMap;

/**
 * Created with IntelliJ IDEA.
 * Date: 8/22/15
 * Time: 4:38 PM
 *
 * @author Zhengzhong Liu
 */
public class RealValueHashFeatureVector extends FeatureVector {

    private static final long serialVersionUID = -8434459870299460601L;

    TIntDoubleMap fv;

    public RealValueHashFeatureVector(FeatureAlphabet alphabet) {
        super(alphabet);
        fv = new TIntDoubleHashMap();
    }

    @Override
    protected boolean addFeatureInternal(int featureIndex, double featureValue) {
        double adjustedValue = fv.adjustOrPutValue(featureIndex, featureValue, featureValue);
        return adjustedValue == featureValue;
    }

    @Override
    public double getFeatureValue(int featureIndex) {
        // Note that the no entry default of trove is 0.
        return fv.get(featureIndex);
    }

    @Override
    public FeatureIterator featureIterator() {
        TIntDoubleIterator iter = fv.iterator();
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
