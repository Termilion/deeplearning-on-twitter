import org.deeplearning4j.graph.api.Edge;
import org.deeplearning4j.graph.api.Vertex;
import org.deeplearning4j.graph.data.EdgeLineProcessor;
import org.deeplearning4j.graph.data.GraphLoader;
import org.deeplearning4j.graph.data.VertexLoader;
import org.deeplearning4j.graph.data.impl.DelimitedEdgeLineProcessor;
import org.deeplearning4j.graph.data.impl.DelimitedVertexLoader;
import org.deeplearning4j.graph.graph.Graph;
import org.deeplearning4j.graph.iterator.GraphWalkIterator;
import org.deeplearning4j.graph.iterator.RandomWalkIterator;
import org.deeplearning4j.graph.models.deepwalk.DeepWalk;
import org.deeplearning4j.graph.vertexfactory.IntegerVertexFactory;
import org.deeplearning4j.graph.vertexfactory.VertexFactory;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.text.documentiterator.LabelAwareDocumentIterator;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.deeplearning4j.text.sentenceiterator.labelaware.LabelAwareFileSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.labelaware.LabelAwareSentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.io.ClassPathResource;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class Test {

    @org.junit.Test
    public void loadTest() {

        try {

            List<Vertex<String>> vertList = new ArrayList<>();

            Map<String, Integer> idMap = new HashMap<>();
            try (BufferedReader br = new BufferedReader(new FileReader("/home/marvin/workspace/Deep-Walk-4J/twitter/12831.feat"))) {
                String line;
                int idx = 0;
                while ((line = br.readLine()) != null) {
                    idMap.put(line.split(" ")[0], idx);
                    vertList.add(new Vertex<String>(idx++, line.split(" ")[0]));
                }
            }

            Graph<String, String> graph = new Graph<String, String>(vertList);

            try (BufferedReader br = new BufferedReader(new FileReader("/home/marvin/workspace/Deep-Walk-4J/twitter/12831.edges"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String from = line.split(" ")[0];
                    String to = line.split(" ")[1];

                    graph.addEdge(new Edge<String>(idMap.get(from),
                            idMap.get(to),
                            from + "-->" + to, true));
                }
            }

            System.out.println(graph.getEdgesOut(idMap.get("398874773")));

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

}

    @org.junit.Test
    public void testDifferentLabels() throws Exception {

        List<String> labelList = new ArrayList<>();
        labelList.add("a");
        labelList.add("b");
        labelList.add("c");

        /*
        # labelvecs.txt
        Ich stehe auf der Leitung
        Ich stehe auf der Leitung
        Ich gehe zum dem Baum
         */

        SentenceIterator iter = new BasicLineIterator("/home/marvin/workspace/Deep-Walk-4J/labelvecs.txt");
        TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();
        ParagraphVectors vec = new ParagraphVectors.Builder()
                .minWordFrequency(1).labels(labelList)
                .layerSize(1)
                .stopWords(new ArrayList<String>())
                .windowSize(2).iterate(iter).tokenizerFactory(tokenizerFactory).build();

        vec.fit();


        assertEquals(vec.lookupTable().vector("b"),vec.lookupTable().vector("a"));

    }

    @org.junit.Test
    public void prepros() {
        File twitterFolder = new File("twitter/");

        String[] featFiles = twitterFolder.list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(".feat");
            }
        });

        // for each egonet
        for(String feat : featFiles) {
            String featnamesFile = "twitter/"+feat.substring(0,feat.indexOf("."))+".featnames";
            File featDir = new File("twitter/"+feat.substring(0,feat.indexOf(".")));
            if (false == featDir.exists() && false == featDir.mkdirs()) {
                System.err.println("Was not able to create directories: " + featDir);
            }

            // feat list
            List<String> featnames = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(featnamesFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    featnames.add(line.split(" ")[1]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // nodeId files
            try (BufferedReader br = new BufferedReader(new FileReader("twitter/"+feat))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] lineArr= line.split(" ",2);
                    FileWriter fw = new FileWriter("twitter/"+feat.substring(0,feat.indexOf("."))+"/"+lineArr[0]);
                    List<String> outList = new ArrayList<>();
                    int index = 0;
                    for(String feature : lineArr[1].split(" ")) {
                        if( feature.equals("1")) {
                            outList.add(featnames.get(index++));
                        }
                    }
                    fw.write(String.join(" ",outList));
                    fw.flush();
                    fw.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //self file
            try (BufferedReader br = new BufferedReader(new FileReader("twitter/"+feat.substring(0,feat.indexOf("."))+".egofeat"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    FileWriter fw = new FileWriter("twitter/"+feat.substring(0,feat.indexOf("."))+"/self");
                    List<String> outList = new ArrayList<>();
                    int index = 0;
                    for(String feature : line.split(" ")) {
                        if( feature.equals("1")) {
                            outList.add(featnames.get(index++));
                        }
                    }
                    fw.write(String.join(" ",outList));
                    fw.flush();
                    fw.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}


