package br.uff.LabESI.SimilaritySearch.algorithms.dist_functions;

public class EuclideanDistance extends DistanceFunction<double[]> {

    @Override
    protected double getDistanceImp(double[] p1, double[] p2) {
        if (p1.length != p2.length) {
            throw new ArrayIndexOutOfBoundsException("The list must have the same size.");
        }

        double sum = 0;
        int length = p1.length;

        for (int i = 0; i < length; i++) {
            double diff = p1[i] - p2[i];
            sum += (diff * diff);
        }
        return Math.sqrt(sum);
    }
}
