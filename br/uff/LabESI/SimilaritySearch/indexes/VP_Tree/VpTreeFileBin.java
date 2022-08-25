package br.uff.LabESI.SimilaritySearch.indexes.VP_Tree;

import br.uff.LabESI.SimilaritySearch.indexes.VP_Tree.elements.Node;
import br.uff.LabESI.SimilaritySearch.algorithms.dist_functions.DistanceFunction;
import br.uff.LabESI.SimilaritySearch.interfaces.GetRowID;
import br.uff.LabESI.SimilaritySearch.interfaces.ParserInput;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class VpTreeFileBin<T> implements Serializable {
    private Node<T> rootNode;
    private double maxDistanceInDataSet;
    private DistanceFunction<T> distanceFunction;
    private ParserInput<T> parserInput;
    private GetRowID getRowID;
}