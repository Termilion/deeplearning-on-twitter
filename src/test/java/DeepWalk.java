import org.deeplearning4j.clustering.cluster.Point;
import org.deeplearning4j.graph.models.GraphVectors;
import org.deeplearning4j.graph.models.loader.GraphVectorSerializer;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class DeepWalk {

    @Test
    public void test() {
        try {
            GraphVectors dw = GraphVectorSerializer.loadTxtVectors(new File("/home/marvin/Workspace/Deep-Walk-4J/out/deepWalk-8-4-3.dw"));


            List<Point> points = Point.toPoints(Arrays.asList(dw.getVertexVector(1)));
            System.out.println(points.get(0));
            System.out.println(dw.getVertexVector(1).getDouble(0));
            System.out.println(points.get(0).getArray().getDouble(0));
            System.out.println(dw.getVertexVector(1).getDouble(1));
            System.out.println(points.get(0).getArray().getDouble(1));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Test
    public void test2() {
        try {
            ParagraphVectors paraVec = WordVectorSerializer.readParagraphVectors("/home/marvin/Workspace/Deep-Walk-4J/out/paraVec-3-25.pv");


            List<String> near = new ArrayList<>(paraVec.nearestLabels(paraVec.lookupTable().vector("12831"),5));

            for(String n : near ) {
                System.out.println(paraVec.lookupTable().vector(n));
            }
            System.out.println(paraVec.lookupTable().vector("12831"));

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}

