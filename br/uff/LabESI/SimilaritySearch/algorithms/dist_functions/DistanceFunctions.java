package br.uff.LabESI.SimilaritySearch.algorithms.dist_functions;

public class DistanceFunctions{
    public static DistanceFunction<double[]>  EUCLIDEAN_DISTANCE = new EuclideanDistance();
    public static DistanceFunction<double[]>  COSINE_DISTANCE = new CosineDistance();
}
