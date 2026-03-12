package com.wut.screenfusionrx.Util;

import com.wut.screencommonrx.Model.VehicleModel;

import java.util.List;

import static com.wut.screencommonrx.Static.FusionModuleStatic.*;

public class DeviceModelParamUtil {
    public static int getRadarDeviceNewState(long avgTimeout) {
        if (avgTimeout == 0L) { return DEVICE_STATE_OFFLINE; }
        if (avgTimeout < DEVICE_STATE_TIMEOUT_GAP) { return DEVICE_STATE_ONLINE; }
        return DEVICE_STATE_HIGH_TIMEOUT;
    }

    public static long getDeviceAvgTimeout(List<VehicleModel> modelList) {
        return (long)(modelList.stream().mapToLong(model -> {
            return model.getSaveTimestamp() - model.getTimestamp();
        }).sum() / modelList.size());
    }

    public static long getDeviceSecAvgTimeout(List<Long> timeoutRecordList) {
        return (long)(timeoutRecordList.stream().reduce(0L, Long::sum) / timeoutRecordList.size());
    }

}
