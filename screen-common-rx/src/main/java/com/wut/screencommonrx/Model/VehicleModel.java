package com.wut.screencommonrx.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleModel {
    private String code;
    private String ip;              // IP
    private Integer id;             // 原始ID
    private Integer idDup;          // 去重ID
    private Integer type;           // 目标类型
    private Double length;          // 目标长度
    private Double width;           // 目标宽度
    @JsonProperty("posX")
    private Double posX;            // X轴坐标
    @JsonProperty("posY")
    private Double posY;            // Y轴坐标
    @JsonProperty("speedX")
    private Double speedX;          // X轴速度
    @JsonProperty("speedY")
    private Double speedY;          // Y轴速度
    private Double speed;           // 速度
    private Double acceleration;    // 目标方向加速度
    private Double longitude;       // 目标经度
    private Double latitude;        // 目标纬度
    @JsonProperty("mercatorX")
    private Double mercatorX;       // X轴平面坐标
    @JsonProperty("mercatorY")
    private Double mercatorY;       // Y轴平面坐标
    @JsonProperty("frenetX")
    private Double frenetX;         // X轴frenet坐标
    @JsonProperty("frenetY")
    private Double frenetY;         // Y轴frenet坐标
    @JsonProperty("headingAngle")
    private Double headingAngle;    // 目标航向角
    @JsonProperty("fiberX")
    private Integer fiberX;
    private Integer lane;
    @JsonProperty("frenetAngle")
    private Double frenetAngle;
    @JsonProperty("roadDirect")
    private String roadDirect;
    @JsonProperty("carId")
    private String carId;           // 牌照信息
    private Long timestamp;         // 时间戳
    @JsonProperty("saveTimestamp")
    private Long saveTimestamp;     // 保存时间戳(计算时延)
    @JsonProperty("height")
    private Double height;
}
