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
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;


public class DistanceBrowsing<T> {

    private RelativeObjectPosition<T> relativeObjectPosition;

    private DistanceFunction<T> distanceFunction;
    private ParserInput<T> getValues;
    private Node<T> tree;
    private T queryPoint;

    private Queue<Node<T>> nodeQueue = new PriorityQueue<>();
    private Queue<LeafElement<T>> elementQueue = new PriorityQueue<>();
    private ArrayList<LeafElement<T>> result = new ArrayList<>();

    public DistanceBrowsing(RelativeObjectPosition<T> relativeObjectPosition, DistanceFunction<T> distanceFunction, ParserInput<T> getValues, Node<T> tree) {
        this.relativeObjectPosition = relativeObjectPosition;
        this.distanceFunction = distanceFunction;
        this.getValues = getValues;
        this.tree = tree;
    }

    public List<LeafElement<T>> distanceBrowsingSearch(T sq, int k) {

        this.queryPoint = sq;

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

    public List<LeafElement<T>> distanceBrowsingSearchWithStops(T sq, int k) {

        this.queryPoint = sq;

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

            // Se entrar aqui, é garantido que o nodeQueue tem nós e que o elementQueue está vazio.
            if (elementQueue.isEmpty()) {

                // Popa a partição
                Node<T> node;
                node = nodeQueue.poll();

                if (node.isLeafNode()) {

                    // Insere os elementos do folha no elementQueue
                    insertElementsInElementQueue(node);
                    // Seta variável para verificarmos quais partições foram visitadas.
                    node.setVisited(true);

                } else {

                    // Seta min/max dos filhos
                    this.relativeObjectPosition.setLeftRightMinMax(node, queryPoint, distanceFunction);

                    // Insere os filhos no nodeQueue
                    nodeQueue.add(node.getLeftNode());
                    nodeQueue.add(node.getRightNode());

                }

            // Se entrar aqui, é garantido que o elementQueue tem elementos e que o nodeQueue tem nós,
            // assim como que o dMin do topo do nodeQueue é menor do que a distância no topo do elementQueue.
            } else if (!nodeQueue.isEmpty() &&
                    (Double.compare(nodeQueue.element().getdMin(), elementQueue.element().distToQueryPoint()) < 0)) {

                // Popa a partição
                Node<T> node;
                node = nodeQueue.poll();

                // Checa se é folha
                if (node.isLeafNode()) {

                    // Insere os elementos do folha no elementQueue
                    insertElementsInElementQueue(node);
                    // Seta variável para verificarmos quais partições foram visitadas.
                    node.setVisited(true);

                } else {

                    // Seta min/max dos filhos
                    this.relativeObjectPosition.setLeftRightMinMax(node, queryPoint, distanceFunction);

                    // Insere os filhos no nodeQueue
                    nodeQueue.add(node.getLeftNode());
                    nodeQueue.add(node.getRightNode());

                }

            // Se entrar aqui, é garantido ou que __nodeQueue está vazio, mas o elementQueue não__,
            // ou que __a distância do elemento no topo do elementQueue é menor do que o dMin do topo do nodeQueue__
            } else {
                result.add(elementQueue.poll());
            }
        }
    }

    private void insertElementsInElementQueue(Node node){
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(node.getFile())))) {
            for (String line; (line = reader.readLine()) != null; ) {
                T values = getValues.parser(line);
                double distance = distanceFunction.getDistance(queryPoint, values);
                elementQueue.add(new LeafElement<>(values, distance));
            }
        } catch (IOException io) {
            System.out.println("IOException ao abrir o nó-folha.");
        }
    }
}