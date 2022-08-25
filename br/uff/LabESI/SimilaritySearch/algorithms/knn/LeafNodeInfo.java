package br.uff.LabESI.SimilaritySearch.algorithms.knn;

import br.uff.LabESI.SimilaritySearch.indexes.VP_Tree.elements.Node;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
public class LeafNodeInfo<T> implements Serializable, Comparable<LeafNodeInfo> {
    private Node<T> node;
    private double dMin = 0;
    private double dMax = 0;

    @Override
    public int compareTo(LeafNodeInfo o) {
        if (this.dMin < o.getdMin())
            return -1;
        else if (this.dMin == o.getdMin()) {
            if (this.dMax < o.getdMax())
                return -1;
            else if (this.dMax == o.getdMax())
                return 0;
            else
                return 1;
        } else
            return 1;
    }

    @Override
    public String toString() {
        return "{dMin=" + dMin + ", dMax=" + dMax + '}';
    }

    public double getdMin() {
        return dMin;
    }

    public void setdMin(double dMin) {
        this.dMin = dMin;
    }

    public double getdMax() {
        return dMax;
    }

    public void setdMax(double dMax) {
        this.dMax = dMax;
    }
}