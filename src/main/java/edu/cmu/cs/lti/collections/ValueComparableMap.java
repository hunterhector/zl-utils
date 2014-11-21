package edu.cmu.cs.lti.collections;

import com.google.common.base.Functions;
import com.google.common.collect.Ordering;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ValueComparableMap<K extends Comparable<K>, V> extends TreeMap<K, V> {
    private static final long serialVersionUID = -4413958550259188958L;
    //A map for doing lookups on the keys for comparison so we don't get infinite loops
    private final Map<K, V> valueMap;

    public ValueComparableMap(final Ordering<? super V> partialValueOrdering) {
        this(partialValueOrdering, new HashMap<K, V>());
    }

    public ValueComparableMap(Ordering<? super V> partialValueOrdering,
                               HashMap<K, V> valueMap) {
        super(partialValueOrdering //Apply the value ordering
                .onResultOf(Functions.forMap(valueMap)) //On the result of getting the value for the key from the map
                .compound(Ordering.natural())); //as well as ensuring that the keys don't get clobbered
        this.valueMap = valueMap;
    }

    public V put(K k, V v) {
        V removedV = null;
        if (valueMap.containsKey(k)) {
            //remove the key in the sorted set before adding the key again
            removedV = remove(k);
        }
        valueMap.put(k, v); //To get "real" unsorted values for the comparator
        super.put(k, v);
        return removedV;
//        return super.put(k, v); //Put it in value order
    }

    public static void main(String[] args) {
        TreeMap<String, Integer> map = new ValueComparableMap<String, Integer>(Ordering.natural());
        map.put("a", 5);
        map.put("b", 1);
        map.put("c", 3);
        map.put("d", 0);
        //ensure it's still a map (by overwriting a key, but with a new value)
        map.put("d", 2);
        //Ensure multiple values do not clobber keys
        map.put("e", 2);

        //check whether put will return the previous value
        Integer v = map.put("a", 1);

        System.out.println(v);

        while (!map.isEmpty()) {
            Map.Entry<String, Integer> firstEntry = map.pollFirstEntry();
            System.out.println(firstEntry.getKey() + " " + firstEntry.getValue());
        }
    }
}
