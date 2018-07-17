import org.deeplearning4j.graph.models.GraphVectors;
import org.deeplearning4j.graph.models.loader.GraphVectorSerializer;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DeepWalk {

    @Test
    public void test() {
        try {
            GraphVectors dw = GraphVectorSerializer.loadTxtVectors(new File("/home/marvin/Workspace/Deep-Walk-4J/out/deepWalk-8-4-10.dw"));
            Logger.getGlobal().info("loaded DW");
            List<List<Double>> res = new ArrayList();
            for( int i = 0; i < 81306; i++) {
                List<Double> tmp = new ArrayList();
                for( int k = 0; k < 81306; k++) {
                    tmp.add(dw.similarity(i,k));
                }
                res.add(tmp);
                Logger.getGlobal().info("Added "+i);
            }
            System.out.println();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}

