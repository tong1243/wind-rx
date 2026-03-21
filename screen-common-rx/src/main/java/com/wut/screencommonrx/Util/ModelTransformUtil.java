package com.wut.screencommonrx.Util;

import com.wut.screencommonrx.Model.CarPlateModel;
import com.wut.screencommonrx.Model.EventTypeModel;
import com.wut.screencommonrx.Model.MsgSendData.Fiber;
import com.wut.screencommonrx.Model.MsgSendData.Laser;
import com.wut.screencommonrx.Model.MsgSendData.Plate;
import com.wut.screencommonrx.Model.MsgSendData.Wave;
import com.wut.screencommonrx.Model.TrajModel;
import com.wut.screencommonrx.Model.VehicleModel;
import org.apache.logging.log4j.util.Strings;

import static com.wut.screencommonrx.Static.FusionModuleStatic.*;

public class ModelTransformUtil {
    public static TrajModel vehicleToTraj(VehicleModel vehicleModel, int carType) {
        return new TrajModel(
                vehicleModel.getType(),
                carType,
                DEFAULT_TRAJ_ID,
                vehicleModel.getId(),
                vehicleModel.getIdDup(),
                vehicleModel.getFrenetX(),
                vehicleModel.getFrenetY(),
                vehicleModel.getSpeed(),
                vehicleModel.getSpeedY(),
                DEFAULT_ACCX,
                Integer.valueOf(vehicleModel.getRoadDirect()),
                vehicleModel.getCarId(),
                DEFAULT_LICENSE_COLOR,
                vehicleModel.getMercatorX(),
                vehicleModel.getMercatorY(),
                vehicleModel.getLongitude(),
                vehicleModel.getLatitude(),
                vehicleModel.getHeadingAngle(),
                vehicleModel.getFrenetX(),
                vehicleModel.getFrenetY(),
                vehicleModel.getRoad(),
                vehicleModel.getLane(),
                vehicleModel.getId(),
                vehicleModel.getTimestamp(),
                vehicleModel.getHeight()
        );
    }

    public static CarPlateModel plateToCarPlate(Plate plate, long timestamp) {
        return new CarPlateModel(
                plate.getIp(),
                plate.getPicId(),
                plate.getGantryId(),
                plate.getCameraNum(),
                plate.getLaneNum(),
                plate.getVehicleId(),
                plate.getPicLicense(),
                plate.getLicenseColor(),
                plate.getVehSpeed().doubleValue(),
                plate.getStart(),
                plate.getEnd(),
                plate.getRoadDirect(),
                plate.getState(),
                plate.getPicTime(),
                plate.getSaveTime(),
                timestamp
        );
    }

    public static VehicleModel fiberToVehicle(Fiber fiber, long timestamp) {
        try {
            return new VehicleModel(
                    fiber.getCode(),
                    DEFAULT_IP,
                    fiber.getId(),
                    FIBER_DEFAULT_IDDUP,
                    MODEL_TYPE_FIBER,
                    fiber.getLength().doubleValue(),
                    fiber.getWidth().doubleValue(),
                    fiber.getPosX().doubleValue(),
                    fiber.getPosY().doubleValue(),
                    fiber.getSpeedX(),
                    fiber.getSpeedY().doubleValue(),
                    fiber.getSpeed(),
                    fiber.getAcceleration().doubleValue(),
                    fiber.getLongitude(),
                    fiber.getLatitude(),
                    fiber.getMercatorX(),
                    fiber.getMercatorY(),
                    fiber.getFrenetX().doubleValue(),
                    fiber.getFrenetY(),
                    fiber.getHeadingAngle(),
                    fiber.getFiberX(),
                    null,
                    fiber.getLane(),
                    fiber.getFrenetAngle().doubleValue(),
                    fiber.getRoadDirect().toString(),
                    fiber.getCarId().toString(),
                    timestamp,
                    fiber.getSaveTimestamp().longValue(),
                    fiber.getHeight()
            );
        } catch (Exception e) { MessagePrintUtil.printException(e, "fiberToVehicle"); }
        return null;
    }



    public static TrajModel trajModelToFrame(TrajModel trajModel, long timestamp) {
        return new TrajModel(
                trajModel.getType(),
                trajModel.getCarType(),
                trajModel.getTrajId(),
                trajModel.getIdOri(),
                trajModel.getIdDup(),
                trajModel.getFrenetXPrediction(),
                trajModel.getFrenetYPrediction(),
                trajModel.getSpeedX(),
                trajModel.getSpeedY(),
                DEFAULT_ACCX,
                trajModel.getRoadDirect(),
                trajModel.getCarId(),
                trajModel.getLicenseColor(),
                trajModel.getMercatorX(),
                trajModel.getMercatorY(),
                trajModel.getLongitude(),
                trajModel.getLatitude(),
                trajModel.getHeadingAngle(),
                trajModel.getFrenetXPrediction(),
                trajModel.getFrenetYPrediction(),
                trajModel.getRoad(),
                trajModel.getLane(),
                trajModel.getRawId(),
                timestamp,
                trajModel.getHeight()
        );
    }

    public static EventTypeModel getEventTypeInstance(int value) {
        return switch (value) {
            case 1 -> EVENT_TYPE_AGAINST;
            case 2 -> EVENT_TYPE_PARKING;
            case 3 -> EVENT_TYPE_SLOW;
            case 4 -> EVENT_TYPE_FAST;
//            case 5 -> EVENT_TYPE_OCCUPY;
            default -> EVENT_TYPE_NORMAL;
        };
    }

}
