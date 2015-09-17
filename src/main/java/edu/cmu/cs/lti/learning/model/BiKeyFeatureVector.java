package edu.cmu.cs.lti.learning.model;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.Serializable;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * Date: 9/16/15
 * Time: 8:36 PM
 *
 * @author Zhengzhong Liu
 */
public class BiKeyFeatureVector implements Serializable {
    private static final long serialVersionUID = -8364410793971904933L;

    private TIntObjectMap<FeatureVector> unikeyFv;

    private Table<Integer, Integer, FeatureVector> bikeyFv;

    private FeatureAlphabet featureAlphabet;

    private ClassAlphabet classAlphabet;

    private boolean isBinary;

    public BiKeyFeatureVector(ClassAlphabet classAlphabet, FeatureAlphabet featureAlphabet) {
        this(classAlphabet, featureAlphabet, false);
    }

    public BiKeyFeatureVector(ClassAlphabet classAlphabet, FeatureAlphabet featureAlphabet, boolean isBinary) {
        int classSize = classAlphabet.size();
        unikeyFv = new TIntObjectHashMap<>();
        bikeyFv = HashBasedTable.create();
        this.featureAlphabet = featureAlphabet;
        this.classAlphabet = classAlphabet;
        this.isBinary = isBinary;
    }

    public TIntObjectIterator<FeatureVector> unikeyIter() {
        return unikeyFv.iterator();
    }

    public Iterator<Table.Cell<Integer, Integer, FeatureVector>> bikeyIter() {
        return bikeyFv.cellSet().iterator();
    }

    private FeatureVector newFeatureVector() {
        return isBinary ? new BinaryHashFeatureVector(featureAlphabet) : new RealValueHashFeatureVector
                (featureAlphabet);
    }

    public void extend(FeatureVector fv, int primaryKey, int secondaryKey) {
        FeatureVector thisFv;
        if (bikeyFv.contains(primaryKey, secondaryKey)) {
            thisFv = bikeyFv.get(primaryKey, secondaryKey);
        } else {
            thisFv = newFeatureVector();
            bikeyFv.put(primaryKey, secondaryKey, thisFv);
        }
        thisFv.extend(fv);
    }

    public void extend(FeatureVector fv, int primaryKey) {
        FeatureVector thisFv;
        if (unikeyFv.containsKey(primaryKey)) {
            thisFv = unikeyFv.get(primaryKey);

        } else {
            thisFv = newFeatureVector();
            unikeyFv.put(primaryKey, thisFv);
        }
        thisFv.extend(fv);
    }

    public void extend(BiKeyFeatureVector vectorToAdd) {
        for (TIntObjectIterator<FeatureVector> toAddUniIter = vectorToAdd.unikeyIter(); toAddUniIter.hasNext(); ) {
            toAddUniIter.advance();
            int addKey = toAddUniIter.key();
            FeatureVector addVector = toAddUniIter.value();
            FeatureVector thisUniKeyFv = unikeyFv.containsKey(addKey) ? unikeyFv.get(addKey) : newFeatureVector();
            thisUniKeyFv.extend(addVector);
        }

        for (Iterator<Table.Cell<Integer, Integer, FeatureVector>> toAddBiIter = vectorToAdd.bikeyIter();
             toAddBiIter.hasNext(); ) {
            Table.Cell<Integer, Integer, FeatureVector> toAddBiKeyFeature = toAddBiIter.next();
            int row = toAddBiKeyFeature.getRowKey();
            int col = toAddBiKeyFeature.getColumnKey();
            FeatureVector toAddVector = toAddBiKeyFeature.getValue();
            FeatureVector thisBiKeyFv = bikeyFv.contains(row, col) ? bikeyFv.get(row, col) : newFeatureVector();
            thisBiKeyFv.extend(toAddVector);
        }
    }

//    public void diff(BiKeyFeatureVector vectorToDiff, BiKeyFeatureVector resultVector) {
//        for (int unikey = 0; unikey < vectorToDiff.unikeyFv.length; unikey++) {
//            FeatureVector thisUnikeyFv = unikeyFv[unikey];
//            resultVector.unikeyFv[unikey] = newFeatureVector();
//            thisUnikeyFv.diff(vectorToDiff.unikeyFv[unikey], resultVector.unikeyFv[unikey]);
//        }
//
//        for (int key1 = 0; key1 < vectorToDiff.bikeyFv.length; key1++) {
//            for (int key2 = 0; key2 < vectorToDiff.bikeyFv[key1].length; key2++) {
//                FeatureVector thisBikeyFv = bikeyFv[key1][key2];
//                resultVector.bikeyFv[key1][key2] = newFeatureVector();
//                thisBikeyFv.diff(vectorToDiff.bikeyFv[key1][key2], resultVector.bikeyFv[key1][key2]);
//            }
//        }
//    }

    public boolean isBinary() {
        return isBinary;
    }

    public ClassAlphabet getClassAlphabet() {
        return classAlphabet;
    }

    public FeatureAlphabet getFeatureAlphabet() {
        return featureAlphabet;
    }
}
