package edu.cmu.cs.lti.collections;

import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 11/11/14
 * Time: 3:49 PM
 */
public class TLongIntDoubleHashTable {
    TLongObjectHashMap<TIntDoubleMap> table = new TLongObjectHashMap<>();

    public TLongIntDoubleHashTable() {

    }

    /**
     * The data are stored in primitive, returning an object for easy null-check
     *
     * @param rowKey
     * @param colKey
     * @return return the value as Double object
     */
    public Double get(long rowKey, int colKey) {
        TIntDoubleMap row = table.get(rowKey);

        if (row != null) {
            //using no_entry_value in trove sounds dangerous, let's explicitly test
            if (row.containsKey(colKey)) {
                return row.get(colKey);
            }
        }
        return null;
    }

    public void put(long rowKey, int colKey, double value) {
        TIntDoubleMap row = table.get(rowKey);
        if (row != null) {
            row.put(colKey, value);
        } else {
            row = new TIntDoubleHashMap();
            row.put(colKey, value);
            table.put(rowKey, row);
        }
    }

    public boolean contains(long rowKey, int colKey) {
        return table.containsKey(rowKey) && table.get(rowKey).containsKey(colKey);
    }

    public boolean adjust(long rowKey, int colKey, double value) {
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
     * @param putAmount    the value put into the map if the key is not initial present
     * @return the value present in the map after the adjustment or put operation
     */
    public double adjustOrPutValue(long rowKey, int colKey, double adjustAmount, double putAmount) {
        double newValue;
        if (table.containsKey(rowKey)) {
            newValue = table.get(rowKey).adjustOrPutValue(colKey, adjustAmount, putAmount);
        } else {
            TIntDoubleMap row = new TIntDoubleHashMap();
            row.put(colKey, putAmount);
            newValue = putAmount;
            table.put(rowKey, row);
        }
        return newValue;
    }

}
