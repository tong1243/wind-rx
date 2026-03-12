package com.wut.screencommonrx.Static;

public class MsgModuleStatic {
    public static final String TOPIC_NAME_PLATE = "plate";
    public static final String TOPIC_NAME_FIBER = "fiber";
    public static final String TOPIC_NAME_LASER = "laser";
    public static final String TOPIC_NAME_WAVE = "wave";
    public static final String TOPIC_NAME_TRAJ = "traj";
    public static final String TOPIC_NAME_DIRECT = "direct";
    public static final String TOPIC_NAME_TIMESTAMP = "timestamp";
    public static final String QUEUE_DEFAULT_EXCHANGE = "";
    public static final String QUEUE_NAME_FLUSH = "flush";
    public static final String QUEUE_NAME_FUSION = "fusion";
    public static final String QUEUE_NAME_SECTION = "section";
    public static final String QUEUE_NAME_EVENT = "event";
    public static final String QUEUE_NAME_POSTURE = "posture";
    public static final String QUEUE_NAME_DEVICE = "device";
    public static final String QUEUE_NAME_RISK = "risk";

    public static final int TOPIC_DEFAULT_PARTITIONS = 10;
    public static final int TOPIC_DEFAULT_REPLICAS = 1;
    public static final long MODEL_FLUSH_WAIT_TIME = 2000;
    public static final long TRAJ_FUSION_WAIT_TIME = 2000;

    public static final long DATA_SYNC_TIMEOUT = 1000;

}
