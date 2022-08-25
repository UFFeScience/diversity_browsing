package br.uff.LabESI.SimilaritySearch.math;

import br.uff.vptree.math.StandardDeviationResult;

public class StandardDeviation {

    private int howManySteps;
    private double sum = 0;
    private double sq_sum = 0;
    int count = 0;

    public StandardDeviation(int howManySteps) {
        this.howManySteps = howManySteps;
    }

    public void appendValues(double value) {
        count++;
        sum += value;
        sq_sum += value * value;
    }

    public StandardDeviationResult calculate() {
        double mean = sum / count;
        double variance = (sq_sum / ((count-1) - (mean * mean)));
        return new StandardDeviationResult(mean, variance, Math.sqrt(variance));
    }
}
