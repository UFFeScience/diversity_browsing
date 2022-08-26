package br.uff.LabESI.SimilaritySearch.args;

import br.uff.LabESI.SimilaritySearch.indexes.VP_Tree.VantagePointStrategies;
import br.uff.LabESI.SimilaritySearch.indexes.VP_Tree.VpTree;
import br.uff.LabESI.SimilaritySearch.indexes.VP_Tree.VpTreeFileBin;
import br.uff.LabESI.SimilaritySearch.indexes.VP_Tree.elements.Node;
import br.uff.LabESI.SimilaritySearch.algorithms.dist_functions.DistanceFunctions;
import br.uff.LabESI.SimilaritySearch.algorithms.dist_functions.DistanceFunction;
import br.uff.LabESI.SimilaritySearch.interfaces.GetRowID;
import br.uff.LabESI.SimilaritySearch.interfaces.ParserInput;
import br.uff.LabESI.SimilaritySearch.interfaces.VantagePointCalculate;
import br.uff.LabESI.SimilaritySearch.utils.Repository;
import com.beust.jcommander.Parameter;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Getter
public class ArgsParser<T> implements Serializable {

    @Parameter(names = {"-mainfolder"}, required = true, description = "The folder containing all information.")
    private File mainFolder;

    @Parameter(names = {"-distfunc"}, required = true, description = "The distance function to be used")
    private String distanceFunction;

    @Setter
    @Parameter(names = {"-trainfile"}, required = false, description = "The dataset to construct the Vp-Tree")
    private File trainFile;

    @Setter
    @Parameter(names = {"-testfile"}, required = false, description = "A file with query points.")
    private File testFile;

    @Setter
    @Parameter(names = {"-example"}, required = true, description = "The code example to be executed.")
    private String example;

    @Setter
    @Parameter(names = {"-buildtree"}, required = false, description = "Whether the tree should be built.")
    private boolean buildtree = false;

    @Setter
    @Parameter(names = {"-datasetname"}, required = false, description = "The name of the dataset")
    private String datasetName;

    @Setter
    @Parameter(names = {"-treeobj"}, required = false, description = "The .treeObj file that represents an already built the Vp-Tree")
    private File treeObj;

    @Parameter(names = {"-pivotalgo"}, required = false, description = "The pivot algorithm to be used")
    private String pivotAlgo;

    @Parameter(names = {"-maxbytes"}, required = false, description = "Maximum bytes per leaf node")
    private int maxBytesPerLeaf = 512000;

    @Parameter(names = {"-maxelements"}, required = false, description = "Maximum elements per leaf node")
    private int maxElementsPerLeaf = 0;

    @Parameter(names = {"-merge"}, required = false, description = "Whether to merge the tree or not")
    private boolean merge = false;

    @Parameter(names = {"-c", "-cores"}, description = "Number of cores")
    private int cores = Runtime.getRuntime().availableProcessors();

    @Parameter(names = {"-vptype"})
    private VpType vpType;

    @Setter
    @Parameter(names = {"-o", "-output"}, description = "The output directory")
    private File outputDir;

    @Parameter(names = {"-local"}, description = "To set the master the local[Available processors]")
    private boolean testEnv = false;

    @Getter
    @Parameter(names = {"-string-id"}, description = "Force to use String ID, the default is int")
    private boolean stringID = false;

    private double tau = 0;

    @Setter
    private ParserInput<T> parserInput;
    private GetRowID getRowID;

    /*VP Objs*/
    private transient List<Node<T>> trees;
    private transient VpTree<T> vpTree;

    public File getTreeObj(){
        try {
            if (this.treeObj != null){
                return treeObj;
            } else {
                File f = new File(this.getOutputDir() + "/" + this.getTrainFile().getName() + ".treeObj");
                if (f.exists()) {
                    return f;
                } else {
                    throw new NullPointerException("unable to find a suitable .treeObj.");
                }
            }
        } catch (NullPointerException np) {
            np.printStackTrace();
            return null;
        }
    }

    public File getTestFile() throws NullPointerException{
        try {
            if (this.testFile != null) {
                return testFile;
            } else {

                File f = new File(this.getMainFolder().getAbsolutePath() + "/test.csv");
                if (f.exists()) {
//                    System.out.println("The testFile was not provided. Using " + f.getAbsolutePath() + " instead.");
                    return f;
                } else {
                    throw new NullPointerException("Unable to find a suitable testFile.");
                }
            }
        } catch (NullPointerException np){
            np.printStackTrace();
            return null;
        }
    }

    public File getTrainFile() throws NullPointerException{
        try {
            if (this.trainFile != null){
                return trainFile;
            } else {
                File f = new File(this.getMainFolder().getAbsolutePath() + "/train.csv");
                if (f.exists()) {
//                    System.out.println("The trainFile was not provided. Using " + this.getMainFolder().getAbsolutePath() + " instead.");
                    return f;
                } else {
                    throw new NullPointerException("Unable to find a suitable trainFile.");
                }
            }
        } catch (NullPointerException np) {
            np.printStackTrace();
            return null;
        }
    }

    public VantagePointCalculate<T> getPivotAlgo() {

        switch (pivotAlgo) {
            case "VP_PIVOT":
                return (VantagePointCalculate<T>) VantagePointStrategies.VP_PIVOT(0.10, (DistanceFunction<double[]>) this.getDistanceFunction());
            case "RANDOM":
            default: return (VantagePointCalculate<T>) VantagePointStrategies.RANDOM();
        }
    }

    public String getPivotAlgoAsString(){
        return pivotAlgo;
    }

    public DistanceFunction<T> getDistanceFunction() {
        switch (distanceFunction) {
            case "EUCLIDEAN_DISTANCE":
                return (DistanceFunction<T>) DistanceFunctions.EUCLIDEAN_DISTANCE;
            case "COSINE_DISTANCE":
                return (DistanceFunction<T>) DistanceFunctions.COSINE_DISTANCE;

            default: return (DistanceFunction<T>) DistanceFunctions.EUCLIDEAN_DISTANCE;
        }
    }

    public String getDistanceFunctionAsString(){
        return distanceFunction;
    }

    public void setDistanceFunction(String distanceFunction) {
        this.distanceFunction = distanceFunction;
    }

    void loadData() throws FileNotFoundException {
        if (vpType == null) {
            Object treeBin = Repository.readFile(this.getTreeObj());
            if (treeBin instanceof VpTreeFileBin) {
                vpType = VpType.VP_TREE;
            } else {
                vpType = VpType.VP_FOREST;
            }
        }

        switch (vpType) {
            case VP_TREE:
                loadVpTree();
                break;
        }
    }

    private void loadVpTree() throws FileNotFoundException {
        vpTree = new VpTree<>(this.getTreeObj());
        parserInput = vpTree.getGetValues();
        getRowID = vpTree.getGetRowID();
        trees = Arrays.asList(vpTree.getRootNode());
    }

    public double getMaxDistanceInDataSet() {
        switch (vpType) {
            case VP_TREE:
                return vpTree.getMaxDistanceInDataSet();
            default:
                throw new NullPointerException();
        }
    }

    public File getRawInputFile() {
        String fileName = this.getTreeObj().getName();
        fileName = fileName.replace(".treeObj", "");
        return new File(getTestFile().getParent(), fileName);
    }

    @Override
    public String toString() {
        return "{" +
            "mainfolder=" + getMainFolder() +
            ", distfunc=" + distanceFunction +
            ", outputDir=" + outputDir +
            ", cores=" + cores +
            '}';
//        return "hidden";
    }
}
