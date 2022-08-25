package br.uff.LabESI.SimilaritySearch.utils;

import br.uff.LabESI.SimilaritySearch.indexes.VP_Tree.elements.Node;

import java.io.File;
import java.io.Serializable;
import java.util.Iterator;

public class FileWithParentAndDistances<T> implements Serializable {

    private File file;
    private Node<T> parent;
    private double parentDistanceToSq;
    private double rootDistanceToSq;
    private double distToParent;
    private double distToRoot;

    public double getParentDistanceToSq() {
        return parentDistanceToSq;
    }

    public void setParentDistanceToSq(double parentDistanceToSq) {
        this.parentDistanceToSq = parentDistanceToSq;
    }

    public double getRootDistanceToSq() {
        return rootDistanceToSq;
    }

    public void setRootDistanceToSq(double rootDistanceToSq) {
        this.rootDistanceToSq = rootDistanceToSq;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Node<T> getParent() {
        return parent;
    }

    public void setParent(Node<T> parent) {
        this.parent = parent;
    }

    public double getDistToParent() {
        return distToParent;
    }

    public void setDistToParent(double distToParent) {
        this.distToParent = distToParent;
    }

    public double getDistToRoot() {
        return distToRoot;
    }

    public void setDistToRoot(double distToRoot) {
        this.distToRoot = distToRoot;
    }
}
