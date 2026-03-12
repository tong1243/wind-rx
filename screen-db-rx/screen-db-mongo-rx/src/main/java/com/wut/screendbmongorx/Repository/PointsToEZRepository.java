package com.wut.screendbmongorx.Repository;

import com.wut.screendbmongorx.Document.PointsToEZ;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointsToEZRepository extends MongoRepository<PointsToEZ, String> {
    public PointsToEZ findTopByFrenetx(Double frenetx);
    public PointsToEZ findTopByXAndY(Double x, Double y);
    @Query("{ mercator: { $near: [?0, ?1], $maxDistance: 50 } }")
    public List<PointsToEZ> findByMercatorNear(Double x, Double y);
    public List<PointsToEZ> findByFrenetxIsBetween(Double min, Double max);

}
