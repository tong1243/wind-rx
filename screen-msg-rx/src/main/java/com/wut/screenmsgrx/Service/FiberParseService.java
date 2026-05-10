package com.wut.screenmsgrx.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wut.screencommonrx.Model.MsgSendDataModel;
import com.wut.screencommonrx.Model.VehicleModel;
import com.wut.screendbmysqlrx.Model.TravelReservation;
import com.wut.screendbmysqlrx.Model.UcCarRealTime;
import com.wut.screendbmysqlrx.Model.UcCarRealTimeCurrent;
import com.wut.screendbmysqlrx.Service.TravelReservationService;
import com.wut.screendbmysqlrx.Service.UcCarRealTimeCurrentService;
import com.wut.screendbmysqlrx.Service.UcCarRealTimeService;
import com.wut.screendbredisrx.Service.RedisModelDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.wut.screencommonrx.Static.FusionModuleStatic.DEFAULT_IP;
import static com.wut.screencommonrx.Static.FusionModuleStatic.FIBER_DEFAULT_IDDUP;
import static com.wut.screencommonrx.Static.FusionModuleStatic.MODEL_TYPE_FIBER;

@Component
public class FiberParseService {
    private static final Logger log = LoggerFactory.getLogger(FiberParseService.class);
    private static final long TRAVEL_RESERVATION_CACHE_TTL_MS = 1000L;
    private static final long UC_REALTIME_BIND_TTL_SECONDS = 2 * 60 * 60L;

    @Qualifier("msgTaskAsyncPool")
    private final Executor msgTaskAsyncPool;
    private final RedisModelDataService redisModelDataService;
    private final UcCarRealTimeService ucCarRealTimeService;
    private final UcCarRealTimeCurrentService ucCarRealTimeCurrentService;
    private final TravelReservationService travelReservationService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile TravelReservation latestReservationCache;
    private volatile long latestReservationCacheTimestamp;

    @Autowired
    public FiberParseService(RedisModelDataService redisModelDataService, Executor msgTaskAsyncPool,
                             UcCarRealTimeService ucCarRealTimeService,
                             UcCarRealTimeCurrentService ucCarRealTimeCurrentService,
                             TravelReservationService travelReservationService) {
        this.redisModelDataService = redisModelDataService;
        this.msgTaskAsyncPool = msgTaskAsyncPool;
        this.ucCarRealTimeService = ucCarRealTimeService;
        this.ucCarRealTimeCurrentService = ucCarRealTimeCurrentService;
        this.travelReservationService = travelReservationService;
    }

    FiberParseService(RedisModelDataService redisModelDataService, Executor msgTaskAsyncPool) {
        this(redisModelDataService, msgTaskAsyncPool, null, null, null);
    }

    public CompletableFuture<Void> collectFiberData(String fiberDataStr) {
        return CompletableFuture
                .supplyAsync(() -> parsePayload(fiberDataStr), msgTaskAsyncPool)
                .thenCompose(parseResult -> {
                    if (parseResult == null || parseResult.vehicleModel() == null) {
                        return CompletableFuture.completedFuture(null);
                    }
                    CompletableFuture<Void> storeFiberTask = redisModelDataService.storeFiberModelData(parseResult.vehicleModel());
                    if (parseResult.ucCarRealTime() == null) {
                        return storeFiberTask;
                    }
                    if (ucCarRealTimeService == null && ucCarRealTimeCurrentService == null) {
                        return storeFiberTask;
                    }
                    return storeFiberTask.thenRunAsync(() -> {
                        persistUcCarRealTime(parseResult.vehicleModel(), parseResult.ucCarRealTime());
                    }, msgTaskAsyncPool);
                });
    }

    private void persistUcCarRealTime(VehicleModel vehicleModel, UcCarRealTime ucCarRealTime) {
        if (ucCarRealTimeService != null) {
            try {
                ucCarRealTimeService.storeOne(ucCarRealTime);
            } catch (Exception e) {
                log.error("Store uc_car_real_time failed, id={}, carLicense={}",
                        vehicleModel.getId(), ucCarRealTime.getCarLicense(), e);
            }
        }

        if (ucCarRealTimeCurrentService != null) {
            try {
                ucCarRealTimeCurrentService.upsertOne(toCurrentRecord(ucCarRealTime));
            } catch (Exception e) {
                log.error("Upsert uc_car_real_time_current failed, id={}, carLicense={}",
                        vehicleModel.getId(), ucCarRealTime.getCarLicense(), e);
            }
        }
    }

    private UcCarRealTimeCurrent toCurrentRecord(UcCarRealTime ucCarRealTime) {
        return new UcCarRealTimeCurrent(
                null,
                ucCarRealTime.getUserPhone(),
                ucCarRealTime.getCarLicense(),
                ucCarRealTime.getCurrentPile(),
                ucCarRealTime.getRealSpeed(),
                ucCarRealTime.getDirection(),
                ucCarRealTime.getDrivingDirection(),
                ucCarRealTime.getLaneNumber(),
                ucCarRealTime.getRoad(),
                ucCarRealTime.getReportTime()
        );
    }

    private ParseResult parsePayload(String fiberDataStr) {
        try {
            MsgSendDataModel msgSendDataModel = objectMapper.readValue(fiberDataStr, MsgSendDataModel.class);
            return toParseResult(msgSendDataModel);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private ParseResult toParseResult(MsgSendDataModel msgSendDataModel) {
        JsonNode dataNode = objectMapper.valueToTree(msgSendDataModel.getData());
        if (dataNode == null || dataNode.isNull() || dataNode.isMissingNode()) {
            return null;
        }
        VehicleModel vehicleModel = toVehicleModel(msgSendDataModel, dataNode);
        UcCarRealTime ucCarRealTime = toUcCarRealTime(dataNode, vehicleModel);
        return new ParseResult(vehicleModel, ucCarRealTime);
    }

    private VehicleModel toVehicleModel(MsgSendDataModel msgSendDataModel, JsonNode dataNode) {

        long timestamp = parseLong(dataNode, "timestamp", msgSendDataModel.getTimestamp());
        int id = parseInt(dataNode, "id", 0);
        int direction = parseInt(dataNode, "direction", parseInt(dataNode, "roadDirect", 1));
        int lane = parseInt(dataNode, "Lane_ID", parseInt(dataNode, "laneId", parseInt(dataNode, "lane", 1)));
        int road = parseInt(dataNode, "road", 0);
        double distanceAlongRoad = parseDouble(dataNode, "distanceAlongRoad", parseDouble(dataNode, "frenetX", parseDouble(dataNode, "fiberX", 0.0)));
        int type = parseInt(dataNode, "type", MODEL_TYPE_FIBER);
        double sourceSpeed = parseDouble(dataNode, "speed", parseDouble(dataNode, "speedX", 0.0));
        double speedX = sourceSpeed;
        double speedY = parseDouble(dataNode, "speedY", 0.0);
        double speed = sourceSpeed;

        double yaw = parseDouble(dataNode, "yaw", parseDouble(dataNode, "headingAngle", 0.0));
        double headingAngle = parseDouble(dataNode, "headingAngle", 0.0);
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
                speedX,
                Math.abs(speedY),
                speed,
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

    private UcCarRealTime toUcCarRealTime(JsonNode dataNode, VehicleModel vehicleModel) {
        if (vehicleModel == null || vehicleModel.getType() == null || vehicleModel.getType() != 1) {
            return null;
        }

        TravelReservation latestReservation = getLatestTravelReservation();
        String reservedPhone = normalizePhone(latestReservation == null ? "" : latestReservation.getUserPhone());
        String reservedCarLicense = truncate(firstNonBlank(latestReservation == null ? "" : latestReservation.getCarLicense()), 20);

        String upstreamCarId = normalizeUpstreamCarId(firstNonBlank(
                parseText(dataNode, "carId", ""),
                vehicleModel.getCarId() == null ? "" : vehicleModel.getCarId(),
                vehicleModel.getId() == null ? "" : String.valueOf(vehicleModel.getId())
        ));

        RedisModelDataService.UcRealtimeCarBinding binding = redisModelDataService.getUcRealtimeCarBinding(upstreamCarId);
        if (binding == null && !upstreamCarId.isBlank() && !reservedPhone.isBlank() && !reservedCarLicense.isBlank()) {
            redisModelDataService.bindUcRealtimeCar(upstreamCarId, reservedPhone, reservedCarLicense, UC_REALTIME_BIND_TTL_SECONDS);
            binding = redisModelDataService.getUcRealtimeCarBinding(upstreamCarId);
        }
        if (binding == null) {
            log.warn("skip uc_car_real_time store due to missing plate binding, id={}, upstreamCarId={}", vehicleModel.getId(), upstreamCarId);
            return null;
        }

        String userPhone = normalizePhone(binding.userPhone());
        String carLicense = truncate(binding.carLicense(), 20);
        if (userPhone.isBlank() || carLicense.isBlank()) {
            log.warn("skip uc_car_real_time store due to invalid plate binding, id={}, upstreamCarId={}", vehicleModel.getId(), upstreamCarId);
            return null;
        }

        String currentPile = firstNonBlank(
                parseText(dataNode, "currentPile", ""),
                parseText(dataNode, "current_pile", ""),
                parseText(dataNode, "pile", ""),
                parseText(dataNode, "pileNo", ""),
                parseText(dataNode, "stakeNo", ""),
                formatPile(vehicleModel.getFrenetX())
        );

        String drivingDirection = resolveDrivingDirection(dataNode, vehicleModel.getRoadDirect());
        if (drivingDirection == null) {
            drivingDirection = "hamimi_to_tuyugou";
            log.warn("driving direction invalid, fallback direction={}, id={}, roadDirect={}",
                    drivingDirection, vehicleModel.getId(), vehicleModel.getRoadDirect());
        }
        Integer direction = normalizeDirection(parseInt(dataNode, "direction",
                parseInt(dataNode, "roadDirect",
                        parseInt(vehicleModel.getRoadDirect(), 0))));
        if (direction == null) {
            direction = toDirectionCode(drivingDirection);
        }

        int laneNumber = normalizeLane(parseInt(dataNode, "Lane_ID",
                parseInt(dataNode, "laneId",
                        parseInt(dataNode, "lane", vehicleModel.getLane() == null ? 1 : vehicleModel.getLane()))));
        Integer road = normalizeRoad(parseInt(dataNode, "road", vehicleModel.getRoad() == null ? 0 : vehicleModel.getRoad()));
        double rawSpeed = parseDouble(dataNode, "speed", vehicleModel.getSpeed() == null ? 0.0 : vehicleModel.getSpeed());
        int realSpeed = normalizeSpeed(rawSpeed);
        LocalDateTime reportTime = resolveReportTime(dataNode, vehicleModel);

        return new UcCarRealTime(
                null,
                userPhone,
                carLicense,
                currentPile,
                realSpeed,
                direction,
                drivingDirection,
                laneNumber,
                road,
                reportTime
        );
    }

    private LocalDateTime resolveReportTime(JsonNode dataNode, VehicleModel vehicleModel) {
        long ts = parseLong(dataNode, "timestamp", 0L);
        if (ts <= 0L) {
            ts = parseLong(dataNode, "timeStamp", 0L);
        }
        if (ts <= 0L && vehicleModel != null && vehicleModel.getTimestamp() != null) {
            ts = vehicleModel.getTimestamp();
        }
        if (ts <= 0L) {
            return LocalDateTime.now();
        }

        // UDP timestamp can be seconds(10 digits) or milliseconds(13 digits).
        long epochMilli = ts < 100_000_000_000L ? ts * 1000L : ts;
        try {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    private String resolveDrivingDirection(JsonNode dataNode, String roadDirect) {
        String explicitDirection = firstNonBlank(
                parseText(dataNode, "driving_direction", ""),
                parseText(dataNode, "drivingDirection", "")
        );
        if ("hamimi_to_tuyugou".equalsIgnoreCase(explicitDirection)) {
            return "hamimi_to_tuyugou";
        }
        if ("tuyugou_to_hamimi".equalsIgnoreCase(explicitDirection)) {
            return "tuyugou_to_hamimi";
        }

        int direction = parseInt(dataNode, "direction",
                parseInt(dataNode, "roadDirect",
                        parseInt(roadDirect, 0)));
        if (direction == 1) {
            return "hamimi_to_tuyugou";
        }
        if (direction == 2) {
            return "tuyugou_to_hamimi";
        }
        return null;
    }

    private String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return "";
        }
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() <= 11) {
            return digits;
        }
        return digits.substring(digits.length() - 11);
    }

    private String normalizeUpstreamCarId(String upstreamCarId) {
        if (upstreamCarId == null || upstreamCarId.isBlank()) {
            return "";
        }
        String normalized = upstreamCarId.trim();
        if ("0".equals(normalized)) {
            return "";
        }
        return normalized;
    }

    private String buildFallbackPhone(Integer id) {
        String digits = id == null ? "0" : String.valueOf(Math.abs(id));
        if (digits.length() >= 11) {
            return digits.substring(digits.length() - 11);
        }
        return "0".repeat(11 - digits.length()) + digits;
    }

    private TravelReservation getLatestTravelReservation() {
        if (travelReservationService == null) {
            return null;
        }
        long now = System.currentTimeMillis();
        TravelReservation cached = latestReservationCache;
        if (cached != null && now - latestReservationCacheTimestamp <= TRAVEL_RESERVATION_CACHE_TTL_MS) {
            return cached;
        }

        synchronized (this) {
            now = System.currentTimeMillis();
            cached = latestReservationCache;
            if (cached != null && now - latestReservationCacheTimestamp <= TRAVEL_RESERVATION_CACHE_TTL_MS) {
                return cached;
            }
            try {
                TravelReservation latestReservation = travelReservationService.getLatestReservation();
                if (latestReservation != null) {
                    latestReservationCache = latestReservation;
                    latestReservationCacheTimestamp = now;
                }
                return latestReservation;
            } catch (Exception e) {
                log.warn("query latest travel reservation failed", e);
                return cached;
            }
        }
    }

    private String truncate(String value, int maxLen) {
        if (value == null) {
            return "";
        }
        if (value.length() <= maxLen) {
            return value;
        }
        return value.substring(0, maxLen);
    }

    private int normalizeSpeed(Double speed) {
        if (speed == null || speed < 0) {
            return 0;
        }
        int speedValue = (int) Math.round(speed);
        return Math.max(0, Math.min(speedValue, 255));
    }

    private int normalizeLane(int laneNumber) {
        if (laneNumber < 1) {
            return 1;
        }
        return Math.min(laneNumber, 4);
    }

    private Integer normalizeDirection(int direction) {
        if (direction == 1 || direction == 2) {
            return direction;
        }
        return null;
    }

    private Integer toDirectionCode(String drivingDirection) {
        if ("hamimi_to_tuyugou".equalsIgnoreCase(drivingDirection)) {
            return 1;
        }
        if ("tuyugou_to_hamimi".equalsIgnoreCase(drivingDirection)) {
            return 2;
        }
        return null;
    }

    private Integer normalizeRoad(int road) {
        if (road <= 0) {
            return null;
        }
        return road;
    }

    private String formatPile(Double distanceMeter) {
        if (distanceMeter == null || distanceMeter < 0) {
            return "k0+000";
        }
        long meter = Math.round(distanceMeter);
        long kilo = meter / 1000L;
        long rest = meter % 1000L;
        return "k" + kilo + "+" + String.format("%03d", rest);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
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
        return parseInt(fieldNode.asText(), defaultValue);
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return (int) Double.parseDouble(value);
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

    private record ParseResult(VehicleModel vehicleModel, UcCarRealTime ucCarRealTime) {
    }
}
