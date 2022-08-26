package br.uff.LabESI.SimilaritySearch.algorithms.dist_functions;

public class CosineDistance extends DistanceFunction<double[]> {

    /*
     * This method requires inputs p1 and p2 to be normalized.
     * @param p1 The first instance to be compared
     * @param p2 The second instance to be compared
     * @return The cosine distance between p1 and p2
     * @throw An exception if p1.length != p2.length
     * */
    @Override
    protected double getDistanceImp(double[] s1, double[] s2) {
        if (s1.length != s2.length) {
            throw new ArrayIndexOutOfBoundsException("The list must have the same size.");
        }

        double S1 = 0d, S2 = 0d, dot = 0d;
        for(int i = 0; i < s1.length; i++){
            dot += s1[i] * s2[i];
            S1 += s1[i] * s1[i];
            S2 += s2[i] * s2[i];
        }
        double res;
        if(S1 == 0 || S2 == 0) res = 0;
        else if(S1 == S2) res = 0;
        else res = dot / (Math.sqrt(S1)*Math.sqrt(S2));
        return res;
    }
}
