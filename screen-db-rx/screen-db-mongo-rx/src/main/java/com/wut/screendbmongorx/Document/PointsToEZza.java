package com.wut.screendbmongorx.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "points_toez_za")
@CompoundIndex(
        name = "mercator_asc",
        def = "{ 'x': 1, 'y': 1 }",
        unique = true
)
public class PointsToEZza {
    private Double x;
    private Double y;
    private Double latitude;
    private Double longitude;
    @Indexed(
            name = "frenet_asc",
            unique = true
    )
    private Double frenetx;
    private Double angle;
    @Indexed(name = "mercator_2d")
    private List<Double> mercator;
}
