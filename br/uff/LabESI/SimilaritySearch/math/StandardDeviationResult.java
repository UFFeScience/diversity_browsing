package br.uff.LabESI.SimilaritySearch.math;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class StandardDeviationResult implements Serializable {
    double mean;
    double variance;
    double standardDeviation;
}
