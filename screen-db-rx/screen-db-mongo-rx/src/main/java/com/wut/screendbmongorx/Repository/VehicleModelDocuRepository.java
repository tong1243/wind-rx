package com.wut.screendbmongorx.Repository;

import com.wut.screendbmongorx.Document.VehicleModelDocu;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleModelDocuRepository extends MongoRepository<VehicleModelDocu, String> {
}
