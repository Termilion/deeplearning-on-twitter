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
import org.nd4j.linalg.ops.transforms.Transforms;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * In memory storage for ParagraphVectors and DeepWalk and global variables.
 * Is used to hold some java instances in memory for faster api access.
 * TODO better use getter functions instead of public access.
 */
public class SingeltonMemory {

    private PreProcessor preProcessor;
    private GraphGenerator graphGenerator;
    private Graph<String, String> graph;
    private ParagraphVectors paraVec;
    private GraphVectors deepWalk;

    public Map<String, Integer> labelToIdMap;
    public Map<Integer, String> idToLabelMap;

    // DeepWalk parameters
    private int dw_vectorSize = 3;
    private int pv_layerSize = 3;
    public int dw_walkLength;

    public int dw_windowSize;
    public int pv_windowSize;

    public String outDir;

    private static SingeltonMemory instance;
    private SingeltonMemory () {}
    public static synchronized SingeltonMemory getInstance () {
        if (SingeltonMemory.instance == null) {
            SingeltonMemory.instance = new SingeltonMemory ();
        }
        return SingeltonMemory.instance;
    }


    /**
     * In-Memory storage initialisation.
     * Creates the
     */
    public void init(String twitterDir, String outDir, File edgesFile, int dw_walkLengthInit, int dw_windowSizeInit, int pv_windowSizeInit) {

        dw_walkLength = dw_walkLengthInit;
        dw_windowSize = dw_windowSizeInit;
        pv_windowSize = pv_windowSizeInit;

        // Store and init Instances for ReST access
        this.outDir = outDir;
        preProcessor = new PreProcessor(twitterDir, outDir, edgesFile);
        labelToIdMap = preProcessor.getLabelToIdMap();
        graphGenerator = new GraphGenerator(edgesFile, preProcessor.getVertices(), preProcessor.getLabelToIdMap());
        graph = graphGenerator.getGraph();

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
        Logger.getGlobal().info("added " + i + " reflective edges");

        // Init PV and DW output files
        String pvFile = outDir + "paragraphVectors/paraVec-" + pv_layerSize + "-" + pv_windowSize + ".pv";
        new File(outDir + "paragraphVectors/").mkdirs();
        String pvCsvFile = outDir + "csv/paraVec-" + pv_layerSize + "-" + pv_windowSize + ".pv";
        String dwFile = outDir + "csv/deepWalk-" + dw_walkLength + "-" + dw_windowSize + "-" + dw_vectorSize + ".dw";

        // if deepWalk persist then load from files
        if ( new File(dwFile).exists() ) {
            try {
                Logger.getGlobal().info("load DeepWalk from file");
                deepWalk = GraphVectorSerializer.loadTxtVectors(new File(dwFile));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        // else generate and init new deepwalk
        } else {
            // walking deep
            Logger.getGlobal().info("init DeepWalk");
            DeepWalk dw = new DeepWalk.Builder().windowSize(dw_windowSize).vectorSize(dw_vectorSize).learningRate(0.001).build();
            dw.initialize(graph);
            dw.fit(graph, dw_walkLength);

            deepWalk = dw;
            try{
                GraphVectorSerializer.writeGraphVectors(dw, dwFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // if paragraphVectors persist then load from files
        if ( new File(pvFile).exists() && new File(pvCsvFile).exists() ) {
            try {
                Logger.getGlobal().info("load ParagraphsVectors from file");
                paraVec = WordVectorSerializer.readParagraphVectors(pvFile);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            // else generate and init new PV
        } else {
            Logger.getGlobal().info("init ParagraphsVectors");

                TokenizerFactory tokenizer = new DefaultTokenizerFactory();
                CustomLabelAwareFileSentenceIterator iterator =
                        new CustomLabelAwareFileSentenceIterator(new File(outDir + "features/"));

                // load paragraph vectors
                paraVec = new ParagraphVectors.Builder()
                        .layerSize(pv_layerSize)
                        .minWordFrequency(1)
                        .windowSize(pv_windowSize)
                        .tokenizerFactory(tokenizer)
                        .iterate(iterator)
                        .build();

                paraVec.fit();

                // persist ParagraphsVectors for global view

                HashMap<Integer,String> entryMap = new HashMap<>();

                for(String lab : labelToIdMap.keySet()) {
                    INDArray indArray = paraVec.lookupTable().vector(lab);
                    try {
                        entryMap.put(labelToIdMap.get(lab),indArray.getDouble(0)+ "\t"+indArray.getDouble(1)+ "\t"+indArray.getDouble(2) +"\n");
                    } catch (NullPointerException npm) {
                        entryMap.put(labelToIdMap.get(lab), "0"+"\t0" +"\t0" +"\n");
                    }
                }

                List<Map.Entry<Integer,String>> sortList= new ArrayList<>(entryMap.entrySet());
                sortList.sort(Map.Entry.comparingByKey());
                Logger.getGlobal().info("sorted");
                try (FileWriter fw = new FileWriter(pvCsvFile)) {
                    for(Map.Entry<Integer,String> entry : sortList) {
                        Logger.getGlobal().info(entry.getKey()+"\t"+entry.getValue());
                        fw.write(entry.getKey()+"\t"+entry.getValue() );
                    }
                    fw.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // persist PV with DeepLearning4j
                WordVectorSerializer.writeParagraphVectors(paraVec, pvFile);
        }

        idToLabelMap = new HashMap<>();

        for(String lab : labelToIdMap.keySet()) {
            idToLabelMap.put(labelToIdMap.get(lab), lab);
        }

    }

    /**
     * Getter Graph
     * @return initialized twitter graph
     */
    public Graph<String,String> getGraph() {
        return graph;
    }

    /**
     * Getter ParagraphVectors
     * @return ParagraphVectors instance
     */
    public ParagraphVectors getParaVec() {
        return paraVec;
    }

    /**
     * Getter DeepWalk
     * @return DeepWalk instance
     */
    public GraphVectors getDeepWalk() {
        return deepWalk;
    }

}
