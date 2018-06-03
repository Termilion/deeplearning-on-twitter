import org.apache.commons.cli.*;
import org.deeplearning4j.graph.data.GraphLoader;
import org.deeplearning4j.graph.graph.Graph;
import org.deeplearning4j.graph.models.deepwalk.DeepWalk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class Cli{

        private static Options options = new Options();

    public Cli() {
            super();
        }

        static {
            options.addRequiredOption("i",
                    "datafolder",
                    true,
                    "folder where the dataset are situated default data/");
            options.addRequiredOption("o",
                    "outfolder",
                    true,
                    "output folder");
            options.addRequiredOption("e",
                    "edges",
                    true,
                    "edgesfile");
            options.addOption("h",
                    "help",
                    false,
                    "show this help");
        }

        private static CommandLine getCommnadLine(String[] args) {
            CommandLineParser clp = new DefaultParser();
            CommandLine cl = null;
            try {
                cl = clp.parse(options, args);
            } catch (ParseException pe) {
                printHelp();
            }
            return cl;
        }

        private static void printHelp() {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("DeepWalk4J", options, true);
            System.exit(0);
        }

        public static void main(String[] args){

            CommandLine commandLine = Cli.getCommnadLine(args);

            String twitterDir = commandLine.getOptionValue("i") + "/";
            String outDir = commandLine.getOptionValue("o") + "/";
            File edgesFile = new File(commandLine.getOptionValue("e"));

            if(!edgesFile.exists()) {
                System.err.println("Edges file not found");
                System.exit(0);
            }

            PreProcessor preProcessor = new PreProcessor(twitterDir,outDir,edgesFile);
            new GraphGenerator(edgesFile,preProcessor.getVerices(),preProcessor.getLabelToIdMap());
        }
}