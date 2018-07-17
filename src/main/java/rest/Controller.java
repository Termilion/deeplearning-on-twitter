package rest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mortbay.util.ajax.JSON;
import org.nd4j.linalg.api.ops.impl.transforms.Sin;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/")
public class Controller {

   // @RequestMapping("/")
   // public String index() {
     //   return "<a href=\"swagger-ui.html\" >Documentation</a>";
    //}


    @RequestMapping(value = "getVertex", method = RequestMethod.GET)
    public String index(
            @RequestParam(value="id",required=true)String id
    ) {
        SingeltonMemory sm = SingeltonMemory.getInstance();

        return String.valueOf(sm.getGraph().getVertex(1));
    }

//    @RequestMapping(value = "getNode", method = RequestMethod.GET)
//    public String index(
//            @RequestParam(value="id",required=true)String id
//    ) {
//        SingeltonMemory sm = SingeltonMemory.getInstance();
//
//        return String.valueOf(sm.getGraph().getVertex(1));
//    }

    @RequestMapping(value = "getDWVector", method = RequestMethod.GET)
    public String getDWVector(
            @RequestParam(value="label",required=true)String label
    ) {
        SingeltonMemory sm = SingeltonMemory.getInstance();

//        return String.valueOf(sm.getDeepWalk().lookupTable().getVector(sm.labelToIdMap.get(label)));
//        return String.valueOf(sm.getDeepWalk().);
        return "";
    }

    @RequestMapping(value = "getPGVector", method = RequestMethod.GET)
    public String getPGVector(
            @RequestParam(value="label",required=true)String label
    ) {
        SingeltonMemory sm = SingeltonMemory.getInstance();

        return String.valueOf(sm.getParaVec().lookupTable().vector(label));
    }


    @RequestMapping(value = "simList", method = RequestMethod.GET)
    public String simList(
            @RequestParam(value="label",required=true)String label
    ) {
        SingeltonMemory sm = SingeltonMemory.getInstance();
        int size = sm.labelToIdMap.size();
        int selection = sm.labelToIdMap.get(label);

        JSONObject ret = new JSONObject();
        ret.put("selection", label);

        JSONArray simArr = new JSONArray();
        for( String l : sm.labelToIdMap.keySet()){
            JSONObject tmp = new JSONObject();
            tmp.put("lbl", l);
            tmp.put("id", sm.labelToIdMap.get(l));
            tmp.put("sim", sm.getDeepWalk().similarity(sm.labelToIdMap.get(label),sm.labelToIdMap.get(l)));
            simArr.add(tmp);
        }
        ret.put("similarities", simArr);
        return ret.toJSONString();
    }



    @RequestMapping(value = "getFriends", method = RequestMethod.GET)
    public String getFriends(
            @RequestParam(value="label",required=true)String label
    ) {
        SingeltonMemory sm = SingeltonMemory.getInstance();

        int [] ids = sm.getGraph().getConnectedVertexIndices(sm.labelToIdMap.get(label));
        JSONObject ret = new JSONObject();
        ret.put("selection",label);
        JSONArray idArr = new JSONArray();
        for(Integer id : ids) {
            idArr.add(sm.idToLabelMap.get(id));
        }
        ret.put("friends",idArr);
        return ret.toJSONString();
    }

    @RequestMapping(value = "getFeats", method = RequestMethod.GET)
    public String getFeats(
            @RequestParam(value="label",required=true)String label
    ) {
        SingeltonMemory sm = SingeltonMemory.getInstance();

        String featFile = sm.outDir+"/preVectors/"+label;

        List<String> feats = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(featFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                for(String feat : line.split(" ")) {
                    feats.add(feat);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        JSONObject ret = new JSONObject();
        ret.put("selection",label);
        JSONArray featArr = new JSONArray();
        featArr.addAll(feats);
        ret.put("feats",featArr);
        return ret.toJSONString();
    }



}