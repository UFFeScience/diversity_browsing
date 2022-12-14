package br.uff.LabESI.SimilaritySearch.algorithms;

import br.uff.LabESI.SimilaritySearch.algorithms.dist_functions.DistanceFunction;
import br.uff.LabESI.SimilaritySearch.algorithms.knn.LeafElement;
import br.uff.LabESI.SimilaritySearch.indexes.VP_Tree.elements.Node;
import br.uff.LabESI.SimilaritySearch.interfaces.ParserInput;
import br.uff.LabESI.SimilaritySearch.interfaces.RelativeObjectPosition;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class DiversityBrowsing<T> {

    private RelativeObjectPosition<T> relativeObjectPosition;
    private DistanceFunction<T> distanceFunction;
    private ParserInput<T> getValues;
    private Node<T> tree;
    private T queryPoint;

    private Queue<Node<T>> nodeQueue = new PriorityQueue<>();
    private Queue<LeafElement<T>> elementQueue = new PriorityQueue<>();
    private LinkedList<LeafElement<T>> result = new LinkedList<>();

    public DiversityBrowsing(RelativeObjectPosition<T> relativeObjectPosition, DistanceFunction<T> distanceFunction, ParserInput<T> getValues, Node<T> tree) {
        this.relativeObjectPosition = relativeObjectPosition;
        this.distanceFunction = distanceFunction;
        this.getValues = getValues;
        this.tree = tree;
    }

    public List<LeafElement<T>> diversityBrowsingSearch(T oq, int k) {

        this.queryPoint = oq;

        if (tree.getSize() > 0) {
            tree.setdMin(0.0);
            tree.setdMax(Double.POSITIVE_INFINITY);
            nodeQueue.add(tree);
            nonRecursiveWalkThrough(k);
        }

        nodeQueue.clear();
        elementQueue.clear();
        return result;
    }

    public List<LeafElement<T>> diversityBrowsingSearchWithStops(T oq, int k) {

        this.queryPoint = oq;

        if (nodeQueue.isEmpty() && elementQueue.isEmpty()) {
            if (tree.getSize() > 0) {
                tree.setdMin(0.0);
                tree.setdMax(Double.POSITIVE_INFINITY);
                nodeQueue.add(tree);
                nonRecursiveWalkThrough(k);
            }
        } else {
            nonRecursiveWalkThrough(k);
        }

        return result;
    }

    private void nonRecursiveWalkThrough(int k){

        while (result.size() < k && !(nodeQueue.isEmpty() && elementQueue.isEmpty())){

            // Se entrar aqui, ?? garantido que o nodeQueue tem n??s e que o elementQueue est?? vazio.
            if (elementQueue.isEmpty()) {

                // Popa a parti????o
                Node<T> node;
                node = nodeQueue.poll();

                if (node.isLeafNode()) {

                    // Insere os elementos do folha no elementQueue
                    insertElementsInElementQueue(node);
                    // Seta vari??vel para verificarmos quais parti????es foram visitadas.
                    node.setVisited(true);

                } else {

                    // Seta min/max dos filhos
                    this.relativeObjectPosition.setLeftRightMinMax(node, queryPoint, distanceFunction);

                    // Insere os filhos no nodeQueue
                    nodeQueue.add(node.getLeftNode());
                    nodeQueue.add(node.getRightNode());

                }

                // Se entrar aqui, ?? garantido que o elementQueue tem elementos e que o nodeQueue tem n??s,
                // assim como que o dMin do topo do nodeQueue ?? menor do que a dist??ncia no topo do elementQueue.
            } else if (!nodeQueue.isEmpty() &&
                    (Double.compare(nodeQueue.element().getdMin(), elementQueue.element().distToQueryPoint()) < 0)) {

                // Popa a parti????o
                Node<T> node;
                node = nodeQueue.poll();

                // Checa se a parti????o est?? influenciada
                boolean influenced = isThePartitionInfluenced(node);

                if (!influenced) {

                    // Checa se ?? folha
                    if (node.isLeafNode()) {

                        // Insere os elementos do folha no elementQueue
                        insertElementsInElementQueue(node);
                        // Seta vari??vel para verificarmos quais parti????es foram visitadas.
                        node.setVisited(true);

                    } else {

                        // Seta min/max dos filhos
                        this.relativeObjectPosition.setLeftRightMinMax(node, queryPoint, distanceFunction);

                        // Insere os filhos no nodeQueue
                        nodeQueue.add(node.getLeftNode());
                        nodeQueue.add(node.getRightNode());

                    }
                }

                // Se entrar aqui, ?? garantido ou que __nodeQueue est?? vazio, mas o elementQueue n??o__,
                // ou que __a dist??ncia do elemento no topo do elementQueue ?? menor do que o dMin do topo do nodeQueue__
            } else {

                // Checa se o elemento est?? influenciado
                // Se n??o estiver, adiciona ao resultado.
                LeafElement element = elementQueue.poll();
                boolean elementInfluenced = isTheElementInfluenced(element);
                if (!elementInfluenced)
                    result.add(element);
            }
        }
    }

    private boolean isThePartitionInfluenced(Node<T> node) {

        for (int i = result.size(); i > 0; i--) {

            LeafElement div_neighbor = result.get(i - 1);

            // Se o elemento no dMin (a.k.a. "menor dist??ncia de um elemento meu para oq")
            // est?? mais distante do que 2*dist_do_ultimo_nn,
            // ele n??o pode estar influenciado por nenhum outro NN.
            if (Double.compare(node.getdMin(), 2*div_neighbor.distToQueryPoint()) >= 0)
                return false;

            double dist = distanceFunction.getDistance(node.getVantagePoint(), (T) div_neighbor.getValue());

            // Se o elemento no (dist - MAX) est?? mais distante do que 2*dist_do_ultimo_nn,
            // ele n??o pode estar influenciado por nenhum outro NN.
            if (Double.compare((dist - node.getCoverage()), 2*div_neighbor.distToQueryPoint()) >= 0) {
                double smallest = Math.min(node.getdMax(), (dist + node.getCoverage() + div_neighbor.distToQueryPoint()));
                node.setdMax(smallest);
                return false;
            }

            if (Double.compare((dist + node.getCoverage()), div_neighbor.distToQueryPoint()) < 0) {
                return true;
            }
        }

        return false;
    }

    private boolean isTheElementInfluenced(LeafElement<T> element) {

        // Se o elemento est?? mais distante do que (2 * dist_do_ultimo_nn),
        // n??o pode estar influenciado por nenhum outro NN, logo, n??o faz sentido checar
        for (int i = result.size(); i > 0; i--){

            LeafElement div_neighbor = result.get(i-1);
            if (Double.compare(element.distToQueryPoint(), 2*div_neighbor.distToQueryPoint()) < 0) {

                double dist = distanceFunction.getDistance(element.getValue(), (T) div_neighbor.getValue());
                // influenciado
                if ((Double.compare(dist, div_neighbor.distToQueryPoint()) < 0) &&
                    (Double.compare(dist, element.distToQueryPoint()) < 0) &&
                    (Double.compare(div_neighbor.distToQueryPoint(), element.distToQueryPoint()) != 0))
                    return true;

            } else {
                return false;
            }
        }

        return false;
    }

    private void insertElementsInElementQueue(Node node){
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(node.getFile())))) {
            for (String line; (line = reader.readLine()) != null; )  {
                T values = getValues.parser(line);
                double distance = distanceFunction.getDistance(queryPoint, values);
                elementQueue.add(new LeafElement<>(values, line, distance));
            }
        } catch (IOException io) {
            System.out.println("IOException ao abrir o n??-folha.");
        }
    }
}