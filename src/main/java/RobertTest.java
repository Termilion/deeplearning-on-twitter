import org.deeplearning4j.graph.api.Edge;
import org.deeplearning4j.graph.api.Vertex;
import org.deeplearning4j.graph.graph.Graph;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class RobertTest {
    public static void main(String[] args) {
        try {
            BufferedReader input = new BufferedReader(new FileReader("/home/kilt/BigData-Prak/Deep-Walk-4J/src/main/resources/12831.feat"));
            String line = "";
            HashMap<Integer, Integer> indexMapping = new HashMap<Integer, Integer>();
            ArrayList<Vertex> vertices = new ArrayList<Vertex>();
            int i = 0;
            while((line = input.readLine()) != null) {
                int label = Integer.parseInt(line.split(" ")[0]);
                Vertex v = new Vertex(i, label);
                vertices.add(v);
                indexMapping.put(label, i);
                i++;
            }
            System.out.println("Vertices loaded.");
            Graph g = new Graph(vertices);
            input = new BufferedReader(new FileReader("/home/kilt/BigData-Prak/Deep-Walk-4J/src/main/resources/12831.edges"));
            int j = 0;
            while((line = input.readLine()) != null) {
                String[] split = line.split(" ");
                int label1 = Integer.parseInt(split[0]);
                int label2 = Integer.parseInt(split[1]);
                Edge e = new Edge(indexMapping.get(label1), indexMapping.get(label2), "e" + j, true);
                j++;
                System.out.println(e);
                g.addEdge(e);
            }
            System.out.println("Edges loaded.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
