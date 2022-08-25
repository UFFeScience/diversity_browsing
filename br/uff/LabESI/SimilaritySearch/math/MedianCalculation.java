package br.uff.LabESI.SimilaritySearch.math;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class MedianCalculation {

    DescriptiveStatistics stats = new DescriptiveStatistics();

    public void appendValue(double value) {
        stats.addValue(value);
    }

    public double getMedian() {
        return stats.getPercentile(50);
    }
}