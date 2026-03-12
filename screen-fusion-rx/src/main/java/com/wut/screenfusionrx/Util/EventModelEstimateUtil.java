package com.wut.screenfusionrx.Util;

import com.wut.screendbmysqlrx.Model.Traj;

import static com.wut.screencommonrx.Static.FusionModuleStatic.*;

public class EventModelEstimateUtil {

    public static boolean hasAgainstEvent(Traj traj, double lastFrenetX) {
        if (lastFrenetX == 0) { return false; }
        switch (traj.getRoadDirect()) {
            // 鄂州至武汉方向1(<-收费站方向),FrenetX正常情况下增大
            case ROAD_DIRECT_TO_WH -> { return traj.getFrenetX() < lastFrenetX; }
            // 武汉至鄂州方向2(->收费站方向),FrenetX正常情况下减小
            case ROAD_DIRECT_TO_EZ -> { return traj.getFrenetX() > lastFrenetX; }
        }
        return false;
    }

    public static boolean hasParkingEvent(Traj traj, double firstSpeed, double lastSpeed,double avgSpeed) {
        // 速度=0,平均速度>20
        return traj.getSpeedX() == EVENT_PARKING_SPEED
                && avgSpeed > EVENT_PARKING_AVGSPEED;
    }

//    public static boolean hasOccupyEvent(Traj traj) {
//        if (traj.getLane() != 9) { return false; }
//        double pos = traj.getFrenetX();
//        return switch (traj.getRoadDirect()) {
//            case ROAD_DIRECT_TO_WH -> EVENT_GENERAL_RANGE.contains(pos)
//                    && !EVENT_OCCUPY_WH_BLOCK_RANGE1.contains(pos)
//                    && !EVENT_OCCUPY_WH_BLOCK_RANGE2.contains(pos)
//                    && !EVENT_OCCUPY_WH_BLOCK_RANGE3.contains(pos);
//            case ROAD_DIRECT_TO_EZ -> EVENT_GENERAL_RANGE.contains(pos)
//                    && !EVENT_OCCUPY_EZ_BLOCK_RANGE1.contains(pos)
//                    && !EVENT_OCCUPY_EZ_BLOCK_RANGE2.contains(pos)
//                    && !EVENT_OCCUPY_EZ_BLOCK_RANGE3.contains(pos);
//            default -> false;
//        };
//    }

    public static boolean hasFastEvent(Traj traj, double lastSpeed) {
        // 当前速度 >65KM/H
        return EVENT_FAST_SPEED_RANGE.contains(traj.getSpeedX());
    }

    public static boolean hasSlowEvent(Traj traj, double avgSpeed) {
        // 当前速度 <20KM/H,平均速度20-40
        return EVENT_SLOW_SPPED_RANGE.contains(traj.getSpeedX())
                && avgSpeed >  EVENT_SLOW_AVGSPEED;
    }

}
