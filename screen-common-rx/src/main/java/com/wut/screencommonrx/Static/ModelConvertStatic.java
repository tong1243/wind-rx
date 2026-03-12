package com.wut.screencommonrx.Static;

import com.google.common.collect.Range;

public class ModelConvertStatic {
    public static final double MERCATOR_E0 = 500;
    public static final double MERCATOR_N0 = 0;
    public static final double MERCATOR_K0A = 0.9996 * 6378.137;
    public static final double MERCATOR_E = 0.0818192;
    public static final double MERCATOR_E2 = Math.pow(MERCATOR_E, 2);
    public static final double MERCATOR_E4 = Math.pow(MERCATOR_E, 4);
    public static final double MERCATOR_E6 = Math.pow(MERCATOR_E, 6);
    public static final double MERCATOR_PI = Math.PI / 180.0;
    public static final double MERCATOR_S_PART1 = 1 - (MERCATOR_E2 / 4) - (3 * MERCATOR_E4 / 64) - (5 * MERCATOR_E6 / 256);
    public static final double MERCATOR_S_PART2 = (3 * MERCATOR_E2 / 8) + (3 * MERCATOR_E4 / 32) + (45 * MERCATOR_E6 / 1024);
    public static final double MERCATOR_S_PART3 = (15 * MERCATOR_E4 / 256) + (45 * MERCATOR_E6 / 1024);
    public static final double MERCATOR_S_PART4 = 35 * MERCATOR_E6 / 3072;
    public static final double MERCATOR_C_FACTOR = MERCATOR_E2 / (1 - MERCATOR_E2);

    public static final double COORDINATE_PI = 180.0 / Math.PI;
    public static final double COORDINATE_LATITUDE_OFFSET = 0.000000400372863181963;
    public static final double COORDINATE_LONGITUDE_OFFSET = -0.0000000688086842459646;
    public static final double COORDINATE_E = 2.718281828;
    public static final double COORDINATE_C_SA = 6378137.000000;
    public static final double COORDINATE_C_SB = 6356752.314245;
    public static final double COORDINATE_C = Math.pow(COORDINATE_C_SA, 2) / COORDINATE_C_SB;
    public static final double COORDINATE_E2_CUADRADA = Math.pow((Math.pow(Math.pow(COORDINATE_C_SA, 2) - Math.pow(COORDINATE_C_SB, 2), 0.5) / COORDINATE_C_SB), 2);
    public static final double COORDINATE_LAT_FACTOR = COORDINATE_C_SA * 0.9996;
    public static final double COORDINATE_ALFA = (3.0 / 4.0) * COORDINATE_E2_CUADRADA;
    public static final double COORDINATE_BETA = (5.0 / 3.0) * Math.pow(COORDINATE_ALFA, 2);
    public static final double COORDINATE_GAMA = (35.0 / 27.0) * Math.pow(COORDINATE_ALFA, 3);
    public static final double COORDINATE_BM_FACTOR = 0.9996 * COORDINATE_C;

    // 车道边线与中线FrenetY标准值
    public static final double LANE_ONE_MIDDLE_FRENETY = 3.75 / 2;
    public static final double LANE_TWO_MIDDLE_FRENETY = 3.75 + (3.75 / 2);
    public static final double LANE_THREE_MIDDLE_FRENETY = (3.75 * 2) + (3.75 / 2);
    public static final double LANE_NINE_MIDDLE_FRENETY = (3.75 * 3) + (3.5 / 2);
    public static final double LANE_ZA_MIDDLE_FRENETY = (3.75 * 3) + 2.5;
    public static final double LANE_ONE_EDGE_FRENETY = 3.75;
    public static final double LANE_TWO_EDGE_FRENETY = 3.75 * 2;
    public static final double LANE_THREE_EDGE_FRENETY = 3.75 * 3;
    public static final double LANE_ZA_EDGE_DISTANCE = 2.5;
    public static final double LANE_NINE_EDGE_DISTANCE = 1.75;

    public static final int CAR_TYPE_COMPACT = 1;
    public static final int CAR_TYPE_BUS = 2;
    public static final int CAR_TYPE_TRUNK = 3;
    public static final Range<Double> CAR_TYPE_COMPACT_RANGE = Range.closed(3.5, 8.0);
    public static final Range<Double> CAR_TYPE_BUS_RANGE = Range.closed(8.0, 12.5);
    public static final Range<Double> CAR_TYPE_TRUNK_RANGE = Range.atLeast(12.5);


    public static final Range<Double> LASER_FRENET_RANGE = Range.closed(3470.0, 4475.0);
    public static final Range<Integer> LASER_ZA_LANE_RANGE = Range.closed(6,7);

    // 鄂州至武汉方向(方向1/sx)匝道桩号范围
    public static final Range<Double> MODEL_WH_ZA_RANGE1 = Range.closed(4165.3, 4792.2);
    public static final Range<Double> MODEL_WH_ZA_RANGE2 = Range.closed(5254.5, 5655.1);
    public static final Range<Double> MODEL_WH_ZA_RANGE3 = Range.closed(12336.8, 12552.5);

    // 武汉至鄂州方向(方向2/xx)匝道桩号范围
    public static final Range<Double> MODEL_EZ_ZA_RANGE1 = Range.closed(4036.1, 4706.3);
    public static final Range<Double> MODEL_EZ_ZA_RANGE2 = Range.closed(5111.3, 5716.5);
    public static final Range<Double> MODEL_EZ_ZA_RANGE3 = Range.closed(11800.1, 12550.6);

    // 鄂州至武汉方向(方向1/sx)加速车道桩号范围
    public static final Range<Double> MODEL_WH_ACCX_RANGE1 = Range.closed(4165.3, 4358.7);
    public static final Range<Double> MODEL_WH_ACCX_RANGE2 = Range.closed(5254.6, 5655.1);
    public static final Range<Double> MODEL_WH_ACCX_RANGE3 = Range.closed(12336.8, 12483.3);

    // 武汉至鄂州方向(方向2/xx)加速车道桩号范围
    public static final Range<Double> MODEL_EZ_ACCX_RANGE1 = Range.closed(4036.1, 4331.6);
    public static final Range<Double> MODEL_EZ_ACCX_RANGE2 = Range.closed(5557.4, 5716.5);
    public static final Range<Double> MODEL_EZ_ACCX_RANGE3 = Range.closed(11800.1, 12140.5);

}
