package com.wut.screenmsgrx.Service;

import com.wut.screencommonrx.Model.VehicleModel;
import com.wut.screendbredisrx.Service.RedisModelDataService;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.wut.screencommonrx.Static.FusionModuleStatic.MODEL_TYPE_FIBER;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FiberParseServiceTest {
    private static class RecordingRedisModelDataService extends RedisModelDataService {
        private VehicleModel captured;

        RecordingRedisModelDataService() {
            super(null, Runnable::run);
        }

        @Override
        public CompletableFuture<Void> storeFiberModelData(VehicleModel model) {
            captured = model;
            return CompletableFuture.completedFuture(null);
        }
    }

    @Test
    void shouldConvertMinimalFieldsToVehicleModel() {
        RecordingRedisModelDataService redisModelDataService = new RecordingRedisModelDataService();
        Executor directExecutor = Runnable::run;

        FiberParseService service = new FiberParseService(redisModelDataService, directExecutor);
        String payload = """
                {
                  "timestamp": 1773910148000,
                  "data": {
                    "id": 664607,
                    "model": 3,
                    "direction": 1,
                    "longitude": 91.5730032,
                    "latitude": 43.3741122,
                    "height": 1074.577,
                    "speed": 106.1,
                    "acc": 0.138,
                    "yaw": 1.637,
                    "road": 112,
                    "Lane_ID": 4,
                    "distanceAlongRoad": 5437.222
                  }
                }
                """;

        service.collectFiberData(payload).join();

        VehicleModel model = redisModelDataService.captured;

        assertEquals(664607, model.getId());
        assertEquals(MODEL_TYPE_FIBER, model.getType());
        assertEquals(13.0, model.getLength(), 1e-6);
        assertEquals(2.6, model.getWidth(), 1e-6);
        assertEquals(5437.222, model.getFrenetX(), 1e-6);
        assertEquals(5437, model.getFiberX());
        assertEquals(112, model.getRoad());
        assertEquals(4, model.getLane());
        assertEquals("1", model.getRoadDirect());
        assertEquals(106.1 / 3.6, model.getSpeed(), 1e-6);
        assertEquals(106.1 / 3.6, model.getSpeedX(), 1e-6);
        assertEquals(Math.toDegrees(1.637), model.getHeadingAngle(), 1e-6);
        assertEquals(0.138, model.getAcceleration(), 1e-6);
        assertEquals(91.5730032, model.getLongitude(), 1e-6);
        assertEquals(43.3741122, model.getLatitude(), 1e-6);
        assertEquals(1074.577, model.getHeight(), 1e-6);
        assertEquals(1773910148000L, model.getTimestamp());
    }
}
