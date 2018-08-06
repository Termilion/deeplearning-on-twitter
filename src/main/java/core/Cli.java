package core;

import org.apache.commons.cli.*;
import org.deeplearning4j.graph.api.Edge;
import org.deeplearning4j.graph.api.Vertex;
import org.deeplearning4j.graph.graph.Graph;
import org.deeplearning4j.graph.models.GraphVectors;
import org.deeplearning4j.graph.models.deepwalk.DeepWalk;
import org.deeplearning4j.graph.models.loader.GraphVectorSerializer;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Class serves and handles command line interface.
 */
public class Cli{

        private static Options options = new Options();

    public Cli() {
            super();
        }

        static {
            options.addRequiredOption("i",
                    "datafolder",
                    true,
                    "folder where the dataset are situated.");
            options.addRequiredOption("o",
                    "outfolder",
                    true,
                    "output folder for persist preprocessed data");
            options.addOption(null,
                    "deepwalk",
                    true,
                    "walklenght,windowsize (default 16,8) ");
            options.addOption(null,
                    "par-vec",
                    true,
                    "windowsize (default 25) ");
            options.addRequiredOption("e",
                    "edges",
                    true,
                    "edgesfile for twitter graph");
            options.addOption("h",
                    "help",
                    false,
                    "show this help");
        }

    /**
     * Parse program arguments to a command line object.
     * @param args
     * @return CommandLine
     */
    public static CommandLine getCommandLine(String[] args) {
            CommandLineParser clp = new DefaultParser();
            CommandLine cl = null;
            try {
                cl = clp.parse(options, args);
            } catch (ParseException pe) {
                printHelp();
            }
            return cl;
        }

    /**
     * Prints command line usage.
     */
    public static void printHelp() {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("DeepWalk4J", options, true);
            System.exit(0);
        }
}