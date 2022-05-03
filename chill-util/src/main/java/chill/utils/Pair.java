package chill.utils;

public class Pair<T1, T2> {
    public T1 first;
    public T2 second;
    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }
    public static <T3, T4> Pair<T3, T4> of(T3 first, T4 second){
        return new Pair<>(first, second);
    }
}
