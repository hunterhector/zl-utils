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

            FeatureVector thisUniKeyFv;

            if (unikeyFv.containsKey(addKey)) {
                thisUniKeyFv = unikeyFv.get(addKey);
            } else {
                thisUniKeyFv = newFeatureVector();
                unikeyFv.put(addKey, thisUniKeyFv);
            }
            thisUniKeyFv.extend(addVector);
        }

        for (Iterator<Table.Cell<Integer, Integer, FeatureVector>> toAddBiIter = vectorToAdd.bikeyIter();
             toAddBiIter.hasNext(); ) {
            Table.Cell<Integer, Integer, FeatureVector> toAddBiKeyFeature = toAddBiIter.next();
            int row = toAddBiKeyFeature.getRowKey();
            int col = toAddBiKeyFeature.getColumnKey();
            FeatureVector toAddVector = toAddBiKeyFeature.getValue();

            FeatureVector thisBiKeyFv;

            if (bikeyFv.contains(row, col)) {
                thisBiKeyFv = bikeyFv.get(row, col);
            } else {
                thisBiKeyFv = newFeatureVector();
                bikeyFv.put(row, col, thisBiKeyFv);
            }
            thisBiKeyFv.extend(toAddVector);
        }
    }

    // TODO check correctness before use.
    public void diff(BiKeyFeatureVector vectorToDiff, BiKeyFeatureVector resultVector) {
        for (TIntObjectIterator<FeatureVector> toDiffUniIter = vectorToDiff.unikeyIter(); toDiffUniIter.hasNext(); ) {
            toDiffUniIter.advance();
            FeatureVector thisUnikeyFv = unikeyFv.get(toDiffUniIter.key());
            FeatureVector diffResultFv = newFeatureVector();
            thisUnikeyFv.diff(toDiffUniIter.value(), diffResultFv);
            resultVector.unikeyFv.put(toDiffUniIter.key(), diffResultFv);
        }

        for (Iterator<Table.Cell<Integer, Integer, FeatureVector>> toDiffBiIter = vectorToDiff.bikeyIter();
             toDiffBiIter.hasNext(); ) {
            Table.Cell<Integer, Integer, FeatureVector> toDiffCell = toDiffBiIter.next();
            FeatureVector thisBikeyFv = bikeyFv.get(toDiffCell.getRowKey(), toDiffCell.getColumnKey());
            FeatureVector diffResultFv = newFeatureVector();
            thisBikeyFv.diff(toDiffCell.getValue(), diffResultFv);
            resultVector.bikeyFv.put(toDiffCell.getRowKey(), toDiffCell.getColumnKey(), diffResultFv);
        }
    }

    public boolean isBinary() {
        return isBinary;
    }

    public ClassAlphabet getClassAlphabet() {
        return classAlphabet;
    }

    public FeatureAlphabet getFeatureAlphabet() {
        return featureAlphabet;
    }

    public String readableUniVector() {
        StringBuilder sb = new StringBuilder();
        for (TIntObjectIterator<FeatureVector> iter = unikeyIter(); iter.hasNext(); ) {
            iter.advance();
            sb.append("Feature at class ").append(classAlphabet.getClassName(iter.key())).append("\n");
            sb.append(iter.value().readableString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
