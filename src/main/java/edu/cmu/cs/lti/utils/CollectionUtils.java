package edu.cmu.cs.lti.utils;

import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 10/21/14
 * Time: 1:16 PM
 */
public class CollectionUtils {
    public static <T> List<Pair<T, T>> nSkippedBigrams(Collection<T> collection, int n) {
        List<Pair<T, T>> resultBigrams = new ArrayList<>();

        List<Iterator<T>> followerIters = new LinkedList<>();

        for (T second : collection) {
            for (Iterator<T> followerIter : followerIters) {
                resultBigrams.add(Pair.of(followerIter.next(), second));
            }

            if (n >= 0) {
                //add at the begining so that the earlist bigram is output first
                followerIters.add(0, collection.iterator());
                n--;
            }
        }

        return resultBigrams;
    }

    public static <E> List<List<E>> cartesian(List<List<E>> lists) {
        if (lists.size() == 0) {
            return new ArrayList<>();
        }
        if (lists.size() == 1) {
            List<List<E>> result = new ArrayList<>();
            for (E e : lists.get(0)) {
                ArrayList<E> l = new ArrayList<>();
                l.add(e);
                result.add(l);
            }
            return result;
        }

        return cartesian(0, lists);
    }

    private static <E> List<List<E>> cartesian(int index, List<List<E>> lists) {
        List<List<E>> result = new ArrayList<>();
        if (index == lists.size()) {
            result.add(new ArrayList<>());
        } else {
            for (E e : lists.get(index)) {
                for (List<E> list : cartesian(index + 1, lists)) {
                    list.add(e);
                    result.add(list);
                }
            }
        }
        return result;
    }
}
