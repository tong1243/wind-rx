package com.wut.screencommonrx.Util;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.logging.Logger;

public class MessagePrintUtil {
    private static final Logger LOGGER = Logger.getLogger("ROOT");
    public static void printListenerReceive(String key, String data) {
        LOGGER.info("[RX LISTENER " + key.toUpperCase() + " RECEIVED FROM TX] " + data);
    }

    public static void printModelFlush(long timestamp) {
        LOGGER.info("[MODEL FLUSH PART TASK] " + timestamp + " PROCESS FINISHED");
    }

    public static void printTrajFusionMain(long timestamp) {
        LOGGER.info("[TRAJ FUSION PART TASK] " + timestamp + " PROCESS FINISHED");
    }

    public static void printCarPlateBind(String fore, String after) {
        LOGGER.info("[TRAJ FUSION BIND CARPLATE] PROCESS SUCCESS ON [" + fore + "-->" + after + "]");
    }

    public static void printProducerTransmit(String key, String data) {
        LOGGER.info("[RX PRODUCER " + key.toUpperCase() + " TRANSMITTED TO SX] " + data);
    }

    public static void printPostureData(long start, long end) {
        LOGGER.info("[POSTURE DATA PART TASK] PROCESS FINISHED ON [" + start + "-->" + end + "]");
    }

    public static void printSectionData(long start, long end) {
        LOGGER.info("[SECTION DATA PART TASK] PROCESS FINISHED ON [" + start + "-->" + end + "]");
    }

    public static void printEventData(long timestamp) {
        LOGGER.info("[EVENT DATA PART TASK] " + timestamp + " PROCESS FINISHED");
    }

    public static void printDeviceData(long start, long end) {
        LOGGER.info("[DEVICE DATA PART TASK] PROCESS FINISHED ON [" + start + "-->" + end + "]");
    }

    public static void printDeviceSecData(long start, long end) {
        LOGGER.info("[DEVICE SECTION DATA PART TASK] PROCESS FINISHED ON [" + start + "-->" + end + "]");
    }

    public static void printDbState(String datetime) {
        LOGGER.info("[MYSQL DB INITIALIZE] CREATE/RESET TABLE section_" + datetime);
        LOGGER.info("[MYSQL DB INITIALIZE] CREATE/RESET TABLE carevent_" + datetime);
        LOGGER.info("[MYSQL DB INITIALIZE] CREATE/RESET TABLE traj_near_real_" + datetime);
        LOGGER.info("[MYSQL DB INITIALIZE] CREATE/RESET TABLE posture_" + datetime);
        LOGGER.info("[MYSQL DB INITIALIZE] CREATE/RESET TABLE fibermetric_" + datetime);
        LOGGER.info("[MYSQL DB INITIALIZE] CREATE/RESET TABLE radarmetric_" + datetime);
        LOGGER.info("[MYSQL DB INITIALIZE] CREATE/RESET TABLE fibersecmetric_" + datetime);
        LOGGER.info("[MYSQL DB INITIALIZE] CREATE/RESET TABLE radarsecmetric_" + datetime);
    }

    public static void printException(Exception e, String info) {
        LOGGER.warning("[CATCH EXCEPTION IN FUNCTION ---" + info + "--- ] " + ExceptionUtils.getStackTrace(e));
    }

    public static void printTrajModelList(long timestamp, String list) {
        LOGGER.info("[TRAJ FUSION MODEL LINE] (" + timestamp + ") " + list);
    }

    public static void printEventModelMap(long timestamp, String map) {
        LOGGER.info("[EVENT DATA MODEL MAP] (" + timestamp + ") " + map);
    }

    public static void printModelConvert(String topic, String origin, String now) {
        LOGGER.info("[" + topic + "] " + origin + " -> " + now);
    }

    public static void printTrajModelLineListToLogger(String data) {
        LOGGER.info("[TRAJ MODEL LINE LIST ] :" + data  );
    }

    public static void printFiberException(String key) {
        LOGGER.warning("[FIBER MODEL LIST] " + key + " IS EMPTY");
    }

    public static void printFiberModel(String s) {
        LOGGER.info("[FIBER MODEL] " + s);
    }

    public static void printCollectFiberModelDataStart() {
        LOGGER.info("[COLLECT FIBER MODEL DATA] START");
    }

    public static void printCollectFiberFlushModelStart() {
        LOGGER.info("[COLLECT FIBER FLUSH MODEL DATA] START");
    }

    public static void printPreFlushStart() {
        LOGGER.info("[PRE FLUSH] START");
    }

    public static void printFiberModelData(String string) {
        LOGGER.info("[FIBER MODEL DATA] " + string);
    }

    public static void printTrajList(long timestamp, String string) {
        LOGGER.info("[TRAJ LIST] (" + timestamp + ") " + string);
    }

    public static void printstoreBottleneckAreaStateData() {
        LOGGER.info("[STORE BOTTLENECK AREA STATE DATA] START");
    }

    public static void printStoreParametersData() {
        LOGGER.info("[STORE PARAMETERS DATA] START");
    }

    public static void printCaculateSuccess(long timestamp) {
        LOGGER.info("[CACULATE SUCCESS] " + timestamp);
    }

    public static void printTaskStart(long timestamp) {
        LOGGER.info("[TASK START] " + timestamp );
    }
    public static void printRiskCount(int count){
        LOGGER.info("[TTC COUNT] " + count);
    }

    public static void printTSCandRiskCount(double ttcStar, int riskCount) {
        LOGGER.info("[TTSSTAR AND RISKCOUNT] " + ttcStar + riskCount);

    }

    public static void printTTCStarandCar(String traj, double ttcStar) {
        LOGGER.info("[TTSSTAR AND Car] " + ttcStar + traj);
    }

    public static void printRiskEventCount(int sectionRiskEventCount) {
        LOGGER.info("[RISK EVENT COUNT] " + sectionRiskEventCount);
    }
}

