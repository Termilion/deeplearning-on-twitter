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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class SingeltonMemory {

    private PreProcessor preProcessor;
    private GraphGenerator gem;
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

    public void init(String twitterDir, String outDir, File edgesFile) {
        this.outDir = outDir;
        preProcessor = new PreProcessor(twitterDir, outDir, edgesFile);
        labelToIdMap = preProcessor.getLabelToIdMap();
        gem = new GraphGenerator(edgesFile, preProcessor.getVertices(), preProcessor.getLabelToIdMap());
        graph = gem.getGraph();

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

        int dw_walkLength = 8;
        int dw_windowSize = 4;
        int dw_vectorSize = 10;
        int pv_layerSize = 8; // Test mit 20/50/100
        int pv_windowSize = 4; // = Wortzahl

        Logger.getGlobal().info("added " + i + "reflective edges");

        String pvFile = outDir + "paraVec-" + pv_layerSize + "-" + pv_windowSize + ".pv";
        String dwFile = outDir + "deepWalk-" + dw_walkLength + "-" + dw_windowSize + "-" + dw_vectorSize + ".dw";
        if (new File(pvFile).exists() && new File(dwFile).exists()) {
            try {
                paraVec = WordVectorSerializer.readParagraphVectors(pvFile);
                Logger.getGlobal().info("loaded dw from file");
                deepWalk = GraphVectorSerializer.loadTxtVectors(new File(dwFile));
                Logger.getGlobal().info("loaded pv from file");
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

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

                // persist
                WordVectorSerializer.writeParagraphVectors(paraVec, pvFile);
                GraphVectorSerializer.writeGraphVectors(dw, dwFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        idToLabelMap = new HashMap<>();

        for(String label : labelToIdMap.keySet()) {
            idToLabelMap.put(labelToIdMap.get(label),label);
        }
    }

    public void init2() {

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

    public Map<String,Integer> getLabelToIdMap() {
        return labelToIdMap;
    }
}
