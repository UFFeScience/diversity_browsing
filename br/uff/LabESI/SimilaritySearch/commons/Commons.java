package br.uff.LabESI.SimilaritySearch.commons;

import br.uff.LabESI.SimilaritySearch.indexes.VP_Tree.VpTree;
import br.uff.LabESI.SimilaritySearch.algorithms.dist_functions.DistanceFunction;
import br.uff.LabESI.SimilaritySearch.args.DiversityMethodsArgs;
import br.uff.LabESI.SimilaritySearch.interfaces.GetRowID;
import br.uff.LabESI.SimilaritySearch.interfaces.ParserInput;
import br.uff.LabESI.SimilaritySearch.math.MedianCalculation;
import com.beust.jcommander.JCommander;

import java.io.*;
import java.nio.file.Files;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class Commons implements Serializable {

    public static void mergeFileIntoAnother(File fromFile, File toFile) {
        try (PrintWriter writer = new PrintWriter(new FileOutputStream(toFile, true))) {
            try (Stream<String> lines = Files.lines(fromFile.toPath())) {
                lines.forEach(writer::println);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean processArgs(String[] args, JCommander jCommander) {
        try {
            jCommander.parse(args);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            jCommander.usage();
            return true;
        }
        return false;
    }

    public static <T> double calculateAverageDistance(T element, File file, DistanceFunction<T> distanceFunction, ParserInput<T> getValues) {
        MedianCalculation medianCalculation = new MedianCalculation();
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            for (String line; (line = reader.readLine()) != null; )  {
                count++;
                T values = getValues.parser(line);
                medianCalculation.appendValue(distanceFunction.getDistance(values, element));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        double median = medianCalculation.getMedian();
        System.out.println("Count: " + count + " , MedianCalculation: " + median);
        return median;
    }

    public static double[] defaultParserLine(String splitChar, String line) {
        String[] data = line.split(splitChar);
        double[] result = new double[data.length];
        int i = 0;
        for (String s : data){
            result[i] = Double.parseDouble(s);
            ++i;
        }
        return result;
    }

    public static ParserInput<double[]> buildDefaultParserLine(String splitChar) {
        return (line) -> defaultParserLine(splitChar, line);
    }

    public static GetRowID buildDefaultGetRowLine(String splitChar) {
        return (line) -> {
            int index = line.indexOf(splitChar);
            return line.substring(0, index);
        };
    }

    public static String formatElapsedTime(long millis) {
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), // The change is in this line
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
    }

    public static File getParentFolder(File outputDir, String fileName) {
        File parentFile = new File(outputDir, fileName.substring(0, 3));
        if (!parentFile.exists()) parentFile.mkdirs();
        return new File(parentFile, fileName);
    }

    public static String getRelativeFilePath(File file) {
        return file.getParentFile().getName() + "/" + file.getName();
    }

    public static void printElapsedTime(long starTime, long endTime) {
        printElapsedTime((endTime - starTime));
    }

    public static void printElapsedTime(long elapsedTime) {
        System.out.println("Time: " + Commons.formatElapsedTime(elapsedTime) + ", or " + elapsedTime + "ms");
    }

    public static void buildVPTree(DiversityMethodsArgs<double[]> divArgs, String trainFilePath) {

        Commons.cleanFolder(divArgs.getArgsParser().getOutputDir());
        ParserInput<double[]> process = Commons.buildDefaultParserLine(",");
        File inputDataset = new File(trainFilePath);

        VpTree.VpTreeBuilder<double[]> builder = VpTree.<double[]>builder()
                .distanceFunction(divArgs.getArgsParser().getDistanceFunction())
                .calculateVantagePoint(() -> divArgs.getArgsParser().getPivotAlgo())
                .mergeTree(divArgs.getArgsParser().isMerge())
                .getValues(process)
                .outputDir(divArgs.getArgsParser().getOutputDir())
                .inputFile(inputDataset);
        VpTree<double[]> vpTree = builder.build();

        //BuildTree
        vpTree.setElementsPerLeafNode(divArgs.getArgsParser().getMaxElementsPerLeaf());
        vpTree.setMaxBytesPerLeafNode(divArgs.getArgsParser().getMaxBytesPerLeaf());
        try {
            vpTree.buildTree();
        } catch (IOException io){
            System.out.println(io.toString());
            System.out.println("Unable to build tree.");
            System.exit(1);
        }
    }

    public static double OMNIDist(File inputFile, int numberOfLines, ParserInput<double[]> getValues, DistanceFunction<double[]> distanceFunction) throws IOException {
        int randomNum = ThreadLocalRandom.current().nextInt(1, numberOfLines);
        double[] randomPoint;
        try (Stream<String> lines = Files.lines(inputFile.toPath())) {
            randomPoint = getValues.parser(lines.skip(randomNum - 1).findFirst().get());
        }
        double[] f1Point = findTheFarthestPointFrom(inputFile, getValues, distanceFunction, randomPoint);
        double[] f2Point = findTheFarthestPointFrom(inputFile, getValues, distanceFunction, f1Point);
        double maxDistance = distanceFunction.getDistance(f1Point, f2Point);
        return maxDistance;
    }

    private static double[] findTheFarthestPointFrom(File inputFile, ParserInput<double[]> getValues, DistanceFunction<double[]> distanceFunction, double[] randomPoint) throws IOException {
        final AtomicReference<Double> maxDistance = new AtomicReference<>(-1d);
        final AtomicReference<double[]> currentPoint = new AtomicReference<>(null);

        try (Stream<String> lines = Files.lines(inputFile.toPath())) {
            lines.map(getValues::parser).forEach(point -> {
                double distance = distanceFunction.getDistance(randomPoint, point);
                if (distance > maxDistance.get()) {
                    maxDistance.set(distance);
                    currentPoint.set(point);
                }
            });
        }
        return currentPoint.get();
    }

    public static void cleanFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) {
            for(File f: files) {
                if(f.isDirectory()) {
                    cleanFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
}
