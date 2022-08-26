package br.uff.LabESI.SimilaritySearch.interfaces;

public interface VantagePointCalculate<T> {
    void appendValues(T values);

    String getAlgorithmName();

    T calculate();
}
