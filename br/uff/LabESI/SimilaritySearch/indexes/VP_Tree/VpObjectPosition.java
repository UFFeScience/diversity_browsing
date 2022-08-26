package br.uff.LabESI.SimilaritySearch.indexes.VP_Tree;

import br.uff.LabESI.SimilaritySearch.algorithms.dist_functions.DistanceFunction;
import br.uff.LabESI.SimilaritySearch.indexes.VP_Tree.elements.Node;
import br.uff.LabESI.SimilaritySearch.interfaces.RelativeObjectPosition;

public class VpObjectPosition<T> implements RelativeObjectPosition<T> {
    
    enum PositionRelativeToPartition {
        INSIDE,
        RING,
        OUTSIDE
    }

    private PositionRelativeToPartition checkoqPosition(double dist, double mu, double max) {

        if (Double.compare(dist, mu) < 0)
            return PositionRelativeToPartition.INSIDE;
        else if (Double.compare(dist, max) <= 0)
            return PositionRelativeToPartition.RING;
        else
            return PositionRelativeToPartition.OUTSIDE;
    }

    @Override
    public void setLeftRightMinMax(Node<T> node, T queryPoint, DistanceFunction<T> distanceFunction) {
        double dist = distanceFunction.getDistance(queryPoint, node.getVantagePoint());
        PositionRelativeToPartition oqPosition = checkoqPosition(dist, node.getMu(), node.getCoverage());

        // oq dentro bola
        if (oqPosition == PositionRelativeToPartition.INSIDE) {

            // Min do Left
            node.getLeftNode().setdMin(0.0);

            // Max do Left
            if (node.getdMax() > node.getMu() + dist - Double.MIN_VALUE)
                node.getLeftNode().setdMax(node.getMu() + dist - Double.MIN_VALUE);
            else
                node.getLeftNode().setdMax(node.getdMax());

            // Min do Right
            node.getRightNode().setdMin(node.getMu() - dist + Double.MIN_VALUE);

            // Max do Right
            if (node.getdMax() > node.getCoverage() + dist)
                node.getRightNode().setdMax(node.getCoverage() + dist);
            else
                node.getRightNode().setdMax(node.getdMax());
        }
        // oq no anel
        else if (oqPosition == PositionRelativeToPartition.RING) {

            // Min do Left
            node.getLeftNode().setdMin(dist - node.getMu() + Double.MIN_VALUE);

            // Max do Left
            if (node.getdMax() > dist + node.getMu() - Double.MIN_VALUE)
                node.getLeftNode().setdMax(dist + node.getMu() - Double.MIN_VALUE);
            else
                node.getLeftNode().setdMax(node.getdMax());

            // Min do Right
            node.getRightNode().setdMin(0.0);

            // Max do Right
            if (node.getdMax() > dist + node.getCoverage())
                node.getRightNode().setdMax(dist + node.getCoverage());
            else
                node.getRightNode().setdMax(node.getdMax());
        }
        // oq fora da partição
        else {

            // Min do Left
            node.getLeftNode().setdMin(dist - node.getMu() + Double.MIN_VALUE);

            // Max do Left
            if (node.getdMax() > dist + node.getMu() - Double.MIN_VALUE)
                node.getLeftNode().setdMax(dist + node.getMu() - Double.MIN_VALUE);
            else
                node.getLeftNode().setdMax(node.getdMax());

            // Min do Right
            node.getRightNode().setdMin(dist - node.getCoverage());

            // Max do Right
            if (node.getdMax() > dist + node.getCoverage())
                node.getRightNode().setdMax(dist + node.getCoverage());
            else
                node.getRightNode().setdMax(node.getdMax());
        }
    }
}