package com.wut.screendbmongorx.Util;

import com.wut.screencommonrx.Model.VehicleModel;
import com.wut.screendbmongorx.Document.*;

public class MongoModelTransformUtil {
    public static Point pointsToEZReduce(PointsToEZ pointsToEZ) {
        if (pointsToEZ == null) { return null; }
        return new Point(
                pointsToEZ.getX(),
                pointsToEZ.getY(),
                pointsToEZ.getLatitude(),
                pointsToEZ.getLongitude(),
                pointsToEZ.getFrenetx(),
                pointsToEZ.getAngle(),
                pointsToEZ.getMercator()
        );
    }

    public static Point pointsToWHReduce(PointsToWH pointsToWH) {
        if (pointsToWH == null) { return null; }
        return new Point(
                pointsToWH.getX(),
                pointsToWH.getY(),
                pointsToWH.getLatitude(),
                pointsToWH.getLongitude(),
                pointsToWH.getFrenetx(),
                pointsToWH.getAngle(),
                pointsToWH.getMercator()
        );
    }

    public static Point pointsToEZzaReduce(PointsToEZza pointsToEZza) {
        if (pointsToEZza == null) { return null; }
        return new Point(
                pointsToEZza.getX(),
                pointsToEZza.getY(),
                pointsToEZza.getLatitude(),
                pointsToEZza.getLongitude(),
                pointsToEZza.getFrenetx(),
                pointsToEZza.getAngle(),
                pointsToEZza.getMercator()
        );
    }

    public static Point pointsToWHzaReduce(PointsToWHza pointsToWHza) {
        if (pointsToWHza == null) { return null; }
        return new Point(
                pointsToWHza.getX(),
                pointsToWHza.getY(),
                pointsToWHza.getLatitude(),
                pointsToWHza.getLongitude(),
                pointsToWHza.getFrenetx(),
                pointsToWHza.getAngle(),
                pointsToWHza.getMercator()
        );
    }

    public static VehicleModelDocu vehicleModelToDocu(VehicleModel vehicleModel) {
        return new VehicleModelDocu(
                vehicleModel.getIp(),
                vehicleModel.getId(),
                vehicleModel.getIdDup(),
                vehicleModel.getType(),
                vehicleModel.getLength(),
                vehicleModel.getWidth(),
                vehicleModel.getPosX(),
                vehicleModel.getPosY(),
                vehicleModel.getSpeedX(),
                vehicleModel.getSpeedY(),
                vehicleModel.getSpeedX(),
                vehicleModel.getAcceleration(),
                vehicleModel.getLongitude(),
                vehicleModel.getLatitude(),
                vehicleModel.getMercatorX(),
                vehicleModel.getMercatorY(),
                vehicleModel.getFrenetX(),
                vehicleModel.getFrenetY(),
                vehicleModel.getHeadingAngle(),
                vehicleModel.getLane(),
                vehicleModel.getFrenetAngle(),
                Integer.parseInt(vehicleModel.getRoadDirect()),
                vehicleModel.getCarId(),
                vehicleModel.getTimestamp(),
                vehicleModel.getSaveTimestamp()
        );
    }

}
