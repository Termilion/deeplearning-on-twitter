package core;

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

    private Logger log = Logger.getLogger("core.GraphGenerator");
    private Graph<String, String> graph;

    /**
     * load Twitter graph
     * @param edges
     * @param vertices
     * @param labelToIdMap
     */
    public GraphGenerator(File edges, List<Vertex<String>> vertices, Map<String,Integer> labelToIdMap) {
        log.info("loading graph");
        graph = new Graph<>(vertices);
        log.info("initialized graph with "+vertices.size()+" vertices");

        long edgesCount = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(edges))) {
            String line;
            while ((line = br.readLine()) != null) {
                String from = line.split(" ")[0];
                String to = line.split(" ")[1];

                //log.info("add edge ( "+from+" --> "+to+" )");
                graph.addEdge(new Edge<>(labelToIdMap.get(from),
                        labelToIdMap.get(to),
                        /**
                         * TODO directed: true
                         * so maybe remove all degree null vertices
                         */
                        from + "-->" + to, true));
                edgesCount++;
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        log.info("initialized graph with "+edgesCount+" edges");
    }

    /**
     * Getter initialized graph
     * @return Graph
     */
    public Graph<String,String> getGraph() {
        return graph;
    }
}
