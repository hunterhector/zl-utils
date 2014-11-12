package edu.cmu.cs.lti.collections;

import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.iterator.TShortDoubleIterator;
import gnu.trove.map.TShortDoubleMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.map.hash.TShortDoubleHashMap;

/**
 * Created with ShortelliJ IDEA.
 * User: zhengzhongliu
 * Date: 11/11/14
 * Time: 3:49 PM
 */
public class TLongShortDoubleHashTable {
    TLongObjectHashMap<TShortDoubleMap> table = new TLongObjectHashMap<>();

    public TLongShortDoubleHashTable() {

    }

    public int getNumRows() {
        return table.size();
    }

    /**
     * The data are stored in primitive, returning an object for easy null-check
     *
     * @param rowKey
     * @param colKey
     * @return return the value as Double object
     */
    public Double get(long rowKey, short colKey) {
        TShortDoubleMap row = table.get(rowKey);

        if (row != null) {
            //using no_entry_value in trove sounds dangerous, let's explicitly test
            if (row.containsKey(colKey)) {
                return row.get(colKey);
            }
        }
        return null;
    }

    public TShortDoubleMap getRow(long rowKey) {
        return table.get(rowKey);
    }

    public void put(long rowKey, short colKey, double value) {
        TShortDoubleMap row = table.get(rowKey);
        if (row != null) {
            row.put(colKey, value);
        } else {
            row = new TShortDoubleHashMap();
            row.put(colKey, value);
            table.put(rowKey, row);
        }
    }

    public boolean contains(long rowKey, short colKey) {
        return table.containsKey(rowKey) && table.get(rowKey).containsKey(colKey);
    }

    public boolean containsRow(long rowKey) {
        return table.containsKey(rowKey);
    }

    public boolean adjust(long rowKey, short colKey, double value) {
        if (contains(rowKey, colKey)) {
            table.get(rowKey).adjustValue(colKey, value);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Adjusts the primitive value mapped to the key if the key pair is present in the map. Otherwise, the initial_value is put in the map.
     *
     * @param rowKey       the row key of the value to increment
     * @param colKey       the column key of the value to increment
     * @param adjustAmount the amount to increment the value by
     * @param putAmount    the value put shorto the map if the key is not initial present
     * @return the value present in the map after the adjustment or put operation
     */
    public double adjustOrPutValue(long rowKey, short colKey, double adjustAmount, double putAmount) {
        double newValue;
        if (table.containsKey(rowKey)) {
            newValue = table.get(rowKey).adjustOrPutValue(colKey, adjustAmount, putAmount);
        } else {
            TShortDoubleMap row = new TShortDoubleHashMap();
            row.put(colKey, putAmount);
            newValue = putAmount;
            table.put(rowKey, row);
        }
        return newValue;
    }

    public TLongObjectIterator<TShortDoubleMap> iterator() {
        return table.iterator();
    }

    public void clear() {
        table.clear();
    }

    public double dotProd(TLongShortDoubleHashTable features) {
        double dotProd = 0;
        for (TLongObjectIterator<TShortDoubleMap> firstLevelIter = features.iterator(); firstLevelIter.hasNext(); ) {
            firstLevelIter.advance();
            long featureRowKey = firstLevelIter.key();
            if (table.containsKey(featureRowKey)) {
                TShortDoubleMap weightsRow = table.get(featureRowKey);
                TShortDoubleMap secondLevelFeatures = firstLevelIter.value();
                for (TShortDoubleIterator secondLevelIter = secondLevelFeatures.iterator(); secondLevelIter.hasNext(); ) {
                    secondLevelIter.advance();
                    if (weightsRow.containsKey(secondLevelIter.key())) {
                        dotProd += secondLevelIter.value() * weightsRow.get(secondLevelIter.key());
                    }
                }
            }
        }
        return dotProd;
    }

    public void multiplyBy(double weight) {
        for (TLongObjectIterator<TShortDoubleMap> firstLevelIter = table.iterator(); firstLevelIter.hasNext(); ) {
            firstLevelIter.advance();
            TShortDoubleMap row = firstLevelIter.value();
            for (TShortDoubleIterator rowIter = row.iterator(); rowIter.hasNext(); ) {
                rowIter.advance();
                rowIter.setValue(rowIter.value() * weight);
            }
        }
    }

    public void minusBy(TLongShortDoubleHashTable minusVec) {
        for (TLongObjectIterator<TShortDoubleMap> firstLevelIter = minusVec.iterator(); firstLevelIter.hasNext(); ) {
            firstLevelIter.advance();
            long featureRowKey = firstLevelIter.key();
            if (table.containsKey(featureRowKey)) {
                TShortDoubleMap weightsRow = table.get(featureRowKey);
                for (TShortDoubleIterator secondLevelIter = firstLevelIter.value().iterator(); secondLevelIter.hasNext(); ) {
                    secondLevelIter.advance();
                    weightsRow.adjustOrPutValue(secondLevelIter.key(), -secondLevelIter.value(), -secondLevelIter.value());
                }
            } else {
                for (TShortDoubleIterator secondLevelIter = firstLevelIter.value().iterator(); secondLevelIter.hasNext(); ) {
                    secondLevelIter.advance();
                    TShortDoubleMap newMap = new TShortDoubleHashMap();
                    newMap.put(secondLevelIter.key(), -secondLevelIter.value());
                    table.put(featureRowKey, newMap);
                }
            }
        }
    }

}
