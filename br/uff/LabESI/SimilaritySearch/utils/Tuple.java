package br.uff.LabESI.SimilaritySearch.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class Tuple<T1, T2> implements Serializable {
    T1 v1;
    T2 v2;

    public static <T1, T2> Tuple<T1, T2> of(T1 v1, T2 v2) {
        return new Tuple<>(v1, v2);
    }
}
