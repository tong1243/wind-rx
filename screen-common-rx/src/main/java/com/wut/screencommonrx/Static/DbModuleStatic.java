package com.wut.screencommonrx.Static;

import java.util.List;

public class DbModuleStatic {
    public static final List<String> DYNAMIC_TABLE_NAMES = List.of(
            "traj_near_real"
    );

    public static final String TABLE_SUFFIX_SEPARATOR  = "_";
    public static final String TABLE_TRAJ_DDL_PREFIX = "traj_near_real" + TABLE_SUFFIX_SEPARATOR;


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
