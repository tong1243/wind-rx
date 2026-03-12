package com.wut.screendbmysqlrx.Util;

import com.wut.screencommonrx.Model.CarEventModel;
import com.wut.screencommonrx.Model.TrajModel;
import com.wut.screendbmysqlrx.Model.*;

import java.util.ArrayList;
import java.util.List;

import static com.wut.screencommonrx.Static.FusionModuleStatic.*;
import static com.wut.screencommonrx.Static.ModelConvertStatic.CAR_TYPE_COMPACT;

public class DbModelTransformUtil {
    public static Traj trajModelToTraj(TrajModel trajModel) {
        return new Traj(
                trajModel.getTrajId(),
                trajModel.getTimestamp(),
                trajModel.getFrenetX(),
                trajModel.getFrenetY(),
                trajModel.getSpeedX(),
                trajModel.getSpeedY(),
                trajModel.getHeadingAngle(),
                trajModel.getLongitude(),
                trajModel.getLatitude(),
                trajModel.getMercatorX(),
                trajModel.getMercatorY(),
                trajModel.getAccx(),
                trajModel.getRoadDirect(),
                trajModel.getCarId(),
                trajModel.getLicenseColor(),
                trajModel.getLane(),
                trajModel.getType().toString(),
                trajModel.getCarType() == null ? CAR_TYPE_COMPACT : trajModel.getCarType(),
                trajModel.getRawId(),
                trajModel.getHeight()
        );
    }

    public static CarEventModel trajToCarEventModel(Traj traj, int type) {
        return new CarEventModel(
                traj.getTrajId(),
                type,
                1,
                traj.getTimestamp(),
                traj.getTimestamp(),
                traj.getCarId(),
                traj.getRoadDirect(),
                traj.getFrenetX(),
                traj.getFrenetX(),
                false,
                true,
                0.0,
                new ArrayList<>(List.of(traj.getSpeedX()))
        );
    }

    public static CarEvent eventModelToEvent(CarEventModel carEventModel) {
        return new CarEvent(
                null,
                carEventModel.getStartTimestamp(),
                carEventModel.getEndTimestamp(),
                carEventModel.getPicLicense(),
                Double.toString(carEventModel.getStartFrenetX()),
                Double.toString(carEventModel.getEndFrenetX()),
                carEventModel.getLane(),
                carEventModel.getEventType(),
                carEventModel.getTrajId(),
                EVENT_DEFAULT_STATUS,
                EVENT_DEFAULT_PROCESS,
                carEventModel.getQueueLength()
                );
    }

    public static Posture getPostureInstance(long timeStart, long timeEnd) {
        return new Posture(
                timeStart,
                timeEnd,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                null
        );
    }

    public static FiberMetric getFiberMetricInstance(long timeStart, long timeEnd) {
        return new FiberMetric(
                timeStart,
                timeEnd,
                0.0,
                0.0,
                0.0,
                0.0,
                0L
        );
    }

    public static RadarMetric getRadarMetricInstance(long timeStart, long timeEnd, RadarInfo radarInfo) {
        return new RadarMetric(
                timeStart,
                timeEnd,
                radarInfo.getRid(),
                radarInfo.getIp(),
                radarInfo.getType(),
                radarInfo.getRoadDirect(),
                0.0,
                0.0,
                0.0,
                0.0,
                0L
        );
    }
    public static BottleneckAreaState getBottleneckAreaStateInstance() {
        return new BottleneckAreaState(
                0,
                0.0,
                0.0,
                0,
                0,
                0.0,
                0.0
        );
    }
    public static Parameters getParametersInstance(long timeStamp) {
        return new Parameters(
                timeStamp,
                0,
                0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0
        );
    }

}
