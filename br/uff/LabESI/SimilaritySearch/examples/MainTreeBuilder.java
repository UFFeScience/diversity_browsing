package br.uff.LabESI.SimilaritySearch.examples;

import br.uff.LabESI.SimilaritySearch.commons.Commons;
import br.uff.LabESI.SimilaritySearch.args.DiversityMethodsArgs;
import com.beust.jcommander.JCommander;

public class MainTreeBuilder {

    public static void main(String[] args) {

        /*Args Passer*/
        DiversityMethodsArgs<double[]> divArgs = new DiversityMethodsArgs<>("vp");
        JCommander jCommander = JCommander.newBuilder().addObject(divArgs).build();
        if (Commons.processArgs(args, jCommander)) return;

        Commons.buildVPTree(divArgs, divArgs.getArgsParser().getTrainFile().getAbsolutePath());
    }
}