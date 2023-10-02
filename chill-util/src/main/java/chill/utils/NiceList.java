package chill.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.*;
import java.util.stream.Stream;

public class NiceList<T> implements List<T>{

    private final List<T> delegate;

    private NiceList(boolean delegateFlag, List<T> wrapped) {
        delegate = wrapped;
    }

    public NiceList() {
        delegate = new LinkedList<>();
    }

    public NiceList(Collection<T> c) {
        delegate = new LinkedList<>(c);
    }

    public NiceList(Iterable<T> c) {
        delegate = new LinkedList<>();
        for (T t : c) {
            delegate.add(t);
        }
    }

    public NiceList(T[] c) {
        delegate = new LinkedList<>();
        for (T t : c) {
            delegate.add(t);
        }
    }

    public static <T> NiceList<T> wrap(List<T> toWrap) {
        NiceList<T> wrapper = new NiceList<>(true, toWrap);
        return wrapper;
    }

    public static NiceList<?> of(Object... value) {
        NiceList<Object> list = new NiceList<>();
        for (Object o : value) {
            list.add(o);
        }
        return list;
    }

    public static NiceList<?> of(Object value) {
        NiceList<?> values = maybeOf(value);
        if (values == null) {
            NiceList list = new NiceList<>();
            list.add(value);
            return list;
        } else {
            return values;
        }
    }

    public static <T> NiceList<T> filled(T elt, int size) {
        return new NiceList<T>().fill(elt, size);
    }

    public static <T> NiceList<T> generate(Function<Integer, T> generator) {
        NiceList<T> list = new NiceList<>();
        int i = 0;
        while (true) {
            T t = generator.apply(i++);
            if (t == null) {
                break;
            }
            list.add(t);
        }
        return list;
    }

    public static NiceList<?> maybeOf(Object value) {
        if (value instanceof Collection) {
            return new NiceList<>((Collection) value);
        } else if (value instanceof Iterable) {
            return new NiceList<>((Iterable) value);
        } else if (value instanceof Object[]) {
            return new NiceList<>((Object[]) value);
        } else {
            return null;
        }
    }

    public NiceList<T> filter(Predicate<? super T> predicate) {
        return TheMissingUtils.filter(this, predicate);
    }

    public <R> NiceList<R> map(Function<? super T, ? extends R> mapper) {
        return TheMissingUtils.map(this, mapper);
    }

    public String join(String str) {
        return TheMissingUtils.join(this, str);
    }

    public NiceList<T> concat(Collection<T> other) {
        return TheMissingUtils.concat(this, other);
    }

    public void each(Each<T> each) {
        for (T t : this) {
            each.doIt(t);
        }
    }

    public void eachWithIndex(EachWithIndex<T> each) {
        int i = 0;
        for (T t : this) {
            each.doIt(t, i++);
        }
    }

    public T find(Predicate<? super T> predicate) {
        for (T t : this) {
            if (predicate.test(t)) {
                return t;
            }
        }
        return null;
    }

    public T first() {
        return TheMissingUtils.first(this);
    }

    public T first(Predicate<T> filter) {
        return TheMissingUtils.first(this, filter);
    }

    public T last() {
        if (size() > 0) {
            return get(size() - 1);
        } else {
            return null;
        }
    }

    public NiceList<T> sortBy(Function<? super T, Comparable> toComp) {
        return TheMissingUtils.sortBy(this, toComp);
    }

    public <K> NiceMap<K, T> toMap(Function<? super T, K> toKey) {
        NiceMap<K, T> map = new NiceMap<>();
        for (T t : this) {
            map.put(toKey.apply(t), t);
        }
        return map;
    }

    public NiceList<T> distinct() {
        return distinct(t -> t);
    }

    public NiceList<T> distinct(Function<T, Object> by) {
        HashSet<Object> objects = new HashSet<>();
        NiceList<T> ts = new NiceList<>();
        for (T t : this) {
            Object key = by.apply(t);
            if (!objects.contains(key)) {
                ts.add(t);
                objects.add(key);
            }
        }
        return ts;
    }

    public NiceList<T> removeNulls() {
        return this.filter(Objects::nonNull);
    }

    public boolean anyMatch(Predicate<T> test) {
        for (T obj : this) {
            if (test.test(obj)) {
                return true;
            }
        }
        return false;
    }
    public boolean allMatch(Predicate<T> test) {
        for (T obj : this) {
            if (!test.test(obj)) {
                return false;
            }
        }
        return true;
    }

    public NiceList<T> sort() {
        NiceList<T>  copy = copy();
        Collections.sort((List) copy);
        return copy;
    }

    public NiceList<T> copy() {
        NiceList<T> copy = new NiceList<>();
        copy.addAll(this);
        return copy;
    }

    public <TT> NiceList<TT> ofType(Class<TT> fkClass) {
        NiceList<TT> elementsOfType = new NiceList<>();
        for (T t : this) {
            if (fkClass.isInstance(t)) {
                elementsOfType.add((TT) t);
            }
        }
        return elementsOfType;
    }

    public NiceList<T> fill(T s, int size) {
        for (int i = 0; i < size; i++) {
            add(s);
        }
        return this;
    }

    public NiceList<T> slice(int i) {
        return slice(i, size());
    }

    public NiceList<T> slice(int start, int end) {
        if (end < 0) {
            end = this.size() + end + 1; // size - 1 + 1 --> size + 0
        }

        NiceList<T> slice = new NiceList<>();
        for (int i = start; i < end; i++) {
            slice.add(get(i));
        }
        return slice;
    }

    public interface Each<T> {
        void doIt(T elt);
    }

    public interface EachWithIndex<T> {
        void doIt(T elt, int i);
    }

    //=========================================================
    // delegation
    //=========================================================

    public int size() {
        return delegate.size();
    }

    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    public Iterator<T> iterator() {
        return delegate.iterator();
    }

    public Object[] toArray() {
        return delegate.toArray();
    }

    public <T1> T1[] toArray(T1[] a) {
        return delegate.toArray(a);
    }

    public boolean add(T t) {
        return delegate.add(t);
    }

    public boolean remove(Object o) {
        return delegate.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
    }

    public boolean addAll(Collection<? extends T> c) {
        return delegate.addAll(c);
    }

    public boolean addAll(int index, Collection<? extends T> c) {
        return delegate.addAll(index, c);
    }

    public boolean removeAll(Collection<?> c) {
        return delegate.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return delegate.retainAll(c);
    }

    public void replaceAll(UnaryOperator<T> operator) {
        delegate.replaceAll(operator);
    }

    public void sort(Comparator<? super T> c) {
        delegate.sort(c);
    }

    public void clear() {
        delegate.clear();
    }

    public T get(int index) {
        return delegate.get(index);
    }

    public T set(int index, T element) {
        return delegate.set(index, element);
    }

    public void add(int index, T element) {
        delegate.add(index, element);
    }

    public T remove(int index) {
        return delegate.remove(index);
    }

    public int indexOf(Object o) {
        return delegate.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return delegate.lastIndexOf(o);
    }

    public ListIterator<T> listIterator() {
        return delegate.listIterator();
    }

    public ListIterator<T> listIterator(int index) {
        return delegate.listIterator(index);
    }

    public List<T> subList(int fromIndex, int toIndex) {
        return delegate.subList(fromIndex, toIndex);
    }

    public Spliterator<T> spliterator() {
        return delegate.spliterator();
    }

    public <T1> T1[] toArray(IntFunction<T1[]> generator) {
        return delegate.toArray(generator);
    }

    public boolean removeIf(Predicate<? super T> filter) {
        return delegate.removeIf(filter);
    }

    public Stream<T> stream() {
        return delegate.stream();
    }

    public Stream<T> parallelStream() {
        return delegate.parallelStream();
    }

    public void forEach(Consumer<? super T> action) {
        delegate.forEach(action);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        List<?> otherList = (List<?>) o;
        return delegate.equals(otherList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(delegate);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
