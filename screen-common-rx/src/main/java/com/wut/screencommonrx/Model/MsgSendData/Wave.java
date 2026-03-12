package com.wut.screencommonrx.Model.MsgSendData;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Repository;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Repository
// 微波雷达数据模型
// 对应数据表:vehiclemodel3_{time}
public class Wave {
    private String code;
    private String ip;                  // 雷达IP
    private Integer id;                 // 目标ID(雷达或者光纤给车分配的编号)
    private Integer type;               // 目标类型
    private Integer length;             // 目标长度
    private Integer width;              // 目标宽度
    @JsonProperty("posX")
    private Integer posX;               // X坐标
    @JsonProperty("posY")
    private Integer posY;               // Y坐标
    @JsonProperty("speedX")
    private Double speedX;              // X轴速度
    @JsonProperty("speedY")
    private Double speedY;              // Y轴速度
    private Double speed;               // 车速
    private Integer acceleration;       // 目标方向加速度
    private Double longitude;           // 目标经度
    private Double latitude;            // 目标纬度
    @JsonProperty("mercatorX")
    private Double mercatorX;           // 目标平面坐标X
    @JsonProperty("mercatorY")
    private Double mercatorY;           // 目标平面坐标Y
    @JsonProperty("frenetX")
    private Double frenetX;             // 目标frenet坐标X
    @JsonProperty("frenetY")
    private Double frenetY;             // 目标frenet坐标Y
    @JsonProperty("headingAngle")
    private Double headingAngle;        // 目标航向角
    @JsonProperty("fiberX")
    private Integer fiberX;
    private Integer lane;
    @JsonProperty("frenetAngle")
    private Double frenetAngle;
    @JsonProperty("roadDirect")
    private Integer roadDirect;
    @JsonProperty("carId")
    private String carId;           // 车牌照信息
    @JsonProperty("timestamp")
    private Double timestamp;
    @JsonProperty("saveTimestamp")
    private String saveTimestamp;
    private String time;
    @JsonProperty("saveTime")
    private String saveTime;
    @JsonProperty("idNew")
    private Integer idNew;
    @JsonProperty("trajStatus")
    private Integer trajStatus;
}
