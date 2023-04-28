package SDhulb;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ReserveArrayList<T> implements List<T> {
    class Iter implements Iterator<T> {
        private int position = 0;
        private ReserveArrayList<T> ref;

        public Iter(ReserveArrayList<T> ref) {
            this.ref = ref;
        }

        @Override
        public boolean hasNext() {
            return position < ref.length;
        }

        @Override
        public T next() {
            return ref.items[position++];
        }
    }
    public int capacity;
    public int length;
    public T[] items;
    @SuppressWarnings("unchecked")
    public ReserveArrayList() {
        this.items = (T[]) new Object[1];
        this.length = 0;
        this.capacity = 1;
    }
    public void shiftRight(int start, int displacement) {
        int oc = capacity;
        int ol = length;
        length += displacement;
        while (length >= capacity) {
            capacity *= 2;
        }
        if (oc != capacity) {
            items = Arrays.copyOf(items, capacity);
        }
        for (int i = ol-1; i >= start; i --) {
            items[i+displacement] = items[i];
        }
    }
    public void shiftLeft(int start, int displacement) {
        int oc = capacity;
        int ol = length;
        length -= displacement;
        while (capacity > length*4) {
            capacity /= 2;
        }
        for (int i = start; i <= ol; i ++) {
            items[i-displacement] = items[i];
        }
        if (oc != capacity) {
            items = Arrays.copyOf(items, capacity);
        }
    }
    public void reserveCapacity(int c) {
        int oc = capacity;
        int tar = length + c;
        while (tar >= capacity) {
            capacity *= 2;
        }
        if (oc != capacity) {
            items = Arrays.copyOf(items, capacity);
        }
    }
    @Override
    public int size() {
        return length;
    }
    @Override
    public boolean isEmpty() {
        return length == 0;
    }
    @Override
    public boolean contains(Object o) {
        for (T item : items) {
            if (o.equals(item)) {
                return true;
            }
        }
        return false;
    }
    @Override
    public Iterator<T> iterator() {
        throw new UnsupportedOperationException("Unimplemented method 'iterator'");
    }
    @Override
    public Object[] toArray() {
        return items;
    }
    @Override
    public <I> I[] toArray(I[] a) {
        throw new UnsupportedOperationException("Unimplemented method 'toArray'");
    }
    @Override
    public boolean add(T e) {
        if (length >= capacity) {
            capacity *= 2;
            items = Arrays.copyOf(items, capacity);
        }
        items[length++] = e;
        return true;
    }
    @Override
    public boolean remove(Object o) {
        int ind = indexOf(o);
        if (ind < 0) {
            return false;
        }
        remove(ind);
        return true;
    }
    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException("Unimplemented method 'containsAll'");
    }
    @Override
    public boolean addAll(Collection<? extends T> c) {
        return addAll(0, c);
    }
    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        shiftRight(index, c.size());
        int j = 0;
        for (T i : c) {
            items[index+(j++)] = i;
        }
        return true;
    }
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Unimplemented method 'removeAll'");
    }
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Unimplemented method 'retainAll'");
    }
    @SuppressWarnings("unchecked")
    @Override
    public void clear() {
        length = 0;
        capacity = 1;
        items = (T[]) new Object[1];
    }
    @Override
    public T get(int index) {
        return items[index];
    }
    @Override
    public T set(int index, T element) {
        T r = items[index];
        items[index] = element;
        return r;
    }
    @Override
    public void add(int index, T element) {
        if (index >= length) {
            throw new InvalidParameterException("Cannot invoke ReserveArrayList.add with index greater than or equal to length");
        }
        shiftRight(index, 1);
        items[index] = element;
    }
    @Override
    public T remove(int index) {
        T r = items[index];
        shiftLeft(index+1, 1);
        return r;
    }
    @Override
    public int indexOf(Object o) {
        int i = 0;
        for (T tst : items) {
            if (tst.equals(o)) {
                return i;
            }
            i ++;
        }
        return -1;
    }
    @Override
    public int lastIndexOf(Object o) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'lastIndexOf'");
    }
    @Override
    public ListIterator<T> listIterator() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listIterator'");
    }
    @Override
    public ListIterator<T> listIterator(int index) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listIterator'");
    }
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'subList'");
    }
}
