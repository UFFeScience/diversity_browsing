package br.uff.LabESI.SimilaritySearch.utils;

import br.uff.LabESI.SimilaritySearch.indexes.VP_Tree.elements.Node;

import java.io.*;

public class Repository {

    public static void writeObject(File file, Object obj) {
        try (ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(file))) {
            stream.writeObject(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> T readFile(File file) {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file))) {
            return (T) objectInputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
