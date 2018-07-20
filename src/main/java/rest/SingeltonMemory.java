package rest;

import core.CustomLabelAwareFileSentenceIterator;
import core.GraphGenerator;
import core.PreProcessor;
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
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * In memory storage for loaded ParagraphVectors and DeepWalk
 */
public class SingeltonMemory {

    private PreProcessor preProcessor;
    private GraphGenerator graphGenerator;
    private Graph<String, String> graph;
    private ParagraphVectors paraVec;
    private GraphVectors deepWalk;
    Map<String, Integer> labelToIdMap;
    Map<Integer, String> idToLabelMap;

    String outDir;


    private static SingeltonMemory instance;
    private SingeltonMemory () {}
    public static synchronized SingeltonMemory getInstance () {
        if (SingeltonMemory.instance == null) {
            SingeltonMemory.instance = new SingeltonMemory ();
        }
        return SingeltonMemory.instance;
    }


    /**
     * In-Memory store init
     */
    public void init(String twitterDir, String outDir, File edgesFile, int dw_walkLength, int dw_windowSize, int pv_windowSize) {

        // Store and init Instances for ReST access
        this.outDir = outDir;
        preProcessor = new PreProcessor(twitterDir, outDir, edgesFile);
        labelToIdMap = preProcessor.getLabelToIdMap();
        graphGenerator = new GraphGenerator(edgesFile, preProcessor.getVertices(), preProcessor.getLabelToIdMap());
        graph = graphGenerator.getGraph();

        int dw_vectorSize = 3;
        int pv_layerSize = 3; // Test mit 20/50/100

        // Knoten die keine ausgehenden Kanten besitzen müssen auf sich selbst zeigen um Fehlern vorzubeugen
        int i = 0;
        for (Vertex ver : graph.getVertices(0, graph.numVertices() - 1)) {
            if (graph.getVertexDegree(ver.vertexID()) == 0) {
                int id = ver.vertexID();
                String label = id + "-->" + id;
                graph.addEdge(new Edge<String>(id, id, label, true));
                i++;
            }
        }
        Logger.getGlobal().info("added " + i + "reflective edges");

        // Init PV and DW output files
        String pvFile = outDir + "paraVec-" + pv_layerSize + "-" + pv_windowSize + ".pv";
        String dwFile = outDir + "deepWalk-" + dw_walkLength + "-" + dw_windowSize + "-" + dw_vectorSize + ".dw";

        // if persist then load from files
        if (new File(pvFile).exists() && new File(dwFile).exists()) {
            try {
                paraVec = WordVectorSerializer.readParagraphVectors(pvFile);
                Logger.getGlobal().info("loaded dw from file");
                deepWalk = GraphVectorSerializer.loadTxtVectors(new File(dwFile));
                Logger.getGlobal().info("loaded pv from file");
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        // else generate and init new DW and PV
        } else {


            // walking deep
            Logger.getGlobal().info("init DeepWalk");
            DeepWalk dw = new DeepWalk.Builder().windowSize(dw_windowSize).vectorSize(dw_vectorSize).learningRate(0.001).build();
            dw.initialize(graph);
            dw.fit(graph, dw_walkLength);

            deepWalk = dw;

            Logger.getGlobal().info("init ParagraphsVectors");
            try {
                TokenizerFactory tokenizer = new DefaultTokenizerFactory();
                CustomLabelAwareFileSentenceIterator iterator =
                        new CustomLabelAwareFileSentenceIterator(new File(outDir + "preVectors/"));

                // load paragraph vectors
                paraVec = new ParagraphVectors.Builder()
                        .layerSize(pv_layerSize)
                        .minWordFrequency(1)
                        .windowSize(pv_windowSize)
                        .tokenizerFactory(tokenizer)
                        .iterate(iterator)
                        .build();

                paraVec.fit();

                //persist PV to own file for global view
                try (FileWriter fw = new FileWriter(pvFile+".default")) {
                    for(String lab : labelToIdMap.keySet()) {
                        INDArray indArray = paraVec.lookupTable().vector(lab);
                        Logger.getGlobal().info("write "+lab);
                            try {
                                fw.write(labelToIdMap.get(lab)+
                                        "\t"+indArray.getDouble(0)+
                                        "\t"+indArray.getDouble(1)+
                                        "\t"+indArray.getDouble(2)
                                        +"\n");
                                fw.flush();
                            } catch (NullPointerException npm) {
                                fw.write(labelToIdMap.get(lab)
                                        +"\t0"
                                        +"\t0"
                                        +"\t0"
                                        +"\n");
                                fw.flush();
                            }
                        }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // persist DW and PV with DeepLearning4j
                WordVectorSerializer.writeParagraphVectors(paraVec, pvFile);
                GraphVectorSerializer.writeGraphVectors(dw, dwFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        idToLabelMap = new HashMap<>();

        for(String lab : labelToIdMap.keySet()) {
            idToLabelMap.put(labelToIdMap.get(lab), lab);
        }

    }

    public Graph<String,String> getGraph() {
        return graph;
    }

    public ParagraphVectors getParaVec() {
        return paraVec;
    }

    public GraphVectors getDeepWalk() {
        return deepWalk;
    }

}
