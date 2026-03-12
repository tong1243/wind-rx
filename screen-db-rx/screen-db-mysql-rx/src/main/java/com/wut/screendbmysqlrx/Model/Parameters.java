package com.wut.screendbmysqlrx.Model;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Parameters {
    @TableField("timeStamp")
    private Long timeStamp;
    @TableField("time")
    private Integer time;
    @TableField("road_id")
    private Integer roadId;
    @TableField("stream")
    private Double stream;
    @TableField("density")
    private Double density;
    @TableField("speed")
    private Double speed;
    @TableField("travel_time")
    private Double travelTime;
    @TableField("delay")
    private Double delay;
    @TableField("state")
    private Integer state;
    @TableField("speed_limit")
    private Double speedLimit;
    @TableField("upSpeed")
    private Double upSpeed;
    @TableField("downSpeed")
    private Double downSpeed;
    @TableField("upDensity")
    private Double upDensity;
    @TableField("downDensity")
    private Double downDensity;
    @TableField("rampStream")
    private Double rampStream;
    @TableField("carCount")
    private Integer carCount;
}
