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
            options.addOption(null,
                    "deepwalk",
                    true,
                    "vectorsize,windowsize,walklenght (default 10,4,8)");
            options.addOption(null,
                    "par-vec",
                    true,
                    "layersize,windowsize (default 8,4)");
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
            int dw_walkLength = 8;
            int dw_windowSize = 4;
            int dw_vectorSize = 10;

            int pv_layerSize = 8;
            int pv_windowSize = 4;

            CommandLine commandLine = Cli.getCommnadLine(args);

            String twitterDir = commandLine.getOptionValue("i") + "/";
            String outDir = commandLine.getOptionValue("o") + "/";
            File edgesFile = new File(commandLine.getOptionValue("e"));
            if(commandLine.hasOption("deepwalk")) {
                String[] split = commandLine.getOptionValue("deepwalk").split(",");
                dw_vectorSize = Integer.parseInt(split[0]);
                dw_windowSize = Integer.parseInt(split[1]);
                dw_walkLength = Integer.parseInt(split[2]);
            }

            if(commandLine.hasOption("pre-vec")) {
                String[] split = commandLine.getOptionValue("par-vec").split(",");
                pv_layerSize = Integer.parseInt(split[0]);
                pv_windowSize = Integer.parseInt(split[1]);
            }

            if(!edgesFile.exists()) {
                System.err.println("Edges file not found");
                System.exit(0);
            }

            // pre-processing
            PreProcessor preProcessor = new PreProcessor(twitterDir,outDir,edgesFile);
            GraphGenerator gem = new GraphGenerator(edgesFile,preProcessor.getVertices(),preProcessor.getLabelToIdMap());
            Graph<String, String> graph = gem.getGraph();

            // Sinnvoll? Erlaubt gerichtete Graphen!
            int i = 0;
            for( Vertex ver : graph.getVertices(0,graph.numVertices()-1)) {
                if(graph.getVertexDegree(ver.vertexID()) == 0) {
                    int id = ver.vertexID();
                    String label = id + "-->" + id;
                    graph.addEdge(new Edge<String>(id, id, label, true));
                }
            }

            // walking deep
            Logger.getGlobal().info("init DeepWalk");
            DeepWalk deepWalk = new DeepWalk.Builder().windowSize(dw_windowSize).vectorSize(dw_vectorSize).learningRate(0.001).build();
            deepWalk.initialize(graph);
            deepWalk.fit(graph, dw_walkLength);

            Logger.getGlobal().info("init ParagraphsVectors");
            try {
                TokenizerFactory tokenizer = new DefaultTokenizerFactory();
                CustomLabelAwareFileSentenceIterator iterator =
                        new CustomLabelAwareFileSentenceIterator(new File(outDir + "preVectors/"));

                // load paragraph vectors
                ParagraphVectors paraVec = new ParagraphVectors.Builder()
                    .layerSize(pv_layerSize)
                    .minWordFrequency(1)
                    .windowSize(pv_windowSize)
                    .tokenizerFactory(tokenizer)
                    .iterate(iterator)
                    .build();

                paraVec.fit();

                // persist
                WordVectorSerializer.writeParagraphVectors(paraVec, outDir + "paraVec-" + pv_layerSize + "-" + pv_windowSize + ".pv");
                GraphVectorSerializer.writeGraphVectors(deepWalk, outDir + "deepWalk-" + dw_walkLength + "-" + dw_windowSize + "-" + dw_vectorSize + ".dw");
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    public ParagraphVectors loadVector(String path) throws IOException {
        return WordVectorSerializer.readParagraphVectors(path);
    }

    public GraphVectors loadDeepWalk(String path) throws IOException {
        return GraphVectorSerializer.loadTxtVectors(new File(path));
    }
}