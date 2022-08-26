package br.uff.LabESI.SimilaritySearch.examples;

import br.uff.LabESI.SimilaritySearch.algorithms.BruteForceBrid;
import br.uff.LabESI.SimilaritySearch.algorithms.DistanceBrowsing;
import br.uff.LabESI.SimilaritySearch.algorithms.DiversityBrowsing;
import br.uff.LabESI.SimilaritySearch.algorithms.knn.LeafElement;
import br.uff.LabESI.SimilaritySearch.args.DiversityMethodsArgs;
import br.uff.LabESI.SimilaritySearch.indexes.VP_Tree.VpObjectPosition;
import br.uff.LabESI.SimilaritySearch.indexes.VP_Tree.elements.Node;
import br.uff.LabESI.SimilaritySearch.interfaces.ParserInput;
import br.uff.LabESI.SimilaritySearch.commons.Commons;

import com.beust.jcommander.JCommander;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class MainLIDEffectOnDiversitySearch {

    public static void main(String[] args) throws IOException {

        DiversityMethodsArgs<double[]> divArgs = new DiversityMethodsArgs<>("vp");
        JCommander jCommander = JCommander.newBuilder().addObject(divArgs).build();
        if (processArgs(args, jCommander)) return;

        if (divArgs.getArgsParser().isBuildtree())
            Commons.buildVPTree(divArgs, divArgs.getArgsParser().getTrainFile().getAbsolutePath());

        divArgs.loadData();
        System.out.println(divArgs);
        divArgs.getArgsParser().getOutputDir().mkdirs();

        int k = divArgs.getK();

        Node tree = divArgs.getArgsParser().getTrees().get(0);

        File output = new File(divArgs.getArgsParser().getMainFolder() + "/queries_result.csv");
        PrintWriter pw = new PrintWriter(output);
        pw.println("id,num_neighbors,dist_to_farthest,elapsed_time_ms");

        int counter = 0;

        VpObjectPosition<double[]> vpObjectPosition = new VpObjectPosition<>();

        DiversityBrowsing<double[]> diversityBrowsing;
        DistanceBrowsing<double[]> distanceBrowsing;
        BruteForceBrid<double[]> bruteForceBrid;

        switch (divArgs.getArgsParser().getExample()) {
            case "1": {
                System.out.println("Running example 1. Diversity Browsing queries.");
                break;
            }
            case "2": {
                System.out.println("Running example 2. Distance Browsing queries.");
                break;
            }
            case "3": {
                System.out.println("Running example 3. Brute Force queries.");
                break;
            }
        }

        Stream<String> linesStream = Files.lines(divArgs.getArgsParser().getTestFile().toPath());
        Iterator<String> linesIterator = linesStream.iterator();
        while (linesIterator.hasNext()) {

            String line = linesIterator.next();

            System.out.println("Executing query line " + counter + ".");

            List<LeafElement<double[]>> result = null;
            long elapsed = 0;

            divArgs.getArgsParser().getDistanceFunction().resetStatistics();
            ParserInput<double[]> targetElement = divArgs.getArgsParser().getParserInput();

            switch (divArgs.getArgsParser().getExample()) {
                case "1": {
                    diversityBrowsing = new DiversityBrowsing<double[]>(vpObjectPosition, divArgs.getArgsParser().getDistanceFunction(), targetElement, tree);
                    long start = System.currentTimeMillis();
                    result = diversityBrowsing.diversityBrowsingSearch(targetElement.parser(line), k);
                    long end = System.currentTimeMillis();
                    elapsed = end - start;
                    break;
                }
                case "2": {
                    distanceBrowsing = new DistanceBrowsing<double[]>(vpObjectPosition, divArgs.getArgsParser().getDistanceFunction(), targetElement, tree);
                    long start = System.currentTimeMillis();
                    result = distanceBrowsing.distanceBrowsingSearch(targetElement.parser(line), k);
                    long end = System.currentTimeMillis();
                    elapsed = end - start;

                    break;
                }
                case "3": {
                    bruteForceBrid = new BruteForceBrid<double[]>(divArgs.getArgsParser().getDistanceFunction(), targetElement);
                    long start = System.currentTimeMillis();
                    result = bruteForceBrid.bruteForceSearch(targetElement.parser(line), k, divArgs.getArgsParser().getTrainFile());
                    long end = System.currentTimeMillis();
                    elapsed = end - start;
                    break;
                }
            }
            pw.println(counter + "," + result.size() + "," + result.get(result.size()-1).distToQueryPoint() + "," + elapsed);
            pw.flush();
            counter++;
        }
        linesStream.close();
        pw.close();
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
}
