package rest;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/graph")
public class BaseController {



    @CrossOrigin
    @ApiOperation(value = "ID Reqeust")
    @RequestMapping(value = "getId", method = RequestMethod.GET)
    public String getId(
            @ApiParam(defaultValue = "12831") @RequestParam(required = true, value = "label")String label
    ) {
        SingeltonMemory sm = SingeltonMemory.getInstance();

        return String.valueOf(sm.labelToIdMap.get(label));
    }

    @CrossOrigin
    @ApiOperation(value = "Label Request")
    @RequestMapping(value = "getLabel", method = RequestMethod.GET)
    public String getLabel(
            @ApiParam(defaultValue = "5777") @RequestParam(required = true, value = "id")int id
    ) {
        SingeltonMemory sm = SingeltonMemory.getInstance();

        return sm.idToLabelMap.get(id);
    }
}
