package core;

import com.google.gson.JsonObject;
import org.apache.spark.graphx.impl.GraphImpl;
import org.deeplearning4j.clustering.cluster.Cluster;
import org.deeplearning4j.clustering.cluster.ClusterSet;
import org.deeplearning4j.clustering.cluster.Point;
import org.deeplearning4j.clustering.kmeans.KMeansClustering;
import org.deeplearning4j.graph.api.Vertex;
import org.deeplearning4j.graph.graph.Graph;
import org.deeplearning4j.graph.models.GraphVectors;
import org.deeplearning4j.graph.models.deepwalk.DeepWalk;
import org.deeplearning4j.graph.models.loader.GraphVectorSerializer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Clustering
{
    public void start(GraphVectors dw) {
        KMeansClustering kmeans = KMeansClustering.setup(2, 10, "cosinesimilarity");
        System.out.println("\nPreparing graph for clustering:");
        List<INDArray> wordVectorMatrix = new ArrayList<INDArray>();
        for(int i = 0; i < dw.numVertices(); i++) {
            wordVectorMatrix.add(dw.getVertexVector(i));
        }
        List<Point> points = Point.toPoints(wordVectorMatrix);
        System.out.println("\nClustering...");
        ClusterSet cs = kmeans.applyTo(points);
        List<Cluster> clsterLst = cs.getClusters();
        System.out.println("\nfinished Clustering...");
        System.out.println("\nCluster Centers:");
        JSONArray clusterList = new JSONArray();
        int i = 0;
        for(Cluster c: clsterLst) {
            JSONObject cluster = new JSONObject();
            cluster.put("id", i);
            cluster.put("label", "Cluster " + i);
            cluster.put("size", c.getPoints().size());
            cluster.put("center", c.getCenter().getArray());
            JSONArray nodes = new JSONArray();
            c.getPoints().forEach(point -> {
                JSONObject node = new JSONObject();
                node.put("id", point.getId());
                nodes.add(node);
            });
            cluster.put("nodes", nodes);
            clusterList.add(cluster);
            System.out.println(clusterList);
            i++;
        }
    }
    public static void main(String[] args) throws IOException {
        String path = "deepwalk.tsv";
        GraphVectors dw = GraphVectorSerializer.loadTxtVectors(new File(path));
        Clustering c = new Clustering();
        c.start(dw);
    }
}
