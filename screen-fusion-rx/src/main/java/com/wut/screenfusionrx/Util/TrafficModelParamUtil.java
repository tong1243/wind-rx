package com.wut.screenfusionrx.Util;

import com.google.common.collect.Range;
import com.wut.screencommonrx.Model.TrajRecordModel;
import com.wut.screencommonrx.Model.VehicleModel;
import com.wut.screencommonrx.Util.CollectionEmptyUtil;
import com.wut.screendbmysqlrx.Model.Traj;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.wut.screencommonrx.Static.FusionModuleStatic.*;

public class TrafficModelParamUtil {
    public static double getTrajRecordSectionArgQ(Map<Long, TrajRecordModel> map) {
        if (CollectionEmptyUtil.forMap(map)) { return 0.0; }
        return map.size() * 4.0;
    }

    public static double getTrajRecordPostureArgQ(Map<Long, TrajRecordModel> map) {
        if (CollectionEmptyUtil.forMap(map)) { return 0.0; }
        return map.size() * 60.0;
    }

    public static List<Double> getTrajRecordValidSpeedList(Map<Long, TrajRecordModel> map) {
        return map.values().stream().map(record -> record.getSpeed() / record.getNum()).filter(value -> value > 0).toList();
    }

    public static double getTrajRecordArgV(Map<Long, TrajRecordModel> map) {
        if (CollectionEmptyUtil.forMap(map)) { return 0.0; }
        List<Double> validSpeedList = getTrajRecordValidSpeedList(map);
        if (CollectionEmptyUtil.forList(validSpeedList)) { return 0.0; }
        return validSpeedList.stream().reduce(0.0, Double::sum) / validSpeedList.size();
    }

    public static double getTrajRecordArgK(Map<Long, TrajRecordModel> map, double avgSpeed, double positionInterval) {
        if (CollectionEmptyUtil.forMap(map) || avgSpeed == 0.0) { return 1.0; }
        List<Double> validSpeedList = getTrajRecordValidSpeedList(map);
        if (CollectionEmptyUtil.forList(validSpeedList)) { return 1.0; }
        if (validSpeedList.size() <= (5 * positionInterval * CAR_LANE_COUNT)) { return 1.0; }
        return SECTION_STREAM_SPEED / avgSpeed;
    }

    public static void recordTrajToMap(Map<Long, TrajRecordModel> map, Traj traj) {
        assert map != null;
        Optional.ofNullable(map.get(traj.getTrajId())).ifPresentOrElse(
                (trajRecordModel) -> {
                    trajRecordModel.setNum(trajRecordModel.getNum() + 1);
                    trajRecordModel.setSpeed(trajRecordModel.getSpeed() + traj.getSpeedX());
                },
                () -> map.put(traj.getTrajId(), new TrajRecordModel(1, traj.getSpeedX(), traj.getCarType()))
        );
    }

    public static void recordVehicleModelToMap(Map<Long, TrajRecordModel> map, VehicleModel model) {
        assert map != null;
        Optional.ofNullable(map.get(model.getId().longValue())).ifPresentOrElse(
                (trajRecordModel) -> {
                    trajRecordModel.setNum(trajRecordModel.getNum() + 1);
                    trajRecordModel.setSpeed(trajRecordModel.getSpeed() + model.getSpeed());
                },
                () -> map.put(model.getId().longValue(), new TrajRecordModel(1, model.getSpeed(), DEFAULT_CAR_TYPE))
        );
    }

}
