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
import java.io.FileReader;
import java.net.URLDecoder;
import java.util.*;

@RestController
@RequestMapping("/paragraphVectors")
public class PVController {

    @CrossOrigin
    @ApiOperation(value = "Top K Request internal method")
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

    @CrossOrigin
    @ApiOperation(value = "Feats Request", notes = "labels ; seperated")
    @RequestMapping(value = "getFeats", method = RequestMethod.GET, produces = "application/json")
    public String getFeats(
            @ApiParam(defaultValue = "12831") @RequestParam(value="label",required=true)String label
    ) {
        SingeltonMemory sm = SingeltonMemory.getInstance();
        String[] labels = URLDecoder.decode(label).split(";");
        JSONArray ret = new JSONArray();

        for(String l : labels) {
            JSONObject entry = new JSONObject();
            entry.put("label",l);
            String featFile = sm.outDir+"/preVectors/"+label;
            try (BufferedReader br = new BufferedReader(new FileReader(featFile))) {
                String line;
                JSONArray featArr = new JSONArray();
                while ((line = br.readLine()) != null) {
                    for(String feat : line.split(" ")) {
                        featArr.add(feat);
                    }
                }
                entry.put("feats",featArr);
            } catch (Exception e) {

            }
            ret.add(entry);
        }

        return ret.toJSONString();
    }

    @CrossOrigin
    @ApiOperation(value = "Top K Request")
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

}
