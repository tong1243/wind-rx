package com.wut.screenfusionrx.Model;

import com.google.common.collect.Range;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RotationMatrixModel {
    private double centerX;
    private double centerY;
    private double offsetX;
    private double offsetY;
    private int roadDirection;
    private int deviceDirection;
    private int status;
    private int sid;
    private Range<Double> range;
    private double cosAngle;
    private double sinAngle;
    private int suffix;
}
