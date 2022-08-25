package br.uff.LabESI.SimilaritySearch.interfaces;

import java.io.Serializable;

@FunctionalInterface
public interface ParserInput<T> extends Serializable {
    T parser(String line);
}
