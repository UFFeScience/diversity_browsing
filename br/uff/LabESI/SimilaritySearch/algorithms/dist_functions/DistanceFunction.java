package br.uff.LabESI.SimilaritySearch.algorithms.dist_functions;

import java.io.Serializable;

public abstract class DistanceFunction<T> implements Serializable{

    int distCount;

    protected abstract double getDistanceImp(final T p1, final T p2);

    public double getDistance(final T p1, final T p2) {

        distCount++;
        return getDistanceImp(p1, p2);
    }

    /**
     * Resets statistics.
     */
    public void resetStatistics() {
        distCount = 0;
    }

    public int getDistanceCount() {
        return distCount;
    }

}