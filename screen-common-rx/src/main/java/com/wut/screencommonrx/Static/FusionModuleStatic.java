package com.wut.screencommonrx.Static;

import com.google.common.collect.Range;
import com.wut.screencommonrx.Model.EventTypeModel;

public class FusionModuleStatic {
    public static final String DEFAULT_IP = "127.0.0.1";
    public static final String DEFAULT_CAR_ID = "0";
    public static final String CORS_MAPPING = "/**";
    public static final String CORS_HEADERS = "*";
    public static final String CORS_METHODS = "*";
    public static final String CORS_ORIGIN_PATTERNS = "*";
    public static final int CORS_MAX_AGE = 3600;

    public static final int ASYNC_SERVICE_TIMEOUT = 30000;
    public static final double FRENETX_TOLERANCE = 10;
    public static final double FRENETY_TOLERANCE = 3.75;
    public static final double FRENETX_PREDICT_FACTOR = 0.2;
    public static final int FUSION_TIME_INTER = 1000;
    public static final int CAR_TYPE_COUNT = 3;
    public static final int CAR_LANE_COUNT = 3;
    public static final long DEFAULT_TRAJ_ID = 0L;
    public static final int DEFAULT_CAR_TYPE = 1;
    public static final double DEFAULT_ACCX = 0.0;
    public static final int DEFAULT_LICENSE_COLOR = 0;

    public static final int ROAD_DIRECT_TO_WH = 1;

    public static final int ROAD_DIRECT_TO_EZ = 2;
    public static final long TRAJ_TIMESTAMP_OFFSET = 600;
    public static final int EVENT_DEFAULT_STATUS = 0;
    public static final int EVENT_DEFAULT_PROCESS = 0;
    public static final long POSTURE_RECORD_TIME_COND = 60000;
    public static final long SECTION_RECORD_TIME_COND = 900000;
    public static final long BATCH_RECORD_TIME_COND = 5000;
    public static final long EVENT_BATCH_RECORD_TIME_COND = 1000;
    public static final long DEVICE_RECORD_TIME_COND = 60000;
    public static final long RISK_RECORD_TIME_COND = 60000;
    public static final long DEVICE_SEC_RECORD_TIME_COND = 900000;
    public static final double SECTION_STREAM_SPEED = 110.0;
    public static final int DEVICE_TYPE_WAVE = 1;
    public static final int DEVICE_TYPE_LASER = 2;
    public static final int DEVICE_STATE_DISABLE = -1;
    public static final int DEVICE_STATE_OFFLINE = 0;
    public static final int DEVICE_STATE_ONLINE = 1;
    public static final int DEVICE_LASER_SECTION_SID = 2;
    public static final int DEVICE_STATE_HIGH_TIMEOUT = 2;
    public static final long DEVICE_STATE_TIMEOUT_GAP = 700;
    public static final double SECTION_ROAD_START = 3400;
    public static final double SECTION_ROAD_END = 12750;
    public static final int TASK_POOL_SIZE = 200;
    public static final int TASK_AWAIT = 60;
    public static final int FIBER_ID_SUFFIX = 10000000;
    public static final int LASER_ID_SUFFIX = 20000000;
    public static final int WAVE_ID_SUFFIX_START = 30000000;
    public static final int WAVE_ID_SUFFIX_STEP = 100000;
    public static final int TRUNK_ROAD_FLAG = 1;
    public static final int ZA_ROAD_FLAG = 2;
    public static final int MODEL_TYPE_FIBER = 10;
    public static final int MODEL_TYPE_LASER = 20;
    public static final int MODEL_TYPE_WAVE = 30;
    public static final int MODEL_TYPE_FIBER_DISRUPT = 15;
    public static final int FIBER_DEFAULT_IDDUP = -1;
    public static final int TRAJ_MODEL_LINE_CAPACITY_LIMIT = 4;
    public static final int TRAJ_MODEL_LINE_EMPTY_LIMIT = 3;
    public static final int TRAJ_MODEL_LINE_STATUS_NEW = 2;
    public static final int TRAJ_MODEL_LINE_STATUS_FINISH = 1;
    public static final int TRAJ_MODEL_LINE_STATUS_EMPTY = 0;

    // 事件类型
    public static final EventTypeModel EVENT_TYPE_NORMAL = new EventTypeModel(0,"#0",0,10000);
    public static final EventTypeModel EVENT_TYPE_AGAINST = new EventTypeModel(1,"#1",50,10000);
    public static final EventTypeModel EVENT_TYPE_PARKING = new EventTypeModel(2,"#2",100,10000);
    public static final EventTypeModel EVENT_TYPE_SLOW = new EventTypeModel(3,"#3",50,10000);
    public static final EventTypeModel EVENT_TYPE_FAST = new EventTypeModel(4,"#4",50,10000);
//    public static final EventTypeModel EVENT_TYPE_OCCUPY = new EventTypeModel(5,"#5",25,10000);

    // 事件判别条件
    public static final Range<Double> EVENT_GENERAL_RANGE = Range.closed(5400.0, 12000.0);
    public static final Range<Double> EVENT_GENERAL_RANGE2 = Range.atLeast(4300.0);

    public static final Range<Double> EVENT_OCCUPY_WH_BLOCK_RANGE1 = Range.closed(4137.5, 4403.7);
    public static final Range<Double> EVENT_OCCUPY_WH_BLOCK_RANGE2 = Range.closed(5256.5, 5352.5);
    public static final Range<Double> EVENT_OCCUPY_WH_BLOCK_RANGE3 = Range.closed(12310.0, 12575.0);
    public static final Range<Double> EVENT_OCCUPY_EZ_BLOCK_RANGE1 = Range.closed(4040.0, 4399.0);
    public static final Range<Double> EVENT_OCCUPY_EZ_BLOCK_RANGE2 = Range.closed(5375.0, 5796.0);
    public static final Range<Double> EVENT_OCCUPY_EZ_BLOCK_RANGE3 = Range.closed(12040.0, 12393.0);

    public static final double EVENT_SPEED_OFFSET = 0.694;
    public static final double EVENT_PARKING_SPEED_FIRST = 2.778;
    public static final double EVENT_PARKING_SPEED_LAST = 2.778;
    public static final double EVENT_PARKING_SPEED = 0;
    public static final double EVENT_PARKING_AVGSPEED = 20;
    public static final double EVENT_SLOW_AVGSPEED = 40;
    public static final Range<Double> EVENT_FAST_SPEED_RANGE = Range.atLeast(65.0);
    public static final Range<Double> EVENT_SLOW_SPPED_RANGE = Range.open(EVENT_PARKING_SPEED, 20.0);

}
