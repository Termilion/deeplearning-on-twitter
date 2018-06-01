import org.deeplearning4j.graph.api.Edge;
import org.deeplearning4j.graph.api.Vertex;
import org.deeplearning4j.graph.data.EdgeLineProcessor;
import org.deeplearning4j.graph.data.GraphLoader;
import org.deeplearning4j.graph.data.VertexLoader;
import org.deeplearning4j.graph.data.impl.DelimitedEdgeLineProcessor;
import org.deeplearning4j.graph.data.impl.DelimitedVertexLoader;
import org.deeplearning4j.graph.graph.Graph;
import org.deeplearning4j.graph.models.deepwalk.DeepWalk;
import org.deeplearning4j.graph.vertexfactory.IntegerVertexFactory;
import org.deeplearning4j.graph.vertexfactory.VertexFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test {

    @org.junit.Test
    public void loadTest() {

        try {

            List<Vertex<String>> vertList = new ArrayList<>();

            Map<String,Integer> idMap = new HashMap<>();
            try(BufferedReader br = new BufferedReader(new FileReader("/home/marvin/workspace/Deep-Walk-4J/twitter_combined.vert"))) {
                String line;
                int idx = 0;
                while( (line = br.readLine()) != null) {
                    idMap.put(line,idx);
                    vertList.add(new Vertex<String>(idx++,line));
                }
            }

            Graph<String,String> graph = new Graph<String,String>(vertList);

            try(BufferedReader br = new BufferedReader(new FileReader("/home/marvin/workspace/Deep-Walk-4J/twitter_combined.txt"))) {
                String line;
                while( (line = br.readLine()) != null) {
                    String from = line.split(" ")[0];
                    String to = line.split(" ")[1];

                    graph.addEdge(new Edge<String>(idMap.get(from),
                            idMap.get(to),
                            from+"-->"+to,true));
                }
            }

            System.out.println(graph.getEdgesOut(idMap.get("652193")));

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
