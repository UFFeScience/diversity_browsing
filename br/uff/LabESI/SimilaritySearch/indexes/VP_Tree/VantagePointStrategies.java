package br.uff.LabESI.SimilaritySearch.indexes.VP_Tree;

import br.uff.LabESI.SimilaritySearch.algorithms.dist_functions.DistanceFunction;
import br.uff.LabESI.SimilaritySearch.interfaces.VantagePointCalculate;
import br.uff.LabESI.SimilaritySearch.math.MedianCalculation;
import br.uff.LabESI.SimilaritySearch.math.StandardDeviation;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class VantagePointStrategies {

    public static VantagePointCalculate<double[]> CENTROID() {
        return new VantagePointCalculate<double[]>() {

            int count;

            double[] vantagePoint;

            @Override
            public void appendValues(double[] values) {
                if (this.vantagePoint == null) {
                    this.vantagePoint = new double[values.length];
                }
                count++;
                for (int i = 0; i < values.length; i++) {
                    vantagePoint[i] += values[i];
                }
            }

            @Override
            public String getAlgorithmName() {
                return "CENTROID";
            }

            @Override
            public double[] calculate() {
                for (int i = 0; i < vantagePoint.length; i++) {
                    vantagePoint[i] = vantagePoint[i] / count;
                }
                return vantagePoint;
            }
        };
    }

    public static VantagePointCalculate<double[]> RANDOM() {
        return new VantagePointCalculate<double[]>() {
            LinkedList<double[]> allKnownElements = new LinkedList<>();

            @Override
            public void appendValues(double[] values) {
                allKnownElements.add(values);
            }

            @Override
            public String getAlgorithmName() {
                return "RANDOM";
            }

            @Override
            public double[] calculate() {
                int randomNum = ThreadLocalRandom.current().nextInt(0, allKnownElements.size());
                double[] result = allKnownElements.get(randomNum);
                allKnownElements = null;
                return result;
            }
        };
    }

    /**
     * @param sampleRate       0 .. 1
     * @param distanceFunction
     * @return
     */
    public static VantagePointCalculate<double[]> VP_PIVOT(double sampleRate, DistanceFunction<double[]> distanceFunction) {
        return new VantagePointCalculate<double[]>() {
            List<double[]> allKnownElements = new ArrayList<>();

            @Override
            public void appendValues(double[] values) {
                allKnownElements.add(values);
            }

            @Override
            public String getAlgorithmName() {
                return "VP_PIVOT";
            }

            @Override
            public double[] calculate() {
                int allElementsSize = allKnownElements.size();
                int sampleSize;
                if (allKnownElements.size() > 150) {
                    sampleSize = Math.max(100, (int) (allElementsSize * sampleRate));
                } else {
                    sampleSize = allElementsSize / 2;
                }

                if (sampleSize == 0 || sampleSize * 2 > allElementsSize) {
                    return allKnownElements.get(ThreadLocalRandom.current().nextInt(0, (int) allElementsSize));
                }

                if (sampleSize == 1) sampleSize = 2;
                //Result
                Double best_spread = null;
                double[] best_p = null;

                //P set
                Set<Integer> pIndices = randomSampleIndex(sampleSize);

                for (int pIndex : pIndices) {
                    double[] p = allKnownElements.get(pIndex);
                    //D set : elementos de allKnownElements que nao estao em P
                    List<double[]> D = new LinkedList<>();
                    MedianCalculation medianCalculation = new MedianCalculation();

                    for (int i = 0; i < allElementsSize; i++) {
                        if (!pIndices.contains(i)) {
                            double[] d = allKnownElements.get(i);
                            D.add(d);
                            double distance = distanceFunction.getDistance(p, d);
                            medianCalculation.appendValue(distance);
                        }
                    }
                    double mu = medianCalculation.getMedian();

                    StandardDeviation standardDeviation = new StandardDeviation(1);
                    for (double[] d : D) {
                        double distance = distanceFunction.getDistance(p, d);
                        standardDeviation.appendValues(distance - mu);
                    }
                    double spread = standardDeviation.calculate().getVariance();

                    if (best_spread == null || spread > best_spread) {
                        best_spread = spread;
                        best_p = p;
                    }
                }
                return best_p;
            }

            Set<Integer> randomSampleIndex(int sampleSize) {
                Set<Integer> alreadyUsedIndex = new HashSet<>();
                for (int i = 0; i < sampleSize; i++) {
                    int randomNum = ThreadLocalRandom.current().nextInt(0, allKnownElements.size());
                    while (alreadyUsedIndex.contains(randomNum))
                        randomNum = ThreadLocalRandom.current().nextInt(0, allKnownElements.size());
                    alreadyUsedIndex.add(randomNum);
                }
                return alreadyUsedIndex;
            }
        };
    }


}
