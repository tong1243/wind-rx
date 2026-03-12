package com.wut.screendbmongorx.Repository;

import com.wut.screendbmongorx.Document.PointsToWHza;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointsToWHzaRepository extends MongoRepository<PointsToWHza, String> {
    public PointsToWHza findTopByFrenetx(Double frenetx);
    public PointsToWHza findTopByXAndY(Double x, Double y);
    @Query("{ mercator: { $near: [?0, ?1], $maxDistance: 50 } }")
    public List<PointsToWHza> findByMercatorNear(Double x, Double y);
    public List<PointsToWHza> findByFrenetxIsBetween(Double min, Double max);
}
