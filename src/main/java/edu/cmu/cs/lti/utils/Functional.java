package edu.cmu.cs.lti.utils;

/**
 * Created with IntelliJ IDEA.
 * Date: 9/19/15
 * Time: 1:41 PM
 *
 * @author Zhengzhong Liu
 */
public class Functional {
    // Do not use custom functional interface in j1.6 version.

//    @FunctionalInterface
//    public interface TriFunction<T, U, S, R> {
//        R apply(T t, U u, S s);
//
//        default <V> TriFunction<T, U, S, V> andThen(
//                Function<? super R, ? extends V> after) {
//            Objects.requireNonNull(after);
//            return (T t, U u, S s) -> after.apply(apply(t, u, s));
//        }
//    }
}
