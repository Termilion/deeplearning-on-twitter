package rest;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.nd4j.linalg.api.ops.impl.transforms.Sin;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

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

//            double dotProduct = sourceDoc.arrayTimes(targetDoc).norm1();
//            double eucledianDist = sourceDoc.normF() * targetDoc.normF();
//            return dotProduct / eucledianDist;
//        }

        return "";
    }
}
