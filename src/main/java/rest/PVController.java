package rest;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.transforms.Sin;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.springframework.web.bind.annotation.*;

import javax.xml.crypto.dsig.Transform;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;

/**
 * Rest controller for ParagraphVectors
 */
@RestController
@RequestMapping("/paragraphVectors")
public class PVController {

    /**
     * Nearest nodes, internal DL4J function.
     * @param label node label
     * @param k number of similar nodes
     * @return
     */
    @CrossOrigin
    @ApiOperation(value = "Top K Request Nearest", notes = "TopK with internal nearest method")
    @RequestMapping(value = "topKnearest", method = RequestMethod.GET, produces = "application/json")
    public String topKnearest(
            @ApiParam(defaultValue = "12831") @RequestParam(value="label",required=true)String label,
            @ApiParam(defaultValue = "5")@RequestParam(value="k",required=true)int k
    ) {

        SingeltonMemory sm = SingeltonMemory.getInstance();
        ParagraphVectors pv = sm.getParaVec();

        List<String> near = new ArrayList<>(pv.nearestLabels(pv.lookupTable().vector(label),k));

        JSONObject ret = new JSONObject();
        ret.put("selection",label);

        JSONArray arr = new JSONArray();
        arr.addAll(near);

        ret.put("top",arr);

        return ret.toJSONString();
    }

    /**
     * Return for a list of nodes their associated features.
     * @param label ; separated list of labels
     * @return json object with feats
     */
    @CrossOrigin
    @ApiOperation(value = "Feats Request", notes = "Multiple status values can be provided with semicolon seperated labels ")
    @RequestMapping(value = "getFeats", method = RequestMethod.GET, produces = "application/json")
    public String getFeats(
            @ApiParam(defaultValue = "12831;761") @RequestParam(value="label",required=true)String label
    ) {
        SingeltonMemory sm = SingeltonMemory.getInstance();
        String[] labels = URLDecoder.decode(label).split(";");
        JSONArray ret = new JSONArray();

        for(String l : labels) {
            JSONObject entry = new JSONObject();
            entry.put("label",l);
            JSONArray featArr = new JSONArray();
            String featFile = sm.outDir+"/features/"+l;
            try (BufferedReader br = new BufferedReader(new FileReader(featFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    for(String feat : line.split(" ")) {
                        featArr.add(feat);
                    }
                }
            } catch (FileNotFoundException file) {

            } catch (IOException ioe) {

            }
            entry.put("feats",featArr);
            ret.add(entry);
        }

        return ret.toJSONString();
    }

    /**
     * Top K Request for ParagraphVectors based on cosine similarity.
     * @param label
     * @param k
     * @return top k nodes
     */
    @CrossOrigin
    @ApiOperation(value = "Top K Request", notes = "Get top K users based on a user label")
    @RequestMapping(value = "topK", method = RequestMethod.GET, produces = "application/json")
    public String topK(
            @ApiParam(defaultValue = "12831") @RequestParam(value="label",required=true)String label,
            @ApiParam(defaultValue = "5")@RequestParam(value="k",required=true)int k
    ) {

        SingeltonMemory sm = SingeltonMemory.getInstance();
        ParagraphVectors pv = sm.getParaVec();
        Set<String> labels = sm.labelToIdMap.keySet();


        JSONObject ret = new JSONObject();
        ret.put("selection", label);

        JSONArray arr = new JSONArray();
        HashMap<String,Double> entryMap = new HashMap<>();


        INDArray A = pv.lookupTable().vector(label);
        for( String l : labels){
            INDArray B = pv.lookupTable().vector(l);
            if( ! Objects.isNull( B ) ) {
                entryMap.put(l, Transforms.cosineSim(A, B));
            }
        }

        List<Map.Entry<String,Double>> sortList= new ArrayList<>(entryMap.entrySet());
        sortList.sort(Map.Entry.comparingByValue());

        int size = sortList.size();

        for( int e = 2; e < k+2; e++){
            JSONObject tmp = new JSONObject();
            String tmplabel = sortList.get( size-e ).getKey();
            tmp.put("sim", sortList.get( size-e ).getValue());
            tmp.put("label", tmplabel);
            arr.add(tmp);
        }

        ret.put("similar", arr);
        return ret.toJSONString();
    }

    /**
     * Cosine similarity between two nodes.
     * @param nodeA label node A
     * @param nodeB lable node B
     * @return json with cosine similarity
     */
    @CrossOrigin
    @ApiOperation(value = "Cosine Similarity", notes = "Cosine Similarity for nodeA and nodeB")
    @RequestMapping(value = "compare", method = RequestMethod.GET, produces = "application/json")
    public String compare(
            @ApiParam(defaultValue = "12831") @RequestParam(value="nodeA",required=true)String nodeA,
            @ApiParam(defaultValue = "761")@RequestParam(value="nodeB",required=true)String nodeB
    ) {

        SingeltonMemory sm = SingeltonMemory.getInstance();
        ParagraphVectors pv = sm.getParaVec();

        INDArray A = pv.lookupTable().vector(nodeA);
        INDArray B = pv.lookupTable().vector(nodeB);

        JSONObject ret = new JSONObject();
        ret.put("selection",nodeA);

        double dumy = 0;

        JSONObject entry = new JSONObject();
        try {
            entry.put("sim", Transforms.cosineSim(A, B));
        } catch (NullPointerException npe) {
            entry.put("sim", dumy );
        }

        entry.put("label", nodeB);

        JSONArray arr = new JSONArray();
        arr.add(entry);

        ret.put("similar",arr);

        return ret.toJSONString();
    }

}
