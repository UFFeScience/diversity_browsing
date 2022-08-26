package br.uff.LabESI.SimilaritySearch.algorithms;

import br.uff.LabESI.SimilaritySearch.algorithms.dist_functions.DistanceFunction;
import br.uff.LabESI.SimilaritySearch.algorithms.knn.LeafElement;
import br.uff.LabESI.SimilaritySearch.interfaces.ParserInput;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class BruteForceBrid<T> {

    private DistanceFunction<T> distanceFunction;
    private ParserInput<T> getValues;
    private Queue<LeafElement<T>> elementQueue = new PriorityQueue<>();
    private LinkedList<LeafElement<T>> result = new LinkedList<>();

    public BruteForceBrid(DistanceFunction<T> distanceFunction, ParserInput<T> getValues) {
        this.distanceFunction = distanceFunction;
        this.getValues = getValues;
    }

    public List<LeafElement<T>> bruteForceSearch(T oq, int k, File file) {

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            for (String line; (line = reader.readLine()) != null; ) {
                T values = getValues.parser(line);
                double distance = distanceFunction.getDistance(oq, values);
                elementQueue.add(new LeafElement<T>(values, distance));
            }

            while (result.size() < k && !elementQueue.isEmpty()) {

                LeafElement element = elementQueue.poll();
                if (!isTheElementInfluenced(element))
                    result.add(element);

            }

        } catch (IOException io) {
            io.printStackTrace();
        }

        return result;
    }

    private boolean isTheElementInfluenced(LeafElement<T> element) {

        // Se o elemento está mais distante do que (2 * dist_do_ultimo_nn),
        // não pode estar influenciado por nenhum outro NN, logo, não faz sentido checar
        int size = result.size();
        for (int i = size; i > 0; i--){

            LeafElement<T> div_neighbor = result.get(i-1);
            if (Double.compare(element.distToQueryPoint(), 2*div_neighbor.distToQueryPoint()) < 0) {

                double dist = distanceFunction.getDistance(element.getValue(), div_neighbor.getValue());
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
}