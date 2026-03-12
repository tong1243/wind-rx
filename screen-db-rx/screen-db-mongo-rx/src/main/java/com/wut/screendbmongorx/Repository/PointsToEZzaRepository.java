package com.wut.screendbmongorx.Repository;

import com.wut.screendbmongorx.Document.PointsToEZza;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointsToEZzaRepository extends MongoRepository<PointsToEZza, String> {
    public PointsToEZza findTopByFrenetx(Double frenetx);
    public PointsToEZza findTopByXAndY(Double x, Double y);
    @Query("{ mercator: { $near: [?0, ?1], $maxDistance: 50 } }")
    public List<PointsToEZza> findByMercatorNear(Double x, Double y);
    public List<PointsToEZza> findByFrenetxIsBetween(Double min, Double max);
}
