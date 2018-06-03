import org.apache.commons.cli.*;
import org.deeplearning4j.graph.api.Vertex;
import org.deeplearning4j.graph.graph.Graph;
import org.deeplearning4j.graph.iterator.GraphWalkIterator;
import org.deeplearning4j.graph.iterator.RandomWalkIterator;
import org.deeplearning4j.graph.models.deepwalk.DeepWalk;
import org.deeplearning4j.graph.models.loader.GraphVectorSerializer;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.text.sentenceiterator.labelaware.LabelAwareFileSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.labelaware.LabelAwareSentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.io.ClassPathResource;

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
            int dw_walkLength = 8;
            int dw_windowSize = 4;
            int dw_vectorSize = 10;

            int pv_layerSize = 8;
            int pv_windowSize = 4;

            CommandLine commandLine = Cli.getCommnadLine(args);

            String twitterDir = commandLine.getOptionValue("i") + "/";
            String outDir = commandLine.getOptionValue("o") + "/";
            File edgesFile = new File(commandLine.getOptionValue("e"));

            if(!edgesFile.exists()) {
                System.err.println("Edges file not found");
                System.exit(0);
            }

            // pre-processing
            PreProcessor preProcessor = new PreProcessor(twitterDir,outDir,edgesFile);
            GraphGenerator gem = new GraphGenerator(edgesFile,preProcessor.getVerices(),preProcessor.getLabelToIdMap());
            Graph<String, String> graph = gem.getGraph();
            int i = 0;
            for( Vertex ver : graph.getVertices(0,graph.numVertices()-1)) {
                if(graph.getVertexDegree(ver.vertexID()) == 0) i++;
            }

            // walking deep
            DeepWalk deepWalk = new DeepWalk.Builder().windowSize(dw_windowSize).vectorSize(dw_vectorSize).learningRate(0.001).build();
            deepWalk.initialize(graph);
            deepWalk.fit(graph, dw_walkLength);

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
}