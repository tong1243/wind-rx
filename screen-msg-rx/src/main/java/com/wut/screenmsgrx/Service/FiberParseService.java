package com.wut.screenmsgrx.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wut.screencommonrx.Model.MsgSendDataModel;
import com.wut.screencommonrx.Model.VehicleModel;
import com.wut.screendbredisrx.Service.RedisModelDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.wut.screencommonrx.Static.FusionModuleStatic.DEFAULT_IP;
import static com.wut.screencommonrx.Static.FusionModuleStatic.FIBER_DEFAULT_IDDUP;
import static com.wut.screencommonrx.Static.FusionModuleStatic.MODEL_TYPE_FIBER;

@Component
public class FiberParseService {
    @Qualifier("msgTaskAsyncPool")
    private final Executor msgTaskAsyncPool;
    private final RedisModelDataService redisModelDataService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public FiberParseService(RedisModelDataService redisModelDataService, Executor msgTaskAsyncPool) {
        this.redisModelDataService = redisModelDataService;
        this.msgTaskAsyncPool = msgTaskAsyncPool;
    }

    public CompletableFuture<Void> collectFiberData(String fiberDataStr) {
        return CompletableFuture
                .supplyAsync(() -> parseVehicleModel(fiberDataStr), msgTaskAsyncPool)
                .thenCompose(vehicleModel -> {
                    if (vehicleModel == null) {
                        return CompletableFuture.completedFuture(null);
                    }
                    return redisModelDataService.storeFiberModelData(vehicleModel);
                });
    }

    private VehicleModel parseVehicleModel(String fiberDataStr) {
        try {
            MsgSendDataModel msgSendDataModel = objectMapper.readValue(fiberDataStr, MsgSendDataModel.class);
            return toVehicleModel(msgSendDataModel);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private VehicleModel toVehicleModel(MsgSendDataModel msgSendDataModel) {
        JsonNode dataNode = objectMapper.valueToTree(msgSendDataModel.getData());
        if (dataNode == null || dataNode.isNull() || dataNode.isMissingNode()) {
            return null;
        }

        long timestamp = parseLong(dataNode, "timestamp", msgSendDataModel.getTimestamp());
        int id = parseInt(dataNode, "id", 0);
        int direction = parseInt(dataNode, "direction", parseInt(dataNode, "roadDirect", 1));
        int lane = parseInt(dataNode, "Lane_ID", parseInt(dataNode, "laneId", parseInt(dataNode, "lane", 1)));
        int road = parseInt(dataNode, "road", 0);
        double distanceAlongRoad = parseDouble(dataNode, "distanceAlongRoad", parseDouble(dataNode, "frenetX", parseDouble(dataNode, "fiberX", 0.0)));
        int type = parseInt(dataNode, "type", MODEL_TYPE_FIBER);
        double speedKmh = parseDouble(dataNode, "speed", Double.NaN);
        double speedX = parseDouble(dataNode, "speedX", Double.isNaN(speedKmh) ? 0.0 : speedKmh / 3.6);
        double speedY = parseDouble(dataNode, "speedY", 0.0);
        double speed = parseDouble(dataNode, "speed", Math.abs(speedX));
        if (!Double.isNaN(speedKmh)) {
            speed = speedKmh / 3.6;
        }

        double yaw = parseDouble(dataNode, "yaw", parseDouble(dataNode, "headingAngle", 0.0));
        double headingAngle = Math.abs(yaw) <= (2 * Math.PI + 1e-6) ? Math.toDegrees(yaw) : yaw;
        double acceleration = parseDouble(dataNode, "acc", parseDouble(dataNode, "acceleration", 0.0));

        int model = parseInt(dataNode, "model", 1);
        double length = mapModelToLength(model);
        double width = mapModelToWidth(model);

        double longitude = parseDouble(dataNode, "longitude", 0.0);
        double latitude = parseDouble(dataNode, "latitude", 0.0);
        double height = parseDouble(dataNode, "height", 0.0);

        String carId = parseText(dataNode, "carId", String.valueOf(id));

        return new VehicleModel(
                "UC",
                DEFAULT_IP,
                id,
                FIBER_DEFAULT_IDDUP,
                type,
                length,
                width,
                distanceAlongRoad,
                0.0,
                Math.abs(speedX),
                Math.abs(speedY),
                Math.abs(speed),
                acceleration,
                longitude,
                latitude,
                0.0,
                0.0,
                distanceAlongRoad,
                0.0,
                headingAngle,
                (int) Math.round(distanceAlongRoad),
                road,
                lane,
                headingAngle,
                String.valueOf(direction),
                carId,
                timestamp,
                timestamp,
                height
        );
    }

    private double mapModelToLength(int model) {
        return switch (model) {
            case 2 -> 9.5;
            case 3 -> 13.0;
            default -> 4.8;
        };
    }

    private double mapModelToWidth(int model) {
        return switch (model) {
            case 2 -> 2.4;
            case 3 -> 2.6;
            default -> 1.9;
        };
    }

    private int parseInt(JsonNode node, String field, int defaultValue) {
        if (node == null) {
            return defaultValue;
        }
        JsonNode fieldNode = node.path(field);
        if (fieldNode.isMissingNode() || fieldNode.isNull()) {
            return defaultValue;
        }
        try {
            return (int) Double.parseDouble(fieldNode.asText());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private long parseLong(JsonNode node, String field, long defaultValue) {
        if (node == null) {
            return defaultValue;
        }
        JsonNode fieldNode = node.path(field);
        if (fieldNode.isMissingNode() || fieldNode.isNull()) {
            return defaultValue;
        }
        try {
            return (long) Double.parseDouble(fieldNode.asText());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private double parseDouble(JsonNode node, String field, double defaultValue) {
        if (node == null) {
            return defaultValue;
        }
        JsonNode fieldNode = node.path(field);
        if (fieldNode.isMissingNode() || fieldNode.isNull()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(fieldNode.asText());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String parseText(JsonNode node, String field, String defaultValue) {
        if (node == null) {
            return defaultValue;
        }
        JsonNode fieldNode = node.path(field);
        if (fieldNode.isMissingNode() || fieldNode.isNull()) {
            return defaultValue;
        }
        String value = fieldNode.asText();
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
