package br.uff.LabESI.SimilaritySearch.args;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import lombok.Getter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
public class DiversityMethodsArgs<T> implements Serializable {

    @Parameter(names = "-k", description = "The k elements for query")
    private int k;

    @ParametersDelegate
    private ArgsParser<T> argsParser = new ArgsParser<>();

    private String destinationFolder;

    public DiversityMethodsArgs(String destinationFolder) {
        this.destinationFolder = destinationFolder;
    }

    public void loadData() throws FileNotFoundException, UnknownHostException {
        if (argsParser.getOutputDir() == null) {
            argsParser.setOutputDir(
                new File(
                    argsParser.getTrainFile().getParentFile(),
                    "output/" + k + "/" + destinationFolder + "/" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH_mm")) + "_" + InetAddress.getLocalHost().getHostName()
                )
            );
        }

        argsParser.loadData();
    }

    @Override
    public String toString() {
        return "DiversityMethodsArgs{" +
                "k=" + k +
                ", argsParser=" + argsParser +
                '}';
    }


}
