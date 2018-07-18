package rest;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import org.deeplearning4j.graph.api.IGraph;
import org.deeplearning4j.graph.graph.Graph;
import org.deeplearning4j.graph.models.GraphVectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URLDecoder;
import java.util.*;


@RestController
@RequestMapping("/deepWalk")
public class DWController {

    @ApiOperation(value = "Top K Request")
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



    @ApiOperation(notes = "get friends from label list separated by ';' ", value = "Friends Request")
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

    @ApiOperation(value = "Top K Request with internal nearest method")
    @RequestMapping(value = "topKnearest", method = RequestMethod.GET, produces = "application/json")
    public String topKdefault(
            @ApiParam(defaultValue = "12831") @RequestParam(value="label",required=true)String label,
            @ApiParam(defaultValue = "5")@RequestParam(value="k",required=true)int k
    ) {

        return "disabled" ;
    }
}