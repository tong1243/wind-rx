package com.wut.screendbmongorx.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Point {
    private Double x;
    private Double y;
    private Double latitude;
    private Double longitude;
    private Double frenetx;
    private Double angle;
    private List<Double> mercator;
}
