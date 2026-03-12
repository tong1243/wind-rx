package com.wut.screencommonrx.Static;

import java.util.List;

public class DbModuleStatic {
    public static final List<String> DYNAMIC_TABLE_NAMES = List.of(
            "carevent",
            "section",
            "traj_near_real",
            "posture",
            "fibermetric",
            "radarmetric",
            "fibersecmetric",
            "radarsecmetric",
            "radarallsecmetric",
            "parameters",
            "bottleneck_area_state",
            "tunnel_risk",
            "risk_event"
    );

    public static final String TABLE_SUFFIX_SEPARATOR  = "_";
    public static final String TABLE_EVENT_DDL_PREFIX = "carevent" + TABLE_SUFFIX_SEPARATOR;
    public static final String TABLE_SECTION_DDL_PREFIX = "section" + TABLE_SUFFIX_SEPARATOR;
    public static final String TABLE_TRAJ_DDL_PREFIX = "traj_near_real" + TABLE_SUFFIX_SEPARATOR;
    public static final String TABLE_POSTURE_DDL_PREFIX = "posture" + TABLE_SUFFIX_SEPARATOR;
    public static final String TABLE_FIBER_METRIC_DDL_PREFIX = "fibermetric" + TABLE_SUFFIX_SEPARATOR;
    public static final String TABLE_RADAR_METRIC_DDL_PREFIX = "radarmetric" + TABLE_SUFFIX_SEPARATOR;
    public static final String TABLE_FIBER_SEC_METRIC_DDL_PREFIX = "fibersecmetric" + TABLE_SUFFIX_SEPARATOR;
    public static final String TABLE_RADAR_SEC_METRIC_DDL_PREFIX = "radarsecmetric" + TABLE_SUFFIX_SEPARATOR;
    public static final String TABLE_RADAR_ALL_SEC_METRIC_DDL_PREFIX = "radarallsecmetric" + TABLE_SUFFIX_SEPARATOR;
    public static final String TABLE_PARAMETERS_DDL_PREFIX = "parameters" + TABLE_SUFFIX_SEPARATOR;
    public static final String TABLE_BOTTLE_DDL_PREFIX = "bottleneck_area_state" + TABLE_SUFFIX_SEPARATOR;
    public static final String TABLE_RISK_DDL_PREFIX = "tunnel_risk" + TABLE_SUFFIX_SEPARATOR;
    public static final String TABLE_RISK_EVENT_DDL_PREFIX = "risk_event" + TABLE_SUFFIX_SEPARATOR;


    public static final String TABLE_SUFFIX_KEY = "timestamp";
    public static final String REDIS_KEY_PLATE_MODEL_DATA = "plateModelData";
    public static final String REDIS_KEY_FIBER_MODEL_DATA = "fiberModelData";
    public static final String REDIS_KEY_LASER_MODEL_DATA = "laserModelData";
    public static final String REDIS_KEY_WAVE_MODEL_DATA = "waveModelData";
    public static final String REDIS_KEY_FIBER_FLUSH_MODEL = "fiberFlushModel";
    public static final String REDIS_KEY_LASER_FLUSH_MODEL = "laserFlushModel";
    public static final String REDIS_KEY_WAVE_FLUSH_MODEL = "waveFlushModel";
    public static final String REDIS_KEY_TRAJ_DATA = "trajData";
    public static final String REDIS_KEY_TRAJ_BATCH_CACHE = "trajBatchCache";
    public static final String REDIS_KEY_EVENT_INSERT_BATCH_CACHE = "eventInsertBatchCache";
    public static final String REDIS_KEY_EVENT_UPDATE_BATCH_CACHE = "eventUpdateBatchCache";

}
