package com.wut.screencommonrx.Util;

import com.wut.screencommonrx.Model.TrajModel;
import com.wut.screencommonrx.Model.TrajModelLine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.wut.screencommonrx.Static.FusionModuleStatic.*;

public class DataParamParseUtil {
    // 用于筛选集合中指定字段相同的元素,使其成为唯一键
    // 可用于stream().filter()的筛选条件,但无法指定出现相同元素时,具体保留哪个元素的逻辑
    public static <T> Predicate<T> modelDistinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object,Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    public static String getPostureComp(List<Integer> list) {
        return list.stream().map(i -> Integer.toString(i)).collect(
            Collectors.joining("-", "[", "]")
        );
    }

    public static TrajModelLine getTrajModelLineInstance(TrajModel trajModel) {
        return new TrajModelLine(TRAJ_MODEL_LINE_STATUS_NEW, 0, 0L, 0, 0, false, new ArrayList<>(List.of(trajModel)));
    }

    public static double getNextFrenetXPrediction(TrajModel trajModel) {
        return switch(trajModel.getRoadDirect()) {
            case ROAD_DIRECT_TO_WH -> trajModel.getFrenetX() + (trajModel.getSpeedX() * FRENETX_PREDICT_FACTOR);
            case ROAD_DIRECT_TO_EZ -> trajModel.getFrenetX() - (trajModel.getSpeedX() * FRENETX_PREDICT_FACTOR);
            default -> trajModel.getFrenetX();
        };
    }
    public static String getPositionStr(double frenetx) {
        int pos = Double.valueOf(frenetx).intValue();
        return "K" + (pos / 1000) + "+" + (pos % 1000 == 0 ? 0 : pos % 1000);
    }

    public static double getRoundValue(double value) {
        BigDecimal decimal = new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
        return decimal.doubleValue();
    }
}
