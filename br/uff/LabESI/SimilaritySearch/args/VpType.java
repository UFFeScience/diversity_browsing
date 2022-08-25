package br.uff.LabESI.SimilaritySearch.args;

public enum VpType {
    VP_TREE, VP_FOREST;

    public static VpType getVpType(int type) {
        switch (type) {
            case 0:
                return VP_TREE;
            case 1:
                return VP_FOREST;
        }
        return null;
    }
}
