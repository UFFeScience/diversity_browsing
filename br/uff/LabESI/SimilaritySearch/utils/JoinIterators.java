package br.uff.LabESI.SimilaritySearch.utils;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;


public class JoinIterators<T> implements Iterator<T> {

    private Iterator<T> current = null;
    private List<Iterator<T>> iterators;

    public JoinIterators(List<Iterator<T>> iterators) {
        this.iterators = iterators;
    }

    T nextValue = null;

    @Override
    public boolean hasNext() {
        if (nextValue != null) return true;
        while (true) {
            if (current == null) {
                if (iterators.isEmpty()) {
                    return false;
                } else {
                    current = iterators.remove(0);
                }
            }
            if (current.hasNext()) {
                nextValue = current.next();
                return true;
            }
            current = null;
        }
    }

    @Override
    public T next() {
        T aux = nextValue;
        nextValue = null;
        return aux;
    }
}
