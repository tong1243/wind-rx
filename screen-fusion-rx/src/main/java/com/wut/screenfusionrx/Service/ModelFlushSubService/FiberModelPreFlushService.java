package com.wut.screenfusionrx.Service.ModelFlushSubService;

import cn.hutool.json.ObjectMapper;
import com.wut.screencommonrx.Model.ModelConvertData.Coordinate;
import com.wut.screencommonrx.Model.ModelConvertData.Frenet;
import com.wut.screencommonrx.Model.ModelConvertData.Mercator;
import com.wut.screencommonrx.Model.VehicleModel;
import com.wut.screencommonrx.Util.CollectionEmptyUtil;
import com.wut.screencommonrx.Util.DataParamParseUtil;
import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screendbmongorx.Document.Point;
import com.wut.screendbredisrx.Service.RedisModelDataService;
import com.wut.screenfusionrx.Context.ModelDataContext;
import com.wut.screenfusionrx.Util.ModelConvertParamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static com.wut.screencommonrx.Static.FusionModuleStatic.ASYNC_SERVICE_TIMEOUT;
import static com.wut.screencommonrx.Static.FusionModuleStatic.FIBER_ID_SUFFIX;
import static com.wut.screencommonrx.Static.ModelConvertStatic.LASER_ZA_LANE_RANGE;

@Component
public class FiberModelPreFlushService {
    // 光纤数据:
    // 原始的是车道和桩号,根据车道得出横向位置FrenetY,根据桩号得出纵向位置FrenetX
    // 再根据FrenetXY计算出UTM,这时要调用参考线静态表
    // 最后将UTM转成经纬度,此时航向角也是沿用车道参考线方向
    @Qualifier("fusionTaskModelPreFlushAsyncPool")
    private final Executor fusionTaskModelPreFlushAsyncPool;
    @Qualifier("fusionTaskModelPreFlushProcessAsyncPool")
    private final Executor fusionTaskModelPreFlushProcessAsyncPool;
    private final RedisModelDataService redisModelDataService;
    private final ModelDataContext modelDataContext;

    @Autowired
    public FiberModelPreFlushService(Executor fusionTaskModelPreFlushAsyncPool, Executor fusionTaskModelPreFlushProcessAsyncPool, RedisModelDataService redisModelDataService, ModelDataContext modelDataContext) {
        this.fusionTaskModelPreFlushAsyncPool = fusionTaskModelPreFlushAsyncPool;
        this.fusionTaskModelPreFlushProcessAsyncPool = fusionTaskModelPreFlushProcessAsyncPool;
        this.redisModelDataService = redisModelDataService;
        this.modelDataContext = modelDataContext;
    }

    public CompletableFuture<Void> startPreFlush(List<VehicleModel> fiberModelList) {
//        MessagePrintUtil.printPreFlushStart();
        return CompletableFuture.runAsync(() -> {
            if (CollectionEmptyUtil.forList(fiberModelList)) {
                
                MessagePrintUtil.printException(new Exception("fiberModelList is empty"), "startFiberPreFlush");
                return; }
            try {
//                MessagePrintUtil.printFiberModelData(fiberModelList.toString());
                List<CompletableFuture<VehicleModel>> processTaskList = fiberModelList.stream()
                        // 过滤相同时间戳相同ID(对某条轨迹重复记录)的光纤数据
                        .filter(DataParamParseUtil.modelDistinctByKey(VehicleModel::getId))
                        // 过滤桩号范围在匝道,但车道不为[6,7]匝道范围的光纤数据
//                        .filter(fiber -> !LASER_ZA_LANE_RANGE.contains(fiber.getLane()) || ModelConvertParamUtil.isOnZaRoad(fiber.getFiberX(), Integer.parseInt(fiber.getRoadDirect())))
                        .map(fiber -> CompletableFuture.supplyAsync(() -> processFiberModel(fiber), fusionTaskModelPreFlushProcessAsyncPool))
                        .toList();
                CompletableFuture.allOf(processTaskList.toArray(CompletableFuture[]::new)).get(ASYNC_SERVICE_TIMEOUT, TimeUnit.SECONDS);
                redisModelDataService.storeFiberFlushModel(processTaskList.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .sorted(Comparator.comparingDouble(VehicleModel::getFrenetX))
                        .toList()
                ).get();
            } catch (Exception e) { MessagePrintUtil.printException(e, "startFiberPreFlush"); }
        }, fusionTaskModelPreFlushAsyncPool);
    }

    public VehicleModel processFiberModel(VehicleModel fiber) {
//        MessagePrintUtil.printFiberModel(fiber.toString());
        // 1:车道转FrenetY,获取原始Frenet坐标及距离车道边线的距离
//        Frenet frenet = new Frenet(fiber.getFiberX(), ModelConvertParamUtil.laneToFrenety(fiber.getLane()));
//        double laneEdgeDistance = ModelConvertParamUtil.laneToDistance(fiber.getLane(), frenet.getY());
        // 2:查找道路静态表(根据车道判断查找主干道或匝道),获取[-0.1, +0.1]范围内的记录列表
//        Map<Double, Point> pointMap = modelDataContext.getFiberMatchPoint(fiber);
        // 3:Frenet坐标转UTM坐标
//        Mercator mercator = ModelConvertParamUtil.frenetToMercator(frenet, pointMap, ModelConvertParamUtil.directionToVector(Integer.parseInt(fiber.getRoadDirect())), laneEdgeDistance);
//        // 4:UTM坐标转经纬度坐标
//        Coordinate coordinate = ModelConvertParamUtil.mercatorToCoordinate(mercator);
        // 坐标转换结果输出,非必要情况下应当注释
        // MessagePrintUtil.printModelConvert("FIBER FRENET TO MERCATOR", frenet.toString(), mercator.toString());
        // MessagePrintUtil.printModelConvert("FIBER MERCATOR TO COORDINATE", mercator.toString(), coordinate.toString());
        fiber.setFrenetX(fiber.getFrenetX());
        fiber.setFrenetY(fiber.getFrenetY());
//        fiber.setLongitude(coordinate.getLongitude());
//        fiber.setLatitude(coordinate.getLatitude());
//        fiber.setMercatorX(mercator.getX());
//        fiber.setMercatorY(mercator.getY());
//        fiber.setHeadingAngle(pointMap.get(fiber.getFrenetX()).getAngle());
        fiber.setSpeed(Math.abs(fiber.getSpeed()));
        fiber.setSpeedX(Math.abs(fiber.getSpeedX()));
        fiber.setSpeedY(Math.abs(fiber.getSpeedY()));
        fiber.setId(FIBER_ID_SUFFIX + fiber.getId());
//        MessagePrintUtil.printFiberModel("FiberModelPreFlushService"+fiber.toString());
        return fiber;
    }

}
