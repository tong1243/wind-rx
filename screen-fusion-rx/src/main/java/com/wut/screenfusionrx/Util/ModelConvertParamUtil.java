package com.wut.screenfusionrx.Util;

import com.wut.screencommonrx.Model.ModelConvertData.Coordinate;
import com.wut.screencommonrx.Model.ModelConvertData.Frenet;
import com.wut.screencommonrx.Model.ModelConvertData.Mercator;
import com.wut.screencommonrx.Util.CollectionEmptyUtil;
import com.wut.screendbmongorx.Document.Point;
import com.wut.screenfusionrx.Model.RotationMatrixModel;
import org.springframework.util.function.SupplierUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.wut.screencommonrx.Static.FusionModuleStatic.ROAD_DIRECT_TO_EZ;
import static com.wut.screencommonrx.Static.FusionModuleStatic.ROAD_DIRECT_TO_WH;
import static com.wut.screencommonrx.Static.ModelConvertStatic.*;

public class ModelConvertParamUtil {
    public record UTMFactor(double x, double y) {}

    // 经纬度坐标转UTM坐标
    public static Mercator coordinateToMercator(Coordinate model) {
        double phi = model.getLatitude() * MERCATOR_PI;
        int zone = (int)(model.getLongitude() / 6) + 30;
        double V = 1 / Math.sqrt(1 - (MERCATOR_E2 * Math.pow(Math.sin(phi), 2)));
        double A = ((model.getLongitude() * MERCATOR_PI) - (((zone * 6) - 177) * MERCATOR_PI)) * Math.cos(phi);
        double T = Math.pow(Math.tan(phi), 2);
        double C = MERCATOR_C_FACTOR * Math.pow(Math.cos(phi), 2);
        double S = (MERCATOR_S_PART1 * phi)
                - (MERCATOR_S_PART2 * Math.sin(2 * phi))
                + (MERCATOR_S_PART3 * Math.sin(4 * phi))
                - (MERCATOR_S_PART4 * Math.sin(6 * phi));
        return new Mercator(
                // UTM坐标系下横向坐标
                (MERCATOR_E0 + (MERCATOR_K0A * V * (A
                        + ((1 - T + C) * Math.pow(A, 3) / 6)
                        + ((5 - (18 * T) + Math.pow(T, 2)) * Math.pow(A, 5) / 120)
                ))) * 1000,
                // UTM坐标系下纵向坐标
                (MERCATOR_N0 + (MERCATOR_K0A * (S + (V * Math.tan(phi) * (
                        (Math.pow(A, 2) / 2)
                        + ((5 - T + (9 * C) + (4 * Math.pow(C, 2))) * Math.pow(A, 4) / 24)
                        + ((61 - (58 * T) + Math.pow(T, 2)) * Math.pow(A, 6) / 720)
                ))))) * 1000
        );
    }

    // UTM坐标转经纬度坐标
    public static Coordinate mercatorToCoordinate(Mercator model) {
        double lat = model.getY() / COORDINATE_LAT_FACTOR;
        double V = COORDINATE_BM_FACTOR / Math.pow(1 + COORDINATE_E2_CUADRADA * Math.cos(lat) * Math.cos(lat), 0.5);
        double A = (model.getX() - 500000) / V;
        double A1 = Math.sin(2 * lat);
        double A2 = A1 * Math.pow(Math.cos(lat), 2);
        double J2 = lat + (A1 / 2);
        double J4 = ((3 * J2) + A2) / 4.0;
        double J6 = ((5 * J4) + Math.pow(A2 * Math.cos(lat), 2)) / 3.0;
        double B = (model.getY() - (COORDINATE_BM_FACTOR * (lat
                - COORDINATE_ALFA * J2
                + COORDINATE_BETA * J4
                - COORDINATE_GAMA * J6
        ))) / V;
        double epsi = (COORDINATE_E2_CUADRADA * Math.pow(A, 2)) / 2.0 * Math.pow(Math.cos(lat), 2);
        double eps = A * (1 - (epsi / 3.0));
        double nab = (B * (1 - epsi)) + lat;
        double senoheps = (Math.pow(COORDINATE_E, eps) - Math.pow(COORDINATE_E, (-eps))) / 2.0;
        double delt = Math.atan(senoheps / Math.cos(nab));
        double taominlat = Math.atan(Math.cos(delt) * Math.tan(nab)) - lat;
        return new Coordinate(
                COORDINATE_LONGITUDE_OFFSET + ((delt * COORDINATE_PI) + 117),
                COORDINATE_LATITUDE_OFFSET + (
                        (lat + (taominlat * (1
                                + (COORDINATE_E2_CUADRADA * Math.pow(Math.cos(lat), 2))
                                - (1.5 * COORDINATE_E2_CUADRADA * Math.sin(lat) * Math.cos(lat) * taominlat)
                        ))) * COORDINATE_PI
                )
        );
    }

    // Frenet坐标转UTM坐标
    public static Mercator frenetToMercator(Frenet frenet, Map<Double, Point> pointMap, int direction, double d) {
        Point point = pointMap.get(frenet.getX());
        List<Double> tangentHighList = Optional.ofNullable(pointMap.get(Math.round((frenet.getX() + (0.1 * direction)) * 10) / 10.0)).map((pointHigh) -> {
            double detax1 = pointHigh.getX() - point.getX();
            double detay1 = pointHigh.getY() - point.getY();
            double factor1 = Math.sqrt(Math.pow(detax1, 2) + Math.pow(detay1, 2));
            return List.of(detax1 / factor1, detay1 / factor1);
        }).orElse(List.of());
        List<Double> tangentLowList = Optional.ofNullable(pointMap.get(Math.round((frenet.getX() - (0.1 * direction)) * 10) / 10.0)).map((pointLow) -> {
            double detay2 = point.getY() - pointLow.getY();
            double detax2 = point.getX() - pointLow.getX();
            double factor2 = Math.sqrt(Math.pow(detax2, 2) + Math.pow(detay2, 2));
            return List.of(detax2 / factor2, detay2 / factor2);
        }).orElse(List.of());
        UTMFactor factor = SupplierUtils.resolve(() -> {
            if (CollectionEmptyUtil.forList(tangentHighList)) {
                return new UTMFactor(tangentLowList.get(1), (-1) * tangentLowList.get(0));
            }
            if (CollectionEmptyUtil.forList(tangentLowList)) {
                return new UTMFactor(tangentHighList.get(1), (-1) * tangentHighList.get(0));
            }
            return new UTMFactor(
                    (tangentHighList.get(1) + tangentLowList.get(1)) / 2,
                    (-1) * (tangentLowList.get(0) + tangentHighList.get(0)) / 2
            );
        });
        assert factor != null;
        return new Mercator(
                point.getX() + (d * factor.x),
                point.getY() + (d * factor.y)
        );
    }

    // UTM坐标转Frenet坐标
    // 现在FrenetY返回的是距离道路边线的有向距离,正代表在行驶方向靠右,负代表在行驶方向靠左
    public static Frenet mercatorToFrenet(Mercator mercator, Point point, Point pointNext) {
        int vector = 1;
        if (pointNext != null) {
            double paraDot = ((pointNext.getX() - point.getX()) * (mercator.getY() - point.getY()))
                           + ((pointNext.getY() - point.getY()) * (mercator.getX() - point.getX()));
            vector = paraDot > 0 ? -1 : 1;
        }
        return new Frenet(
                point.getFrenetx(),
                vector * Math.sqrt(
                        Math.pow((mercator.getX() - point.getX()), 2)
                        + Math.pow((mercator.getY() - point.getY()), 2)
                )
        );
    }

    // 微波雷达经纬度转UTM坐标旋转
    public static Mercator mercatorRectify(Mercator mercator, RotationMatrixModel matrixModel) {
        // (x1, y1) + (offsetX, offsetY) - (centerX, centerY)
        // (x2, y2) * [cos(angle) , sin(angle)]
        //            [-sin(angle), cos(angle)]
        // (x3, y3) + (centerX, centerY)
        double mx = (mercator.getX() + matrixModel.getOffsetX()) - matrixModel.getCenterX();
        double my = (mercator.getY() + matrixModel.getOffsetY()) - matrixModel.getCenterY();
        return new Mercator(
                ((mx * matrixModel.getCosAngle()) - (my * matrixModel.getSinAngle())) + matrixModel.getCenterX(),
                ((mx * matrixModel.getSinAngle()) + (my * matrixModel.getCosAngle())) + matrixModel.getCenterY()
        );
    }

    // 车道转FrenetY
    public static double laneToFrenety(int lane) {
        return switch(lane) {
            case 1 -> LANE_ONE_MIDDLE_FRENETY;
            case 2 -> LANE_TWO_MIDDLE_FRENETY;
            case 3 -> LANE_THREE_MIDDLE_FRENETY;
            case 6, 7 -> LANE_ZA_MIDDLE_FRENETY;
            case 4, 9 -> LANE_NINE_MIDDLE_FRENETY;
            default -> 0.0;
        };
    }

    // 车道转离车道边线的距离
    public static double laneToDistance(int lane, double frenetY) {
        return switch(lane) {
            case 1, 2, 3 -> frenetY;
            case 6, 7 -> LANE_ZA_EDGE_DISTANCE;
            case 4, 9 -> LANE_NINE_EDGE_DISTANCE;
            default -> 0.0;
        };
    }

    // FrenetY转车道(只包含非匝道情况下的判断)
    // frenetY向量 <0 时,判定为对向车道并返回0
    // frenetY向量 >11.25 时判定为应急车道并返回9
    public static int frenetyToLane(double frenetY) {
        if (frenetY < 0) { return 0; }
        if (frenetY < LANE_ONE_EDGE_FRENETY) { return 1; }
        if (frenetY < LANE_TWO_EDGE_FRENETY) { return 2; }
        if (frenetY < LANE_THREE_EDGE_FRENETY) { return 3; }
        return 9;
    }

    // 获取车辆行驶方向关联的单位向量
    public static int directionToVector(int direction) {
        // 上行(鄂州到武汉方向)为1,下行(武汉到鄂州方向)为-1
        return direction == ROAD_DIRECT_TO_WH ? 1 : -1;
    }

    // 车长转车辆类型
    // 车长在[3.5, 8]时,划分为小型汽车
    // 车长在[8, 12.5]时,划分为中型客车
    // 车长>12.5时,划分为大型货车
    public static int carLengthToType(double length) {
        if (CAR_TYPE_COMPACT_RANGE.contains(length)) { return CAR_TYPE_COMPACT; }
        if (CAR_TYPE_BUS_RANGE.contains(length)) { return CAR_TYPE_BUS; }
        if (CAR_TYPE_TRUNK_RANGE.contains(length)) { return CAR_TYPE_TRUNK; }
        return CAR_TYPE_COMPACT;
    }

    public static boolean isOnZaRoad(double frenetX, int direction) {
        return switch(direction) {
            case ROAD_DIRECT_TO_WH -> MODEL_WH_ZA_RANGE1.contains(frenetX) || MODEL_WH_ZA_RANGE2.contains(frenetX) || MODEL_WH_ZA_RANGE3.contains(frenetX);
            case ROAD_DIRECT_TO_EZ -> MODEL_EZ_ZA_RANGE1.contains(frenetX) || MODEL_EZ_ZA_RANGE2.contains(frenetX) || MODEL_EZ_ZA_RANGE3.contains(frenetX);
            default -> false;
        };
    }

    public static boolean isOnAccxRoad(double frenetX, int direction) {
        return switch(direction) {
            case ROAD_DIRECT_TO_WH -> MODEL_WH_ACCX_RANGE1.contains(frenetX) || MODEL_WH_ACCX_RANGE2.contains(frenetX) || MODEL_WH_ACCX_RANGE3.contains(frenetX);
            case ROAD_DIRECT_TO_EZ -> MODEL_EZ_ACCX_RANGE1.contains(frenetX) || MODEL_EZ_ACCX_RANGE2.contains(frenetX) || MODEL_EZ_ACCX_RANGE3.contains(frenetX);
            default -> false;
        };
    }

}
