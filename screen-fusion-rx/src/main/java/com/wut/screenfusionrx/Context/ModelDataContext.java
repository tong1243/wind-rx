package com.wut.screenfusionrx.Context;

import com.google.common.collect.Range;
import com.wut.screencommonrx.Model.ModelConvertData.Mercator;
import com.wut.screencommonrx.Model.VehicleModel;
import com.wut.screendbmongorx.Document.Point;
import com.wut.screendbmongorx.Repository.*;
import com.wut.screendbmongorx.Util.MongoModelTransformUtil;
import com.wut.screendbmysqlrx.Model.Rotation;
import com.wut.screenfusionrx.Model.RotationMatrixModel;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.wut.screencommonrx.Static.FusionModuleStatic.*;

@Component
public class ModelDataContext {
    @Qualifier("fusionTaskModelFlushAsyncPool")
    private final Executor fusionTaskModelFlushAsyncPool;
    private final PointsToWHRepository pointsToWHRepository;
    private final PointsToEZRepository pointsToEZRepository;
    private final PointsToWHzaRepository pointsToWHzaRepository;
    private final PointsToEZzaRepository pointsToEZzaRepository;
    private final VehicleModelDocuRepository vehicleModelDocuRepository;
//    private final RotationService rotationService;
    // 微波雷达设备静态表
    @Getter
    private final Map<String, RotationMatrixModel> waveRotationMap = new HashMap<>();

    @Autowired
    public ModelDataContext(Executor fusionTaskModelFlushAsyncPool, PointsToWHRepository pointsToWHRepository, PointsToEZRepository pointsToEZRepository, PointsToWHzaRepository pointsToWHzaRepository, PointsToEZzaRepository pointsToEZzaRepository, VehicleModelDocuRepository vehicleModelDocuRepository) {
        this.fusionTaskModelFlushAsyncPool = fusionTaskModelFlushAsyncPool;
        this.pointsToWHRepository = pointsToWHRepository;
        this.pointsToEZRepository = pointsToEZRepository;
        this.pointsToWHzaRepository = pointsToWHzaRepository;
        this.pointsToEZzaRepository = pointsToEZzaRepository;
//        this.rotationService = rotationService;
        this.vehicleModelDocuRepository = vehicleModelDocuRepository;
    }

    @PostConstruct
    public void initModelDataContext() {
//        updateWaveRotation().thenRunAsync(() -> {});
//        resetVehicleModelDocu().thenRunAsync(() -> {});
    }

//    public CompletableFuture<Void> updateWaveRotation() {
//        return CompletableFuture.runAsync(() -> {
//            waveRotationMap.clear();
//            AtomicInteger suffix = new AtomicInteger(WAVE_ID_SUFFIX_START);
//            List<Rotation> rotationList = rotationService.getAllRotation();
//            rotationList.stream().forEach(rotation -> {
//                waveRotationMap.put(rotation.getIp(), new RotationMatrixModel(
//                        rotation.getCenterX(),
//                        rotation.getCenterY(),
//                        rotation.getOffsetX(),
//                        rotation.getOffsetY(),
//                        rotation.getRoadDirection(),
//                        rotation.getDeviceDirection(),
//                        rotation.getStatus(),
//                        rotation.getSid(),
//                        Range.closed(rotation.getMinX(), rotation.getMaxX()),
//                        Math.cos(rotation.getAngle()),
//                        Math.sin(rotation.getAngle()),
//                        suffix.getAndAdd(WAVE_ID_SUFFIX_STEP)
//                ));
//            });
//        }, fusionTaskModelFlushAsyncPool);
//    }

    public CompletableFuture<Void> resetVehicleModelDocu() {
        return CompletableFuture.runAsync(vehicleModelDocuRepository::deleteAll, fusionTaskModelFlushAsyncPool);
    }

    // 查找光纤数据FrenetX对应道路静态表上的记录
    public Map<Double, Point> getFiberMatchPoint(VehicleModel fiber) {
        double min = fiber.getFrenetX() - 0.2;
        double max = fiber.getFrenetX() + 0.2;
        return switch(Integer.parseInt(fiber.getRoadDirect())) {
            // 鄂州至武汉方向
            case ROAD_DIRECT_TO_WH -> fiber.getLane() == 6 || fiber.getLane() ==7
                    // 车道为匝道,查找匝道道路静态表
                    ? pointsToWHzaRepository.findByFrenetxIsBetween(min, max).stream()
                        .map(MongoModelTransformUtil::pointsToWHzaReduce)
                        .collect(Collectors.toMap(Point::getFrenetx, point -> point))
                    // 车道为主干道,查找主干道道路静态表
                    : pointsToWHRepository.findByFrenetxIsBetween(min, max).stream()
                        .map(MongoModelTransformUtil::pointsToWHReduce)
                        .collect(Collectors.toMap(Point::getFrenetx, point -> point));
            // 武汉至鄂州方向
            case ROAD_DIRECT_TO_EZ -> fiber.getLane() == 6 || fiber.getLane() ==7
                    // 车道为匝道,查找匝道道路静态表
                    ? pointsToEZzaRepository.findByFrenetxIsBetween(min, max).stream()
                        .map(MongoModelTransformUtil::pointsToEZzaReduce)
                        .collect(Collectors.toMap(Point::getFrenetx, point -> point))
                    // 车道为主干道,查找主干道道路静态表
                    : pointsToEZRepository.findByFrenetxIsBetween(min, max).stream()
                        .map(MongoModelTransformUtil::pointsToEZReduce)
                        .collect(Collectors.toMap(Point::getFrenetx, point -> point));
            default -> null;
        };
    }

    // 查找雷达数据UTM坐标在主干道道路静态表上的最近点
    public Point getRadarTrunkMatchPoint(Mercator model, int direction) {
        return switch(direction) {
            // 查找鄂州至武汉方向主干道道路静态表
            case ROAD_DIRECT_TO_WH -> pointsToWHRepository.findByMercatorNear(model.getX(), model.getY()).stream()
                    .findFirst()
                    .map(MongoModelTransformUtil::pointsToWHReduce)
                    .orElse(null);
            // 查找武汉至鄂州方向主干道道路静态表
            case ROAD_DIRECT_TO_EZ -> pointsToEZRepository.findByMercatorNear(model.getX(), model.getY()).stream()
                    .findFirst()
                    .map(MongoModelTransformUtil::pointsToEZReduce)
                    .orElse(null);
            default -> null;
        };
    }

    // 查找雷达数据UTM坐标在匝道道路静态表上的最近点
    public Point getRadarZaMatchPoint(Mercator model, int direction) {
        return switch(direction) {
            // 查找鄂州至武汉方向匝道道路静态表
            case ROAD_DIRECT_TO_WH -> pointsToWHzaRepository.findByMercatorNear(model.getX(), model.getY()).stream()
                    .findFirst()
                    .map(MongoModelTransformUtil::pointsToWHzaReduce)
                    .orElse(null);
            // 查找武汉至鄂州方向主干道道路静态表
            case ROAD_DIRECT_TO_EZ -> pointsToEZzaRepository.findByMercatorNear(model.getX(), model.getY()).stream()
                    .findFirst()
                    .map(MongoModelTransformUtil::pointsToEZzaReduce)
                    .orElse(null);
            default -> null;
        };
    }

    // 查找雷达数据在主干道道路静态表上,下个桩号记录点的数据(步长0.1)
//    public Point getRadarTrunkNextPoint(Double frenetX, int direction) {
//        return switch(direction) {
//            // 查询鄂州至武汉方向主干道道路静态表
//            case ROAD_DIRECT_TO_WH -> MongoModelTransformUtil.pointsToWHReduce(pointsToWHRepository.findTopByFrenetx(frenetX));
//            // 查询武汉至鄂州方向主干道道路静态表
//            case ROAD_DIRECT_TO_EZ -> MongoModelTransformUtil.pointsToEZReduce(pointsToEZRepository.findTopByFrenetx(frenetX));
//            default -> null;
//        };
//    }

    // 查找雷达数据在匝道道路静态表上,下个桩号记录点的数据(步长0.1)
    public Point getRadarZaNextPoint(Double frenetX, int direction) {
        return switch(direction) {
            // 查询鄂州至武汉方向匝道道路静态表
            case ROAD_DIRECT_TO_WH -> MongoModelTransformUtil.pointsToWHzaReduce(pointsToWHzaRepository.findTopByFrenetx(frenetX));
            // 查询武汉至鄂州方向匝道道路静态表
            case ROAD_DIRECT_TO_EZ -> MongoModelTransformUtil.pointsToEZzaReduce(pointsToEZzaRepository.findTopByFrenetx(frenetX));
            default -> null;
        };
    }

}
