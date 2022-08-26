package br.uff.LabESI.SimilaritySearch.indexes.VP_Tree.elements;

import br.uff.LabESI.SimilaritySearch.math.StandardDeviation;
import br.uff.LabESI.SimilaritySearch.math.StandardDeviationResult;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
public class Node<T> implements Serializable, Comparable<Node> {

    private double coverage;
    private StandardDeviationResult distanceDistribution;

    private UUID id = UUID.randomUUID();
    private T vantagePoint;
    private double mu;
    private int size;
    private double dMin = Double.MAX_VALUE;
    private double dMax = Double.MIN_VALUE;

    @Setter
    private double distanceToRoot;
    private Node parent;
    private double parentDistanceToSq;
    private double distanceToParent;

    private boolean isRootNode = false;
    private Node leftNode;
    private Node rightNode;

    private boolean visited = false;

    @Setter(AccessLevel.NONE)
    private File file;
    private long fileSize;

    public Node(File file) {
        this.file = file;
        fileSize = file.length();
    }

    public Node() {
    }

    public void setFile(File file) {
        this.file = file;
        fileSize = file.length();
    }

    public boolean isLeafNode() {
        return leftNode == null && rightNode == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node<?> node = (Node<?>) o;
        return Objects.equals(id, node.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
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
    public int compareTo(Node o) {
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
