package br.uff.LabESI.SimilaritySearch.algorithms.knn;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
public class LeafElement<T> implements Serializable, Comparable<LeafElement> {

    private T value;
    private String realValue;
    private double distToQueryPoint;

    public LeafElement(T values, double distance) {
        this.value = values;
        this.distToQueryPoint = distance;
    }

    @Override
    public int compareTo(LeafElement leafElement) {
        return Double.compare(this.distToQueryPoint, leafElement.distToQueryPoint());
    }
    @Override
    public String toString() {
        return distToQueryPoint + "";
    }

    public double distToQueryPoint() {
        return distToQueryPoint;
    }
}
