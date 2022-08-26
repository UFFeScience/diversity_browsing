package br.uff.LabESI.SimilaritySearch.indexes.VP_Tree.elements;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
public class QueryNode<T> implements Serializable, Comparable<QueryNode> {

    private double coverage;

    private T vantagePoint;
    private double mu;
    private int size;
    private double dMin = Double.MAX_VALUE;
    private double dMax = Double.MIN_VALUE;

    private QueryNode leftNode;
    private QueryNode rightNode;

    private boolean visited = false;

    @Setter(AccessLevel.NONE)
    private File file;
    private long fileSize;

    public QueryNode(File file) {
        this.file = file;
        fileSize = file.length();
    }

    public QueryNode() {
    }

    public void setFile(File file) {
        this.file = file;
        fileSize = file.length();
    }

    public boolean isLeafNode() {
        return leftNode == null && rightNode == null;
    }

    @Override
    public String toString() {
        return "Node{" +
                "coverage=" + coverage +
                ", size=" + size +
                ", file=" + file +
                '}';
    }

    @Override
    public int compareTo(QueryNode o) {
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
