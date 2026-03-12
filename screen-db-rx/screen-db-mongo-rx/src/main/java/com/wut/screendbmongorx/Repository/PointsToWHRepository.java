package com.wut.screendbmongorx.Repository;

import com.wut.screendbmongorx.Document.PointsToWH;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointsToWHRepository extends MongoRepository<PointsToWH, String> {
    public PointsToWH findTopByFrenetx(Double frenetx);
    public PointsToWH findTopByXAndY(Double x, Double y);
    @Query("{ mercator: { $near: [?0, ?1], $maxDistance: 50 } }")
    public List<PointsToWH> findByMercatorNear(Double x, Double y);
    public List<PointsToWH> findByFrenetxIsBetween(Double min, Double max);
}
