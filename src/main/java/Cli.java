import org.apache.commons.cli.*;
import org.deeplearning4j.graph.api.Vertex;
import org.deeplearning4j.graph.graph.Graph;
import org.deeplearning4j.graph.iterator.GraphWalkIterator;
import org.deeplearning4j.graph.iterator.RandomWalkIterator;
import org.deeplearning4j.graph.models.deepwalk.DeepWalk;
import org.deeplearning4j.graph.models.loader.GraphVectorSerializer;

import java.io.File;
import java.io.IOException;

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
            int walkLength = 8;
            int windowSize = 4;
            int vectorSize = 10;

            CommandLine commandLine = Cli.getCommnadLine(args);

            String twitterDir = commandLine.getOptionValue("i") + "/";
            String outDir = commandLine.getOptionValue("o") + "/";
            File edgesFile = new File(commandLine.getOptionValue("e"));

            if(!edgesFile.exists()) {
                System.err.println("Edges file not found");
                System.exit(0);
            }

            PreProcessor preProcessor = new PreProcessor(twitterDir,outDir,edgesFile);
            GraphGenerator gem = new GraphGenerator(edgesFile,preProcessor.getVerices(),preProcessor.getLabelToIdMap());
            Graph<String, String> graph = gem.getGraph();
            int i = 0;
            for( Vertex ver : graph.getVertices(0,graph.numVertices()-1)) {
                if(graph.getVertexDegree(ver.vertexID()) == 0) i++;
            }

            DeepWalk deepWalk = new DeepWalk.Builder().windowSize(windowSize).vectorSize(vectorSize).learningRate(0.001).build();
            deepWalk.initialize(graph);

            deepWalk.fit(graph, walkLength);

            try {
                GraphVectorSerializer.writeGraphVectors(deepWalk, outDir + "deepWalk-" + walkLength + "-" + windowSize + "-" + vectorSize + ".dw");
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
}