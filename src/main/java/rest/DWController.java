package rest;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import org.deeplearning4j.graph.api.IGraph;
import org.deeplearning4j.graph.graph.Graph;
import org.deeplearning4j.graph.models.GraphVectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.util.*;


/**
 * Rest Controller for DeepWalk embedding based operations.
 */
@RestController
@RequestMapping("/deepWalk")
public class DWController {

    /**
     * Provides the top k similar nodes for a given node label.
     * @param label node label
     * @param k number how many similar nodes
     * @return json serialized object
     */
    @CrossOrigin
    @ApiOperation(value = "Top K Request", notes = "Get top K users based on a user label")
    @RequestMapping(value = "topK", method = RequestMethod.GET, produces = "application/json")
    public String topK(
            @ApiParam(defaultValue = "12831") @RequestParam(value="label",required=true)String label,
            @ApiParam(defaultValue = "5")@RequestParam(value="k",required=true)int k
    ) {
        SingeltonMemory sm = SingeltonMemory.getInstance();
        Map<String,Integer> lToId = sm.labelToIdMap;
        GraphVectors deepWalk = sm.getDeepWalk();
        IGraph ig = deepWalk.getGraph();
        int size = lToId.size();

        JSONObject ret = new JSONObject();
        ret.put("selection", label);

        JSONArray arr = new JSONArray();
        HashMap<String,Double> entryMap = new HashMap<>();

        for( String l : lToId.keySet()){
            entryMap.put(l,deepWalk.similarity(lToId.get(label),lToId.get(l)));
        }

        List<Map.Entry<String,Double>> sortList= new ArrayList<>(entryMap.entrySet());
        sortList.sort(Map.Entry.comparingByValue());

        for( int e = 2; e < k+2; e++){
            JSONObject tmp = new JSONObject();
            String tmplabel = sortList.get( size-e ).getKey();
            //tmp.put("v", Arrays.asList(indArray.getDouble(0),indArray.getDouble(1),indArray.getDouble(2)));
            tmp.put("sim", sortList.get( size-e ).getValue());
            tmp.put("label", tmplabel);
            arr.add(tmp);
        }

        ret.put("similar", arr);
        return ret.toJSONString();
    }

    /**
     * Get json with all direct follows
     * @param label ; separated list of labels
     * @return json
     */
    @CrossOrigin
    @ApiOperation(notes = "Multiple status values can be provided with semicolon separated labels ", value = "Friends Request")
    @RequestMapping(value = "getFriends", method = RequestMethod.GET, produces = "application/json")
    public String getFriends(
            @ApiParam(defaultValue = "12831;761") @RequestParam(value="label",required=true)String label
    ) {

        // init
        SingeltonMemory sm = SingeltonMemory.getInstance();
        Graph<String,String> graph = sm.getGraph();
        String[] labels = URLDecoder.decode(label).split(";");
        JSONArray ret = new JSONArray();
        for( String l : labels ) {

            JSONObject entry = new JSONObject();
            entry.put("label", l);
            int[] vertices =  graph.getConnectedVertexIndices(sm.labelToIdMap.get(l));

            JSONArray friends = new JSONArray();
            for(int v : vertices) {
               friends.add(sm.idToLabelMap.get(v));
            }
            entry.put("friends",friends);
            ret.add(entry);
        }
        return ret.toJSONString();
    }

    /**
     * Nearest nodes, internal DL4J function.
     * @param label node label
     * @param k number
     * @return json
     */
    @CrossOrigin
    @ApiOperation(value = "Top K Request Nearest", notes = "TopK with internal nearest method")
    @RequestMapping(value = "topKnearest", method = RequestMethod.GET, produces = "application/json")
    public String topKdefault(
            @ApiParam(defaultValue = "12831") @RequestParam(value="label",required=true)String label,
            @ApiParam(defaultValue = "5")@RequestParam(value="k",required=true)int k
    ) {

        return "disabled" ;
    }

    /**
     * Compute cosine similarity between two nodes.
     * @param nodeA label of nodeA
     * @param nodeB label of nodeB
     * @return json
     */
    @CrossOrigin
    @ApiOperation(value = "Cosine Similarity", notes = "Cosine Similarity for nodeA and nodeB")
    @RequestMapping(value = "compare", method = RequestMethod.GET, produces = "application/json")
    public String compare(
            @ApiParam(defaultValue = "12831") @RequestParam(value="nodeA",required=true)String nodeA,
            @ApiParam(defaultValue = "761")@RequestParam(value="nodeB",required=true)String nodeB
    ) {
        SingeltonMemory sm = SingeltonMemory.getInstance();
        GraphVectors deepWalk = sm.getDeepWalk();
        Map<String,Integer> idMap = sm.labelToIdMap;

        JSONObject ret = new JSONObject();
        ret.put("selection", nodeA);

        JSONObject entry = new JSONObject();
        entry.put("label",nodeB);

        double dumy = 0;
        try {
            entry.put("sim",deepWalk.similarity(idMap.get(nodeA),idMap.get(nodeB)));
        } catch (NullPointerException npe) {
            entry.put("sim", 0);
        }

        JSONArray arr = new JSONArray();
        arr.add(entry);

        ret.put("similar", arr);
        return ret.toJSONString();
    }

}