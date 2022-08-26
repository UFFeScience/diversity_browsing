package br.uff.LabESI.SimilaritySearch.interfaces;

import java.io.Serializable;

@FunctionalInterface
public interface GetRowID extends Serializable {
    String getRowID(String line);
}
