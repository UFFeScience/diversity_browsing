package br.uff.LabESI.SimilaritySearch.interfaces;

import br.uff.LabESI.SimilaritySearch.algorithms.dist_functions.DistanceFunction;
import br.uff.LabESI.SimilaritySearch.indexes.VP_Tree.elements.Node;

public interface RelativeObjectPosition<T> {

    void setLeftRightMinMax(Node<T> node, T queryPoint, DistanceFunction<T> distanceFunction);
}
