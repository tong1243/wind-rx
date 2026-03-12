package com.wut.screenfusionrx.Config;

import com.wut.screencommonrx.Model.MsgSendData.Fiber;
import com.wut.screencommonrx.Model.MsgSendData.Wave;
import com.wut.screencommonrx.Model.VehicleModel;
import com.wut.screencommonrx.Util.ModelTransformUtil;
import com.wut.screendbredisrx.Service.RedisModelDataService;
import com.wut.screenfusionrx.Service.ModelFlushSubService.FiberModelPreFlushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class EntryControllerConfig {
    private final FiberModelPreFlushService fiberModelPreFlushService;
    private final RedisModelDataService redisModelDataService;
    private final Wave wave;

    @Autowired
    public EntryControllerConfig(FiberModelPreFlushService fiberModelPreFlushService , RedisModelDataService redisModelDataService, Wave wave) {
        this.fiberModelPreFlushService = fiberModelPreFlushService;
        this.redisModelDataService = redisModelDataService;
        this.wave = wave;
    }

    @PostMapping("/pre/flush/fiber")
    public void preFlushFiberModel(@RequestBody Fiber fiber) {
        VehicleModel vehicleModel = ModelTransformUtil.fiberToVehicle(fiber, fiber.getTimestamp().longValue());
        assert vehicleModel != null;
        System.out.println(fiberModelPreFlushService.processFiberModel(vehicleModel));
    }


    @GetMapping("/pre/flush/direct")
    public void preFlushDirect() {}

}
