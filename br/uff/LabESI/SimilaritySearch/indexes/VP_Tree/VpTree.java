package br.uff.LabESI.SimilaritySearch.indexes.VP_Tree;

import br.uff.LabESI.SimilaritySearch.algorithms.dist_functions.DistanceFunction;
import br.uff.LabESI.SimilaritySearch.indexes.VP_Tree.elements.Node;
import br.uff.LabESI.SimilaritySearch.interfaces.GetRowID;
import br.uff.LabESI.SimilaritySearch.interfaces.ParserInput;
import br.uff.LabESI.SimilaritySearch.interfaces.VantagePointCalculate;
import br.uff.LabESI.SimilaritySearch.math.MedianCalculation;
import br.uff.LabESI.SimilaritySearch.math.StandardDeviation;
import br.uff.LabESI.SimilaritySearch.utils.Repository;
import br.uff.LabESI.SimilaritySearch.commons.Commons;
import lombok.*;

import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

@Getter
public class VpTree<T> {
    @Getter(AccessLevel.NONE)
    private final AtomicLong openedElements = new AtomicLong();
    @Setter
    private File outputDir;
    private ParserInput<T> getValues;
    private GetRowID getRowID;
    private Supplier<VantagePointCalculate<T>> calculateVantagePoint;
    private File inputFile;
    @Setter(AccessLevel.PACKAGE)
    private Node<T> rootNode;
    private DistanceFunction<T> distanceFunction;
    private ExecutorService threadPOOL;
    @Setter
    private int threadPoolSize = Runtime.getRuntime().availableProcessors();
    @Setter
    private int elementsPerLeafNode = 0;
    @Setter
    private int maxBytesPerLeafNode;
    @Setter
    private boolean mergeTree = false;

    private boolean teveMerge;
    @Setter
    private Double maxDistanceInDataSet;
    private AtomicInteger currentNodeSize = new AtomicInteger(0);
    private AtomicInteger totalOpenedDirectoryNodes = new AtomicInteger(0);
    private AtomicInteger leavesReached = new AtomicInteger(0);

    protected VpTree() {
    }

    public VpTree(File inputTree) throws FileNotFoundException {
        if (!inputTree.exists())
            throw new FileNotFoundException(inputTree.getAbsolutePath());
        long startTime = System.currentTimeMillis();
        VpTreeFileBin<T> vpTreeFileBin = Repository.readFile(inputTree);
        this.distanceFunction = vpTreeFileBin.getDistanceFunction();
        this.rootNode = vpTreeFileBin.getRootNode();
        this.maxDistanceInDataSet = vpTreeFileBin.getMaxDistanceInDataSet();
        this.getValues = vpTreeFileBin.getParserInput();
        this.getRowID = vpTreeFileBin.getGetRowID();
        File parentFile = inputTree.getParentFile();
        if (!parentFile.equals(rootNode.getFile().getParentFile())) {
            System.out.println("Update path references of leafNodes nodes");
            new TreeAlgorithms<T>(threadPoolSize)
                    .getAllLeafNodes(rootNode)
                    .stream()
                    .forEach(node -> node.setFile(new File(parentFile, Commons.getRelativeFilePath(node.getFile()))));
        }
        long endTime = System.currentTimeMillis();
        Commons.printElapsedTime(startTime, endTime);
    }

    @Builder
    private VpTree(boolean mergeTree, File inputFile, File outputDir, @NonNull ParserInput<T> getValues, @NonNull Supplier<VantagePointCalculate<T>> calculateVantagePoint, @NonNull DistanceFunction<T> distanceFunction, GetRowID getRowID) {
        this.getValues = getValues;
        this.calculateVantagePoint = calculateVantagePoint;
        this.inputFile = inputFile;
        this.outputDir = outputDir;
        this.distanceFunction = distanceFunction;
        this.mergeTree = mergeTree;
        this.getRowID = getRowID;
    }

    private Thread logThread = new Thread(() -> {
        File logFile = new File(outputDir.getAbsolutePath() + "/log");
        PrintWriter pw = null;

        while (true) {

            try {
                Thread.sleep(10000);
            } catch (InterruptedException ie) {
                System.out.println("Printer thread interrupted on Thread.sleep().");
                ie.printStackTrace();
            }

            logFile.delete();
            try {
                logFile.createNewFile();
                pw = new PrintWriter(logFile);
            } catch (IOException io){
                System.out.println("Unable to create new log file due to IOException.");
                io.printStackTrace();
            }

            pw.println("Last computed node size: " + this.currentNodeSize +
                       " (Max.: " + this.elementsPerLeafNode + ")");
            pw.println("Opened directory nodes: " + this.totalOpenedDirectoryNodes);
            pw.println("Leaves reached: " + this.leavesReached);
            pw.flush();
        }
    });

    public long buildTree() throws IOException {
        long startTime = System.currentTimeMillis();
        if (outputDir == null) {
            outputDir = inputFile.getParentFile();
        }

        if (new File(outputDir, inputFile.getName() + ".treeObj").exists()) {
            throw new IOException("The file:  " + new File(outputDir, inputFile.getName() + ".treeObj") +
                    " already exists.");
        }

        if (!outputDir.exists()) outputDir.mkdirs();

        System.out.println("Building tree");

        rootNode = new Node<>(inputFile);
        rootNode.setRootNode(true);

        threadPOOL = Executors.newFixedThreadPool(threadPoolSize);
        openedElements.incrementAndGet();

        VantagePointCalculate<T> vantagePoint = this.calculateVantagePoint.get();

        int size = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(rootNode.getFile())))) {
            for (String line; (line = reader.readLine()) != null; )  {
                T values = getValues.parser(line);
                vantagePoint.appendValues(values);
                size++;
            }
        }
        rootNode.setSize(size);
        rootNode.setVantagePoint(vantagePoint.calculate());

        logThread.start();
        threadPOOL.execute(() -> computeNodeAndSplit(rootNode));

        waitJobsFinish();
        logThread.stop();

        threadPOOL.shutdown();

        if (mergeTree) {
            teveMerge = new TreeAlgorithms<T>().checkAndMergeTree(rootNode);
        }

        long endTime = System.currentTimeMillis();
        if (maxDistanceInDataSet == null) {
            maxDistanceInDataSet = Commons.OMNIDist(inputFile, rootNode.getSize(), (ParserInput<double[]>) getValues, (DistanceFunction<double[]>) distanceFunction);
        }

        persistTree();
        return endTime - startTime;
    }

    private void computeNodeAndSplit(Node<T> node) {
        double mu = calculateAverageDistance(node);

        this.currentNodeSize.set(node.getSize());
        if (node.getSize() > getElementsPerLeafNode()) {

            this.totalOpenedDirectoryNodes.getAndIncrement();

            Node leftNode = new Node();
            leftNode.setFile(Commons.getParentFolder(outputDir, node.getId() + "_" + leftNode.getId() + ".datNode"));
            PrintWriter leftWriter = null;
            try {
                leftWriter = new PrintWriter(leftNode.getFile());
            } catch (FileNotFoundException fnf) {
                fnf.printStackTrace();
                System.exit(1);
            }
            VantagePointCalculate<T> leftVantagePoint = this.calculateVantagePoint.get();
            int leftSize = 0;

            Node rightNode = new Node();
            rightNode.setFile(Commons.getParentFolder(outputDir, node.getId() + "_" + rightNode.getId() + ".datNode"));
            PrintWriter rightWriter = null;
            try {
                rightWriter = new PrintWriter(rightNode.getFile());
            } catch (FileNotFoundException fnf) {
                fnf.printStackTrace();
                System.exit(1);
            }
            VantagePointCalculate<T> rightVantagePoint = this.calculateVantagePoint.get();
            int rightSize = 0;

            Double maxDistance = null;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(node.getFile())))) {
                for (String line; (line = reader.readLine()) != null; )  {
                    T values = getValues.parser(line);
                    double distance = this.distanceFunction.getDistance(node.getVantagePoint(), values);
                    if (maxDistance == null || distance > maxDistance)
                        maxDistance = distance;
                    if (distance < mu) {
                        leftWriter.println(line);
                        leftWriter.flush();
                        leftVantagePoint.appendValues(values);
                        leftSize++;
                    } else {
                        rightWriter.println(line);
                        rightWriter.flush();
                        rightVantagePoint.appendValues(values);
                        rightSize++;
                    }
                }
            } catch (IOException io) {
                io.printStackTrace();
                System.exit(1);
            }
            node.setCoverage(maxDistance);

            /* Closing Resources */
            leftWriter.flush();
            leftWriter.close();
            rightWriter.flush();
            rightWriter.close();

            if (leftSize == 0) {
                leftNode.getFile().delete();
                node.setLeftNode(null);
            } else {
                openedElements.addAndGet(1);

                if (!node.isRootNode() && node.getFile().exists()) {
                    node.getFile().delete();
                }

                leftNode.setVantagePoint(leftVantagePoint.calculate());
                leftNode.setSize(leftSize);
                leftNode.setParent(node);
                leftNode.setDistanceToParent(distanceFunction.getDistance(node.getVantagePoint(),
                        (T) leftNode.getVantagePoint()));
                node.setLeftNode(leftNode);

                computeNodeAndSplit(leftNode);
            }

            if (rightSize == 0 || rightSize == node.getSize()) {
                rightNode.getFile().delete();
                node.setRightNode(null);
            } else {
                openedElements.addAndGet(1);

                if (!node.isRootNode() && node.getFile().exists()) {
                    node.getFile().delete();
                }

                rightNode.setVantagePoint(rightVantagePoint.calculate());
                rightNode.setSize(rightSize);
                rightNode.setParent(node);
                rightNode.setDistanceToParent(distanceFunction.getDistance(node.getVantagePoint(),
                        (T) rightNode.getVantagePoint()));
                node.setRightNode(rightNode);

                computeNodeAndSplit(rightNode);
            }

        } else {

            this.leavesReached.getAndIncrement();

            Double maxDistance = null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(node.getFile())))) {
                for (String line; (line = reader.readLine()) != null; )  {
                    T values = getValues.parser(line);
                    double distance = this.distanceFunction.getDistance(node.getVantagePoint(), values);
                    if (maxDistance == null || distance > maxDistance)
                        maxDistance = distance;
                }
            } catch (IOException io) {
                io.printStackTrace();
                System.exit(1);
            }
            node.setCoverage(maxDistance);
            double dist_to_root = this.distanceFunction.getDistance(node.getVantagePoint(),
                    this.getRootNode().getVantagePoint());
            node.setDistanceToRoot(dist_to_root);
        }
        openedElements.decrementAndGet();
    }

    private double calculateAverageDistance(Node<T> node) {
        StandardDeviation standardDeviation = new StandardDeviation(1);
        MedianCalculation medianCalculation = new MedianCalculation();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(node.getFile())))) {
            for (String line; (line = reader.readLine()) != null; )  {
                T values = getValues.parser(line);
                double distance = this.distanceFunction.getDistance(node.getVantagePoint(), values);
                standardDeviation.appendValues(distance);
                medianCalculation.appendValue(distance);
            }
        } catch (IOException io) {
            io.printStackTrace();
            System.exit(1);
        }
        node.setDistanceDistribution(standardDeviation.calculate());
        node.setMu(medianCalculation.getMedian());
        return node.getMu();
    }

    private void waitJobsFinish() {
        while (openedElements.get() > 0) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void persistTree() {
        File treeObjFile = new File(outputDir, inputFile.getName() + ".treeObj");
        VpTreeFileBin<T> vpTreeFileBin = new VpTreeFileBin<>(rootNode, maxDistanceInDataSet, distanceFunction, getValues, getRowID);
        Repository.writeObject(treeObjFile, vpTreeFileBin);
    }

    @Override
    public String toString() {
        TreeAlgorithms<T> treeAlgorithms = new TreeAlgorithms<>(threadPoolSize);
        List<Node<T>> leafNodes = treeAlgorithms.getAllLeafNodes(rootNode);
        return "VpTree{" +
                "\n\tTotal de leaf nodes: " + leafNodes.size() +
                "\n\tTotal de elementos: " + leafNodes.stream().mapToLong(Node::getSize).count() +
                "\n\tTaxa de ocupacao Media: " + leafNodes.stream().mapToLong(Node::getSize).average().getAsDouble() +
                "\n\tvpTree.alturaDaArvore(): " + TreeAlgorithms.alturaDaArvore(rootNode) +
                "\n\tvpTree.alturaMinimaDaArvore(): " + TreeAlgorithms.alturaMinimaDaArvore(rootNode) +
                "\n\tmerge: " + mergeTree +
                "\n\tteveMerge: " + teveMerge +
                "}";
    }

    public int getElementsPerLeafNode() {
        if (elementsPerLeafNode == 0)
            elementsPerLeafNode = calcMaxElementsPerLeafNode(inputFile);

        return elementsPerLeafNode;
    }

    private int calcMaxElementsPerLeafNode(File input) {

        int bytes = 0;
        try {

            byte[] chunks;
            String line;
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(input.getAbsolutePath())));
            while ((line = in.readLine()) != null) {
                chunks = line.getBytes();
                if (chunks.length > bytes)
                    bytes = chunks.length;
            }

        } catch (FileNotFoundException f) {
            f.printStackTrace();
        } catch (UnsupportedEncodingException u) {
            u.printStackTrace();
        } catch (IOException i) {
            i.printStackTrace();
        }

        int max = maxBytesPerLeafNode / bytes;

        if (max > 1) {
            return max;
        } else {
            return 1;
        }
    }
}
