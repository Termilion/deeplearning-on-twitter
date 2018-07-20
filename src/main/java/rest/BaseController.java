package rest;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.json.simple.JSONObject;
import org.mortbay.util.ajax.JSON;
import org.springframework.web.bind.annotation.*;

/**
 * Base graph controller
 */
@RestController
@RequestMapping("/graph")
public class BaseController {

    @CrossOrigin
    @ApiOperation(value = "ID Reqeust")
    @RequestMapping(value = "getId", method = RequestMethod.GET, produces = "application/json")
    public String getId(
            @ApiParam(defaultValue = "12831") @RequestParam(required = true, value = "label")String label
    ) {
        SingeltonMemory sm = SingeltonMemory.getInstance();

        JSONObject ret = new JSONObject();
        ret.put("label",label);
        ret.put("id",sm.labelToIdMap.get(label));
        return ret.toJSONString();
    }

    @CrossOrigin
    @ApiOperation(value = "Label Request")
    @RequestMapping(value = "getLabel", method = RequestMethod.GET, produces = "application/json")
    public String getLabel(
            @ApiParam(defaultValue = "5777") @RequestParam(required = true, value = "id")int id
    ) {
        SingeltonMemory sm = SingeltonMemory.getInstance();

        JSONObject ret = new JSONObject();
        ret.put("label",sm.idToLabelMap.get(id));
        ret.put("id",id);
        return ret.toJSONString();
    }
}
