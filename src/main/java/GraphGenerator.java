import org.deeplearning4j.graph.api.Edge;
import org.deeplearning4j.graph.api.Vertex;
import org.deeplearning4j.graph.graph.Graph;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Generates a graph by given edges (File), vertices (List), and labelToIdMap (Map)
 */
public class GraphGenerator {

    private Logger log = Logger.getLogger("GraphGenerator");
    private Graph<String, String> graph;

    GraphGenerator(File edges, List<Vertex<String>> vertices, Map<String,Integer> labelToIdMap) {
        log.info("loading graph");

        Graph<String, String> graph = new Graph<>(vertices);
        log.info("initialized graph with "+vertices.size()+" vertices");

        long edegesCount = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(edges))) {
            String line;
            while ((line = br.readLine()) != null) {
                String from = line.split(" ")[0];
                String to = line.split(" ")[1];

                //log.info("add edge ( "+from+" --> "+to+" )");
                graph.addEdge(new Edge<>(labelToIdMap.get(from),
                        labelToIdMap.get(to),
                        from + "-->" + to, true));
                edegesCount++;
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        log.info("initialized graph with "+edegesCount+" edges");
    }

    /**
     * Getter initialized graph
     * @return Graph
     */
    public Graph<String,String> getGraph() {
        return graph;
    }
}
