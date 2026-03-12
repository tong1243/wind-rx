package com.wut.screendbmongorx.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "vehicle_model_docu")
public class VehicleModelDocu {
    private String ip;              // 雷达IP
    private Integer markId;         // 目标ID
    private Integer markIdDup;      // 去重ID
    private Integer type;           // 目标类型
    private Double length;          // 目标长度
    private Double width;           // 目标宽度
    private Double posX;            // X轴坐标
    private Double posY;            // Y轴坐标
    private Double speedX;          // X轴速度
    private Double speedY;          // Y轴速度
    private Double speed;           // 速度
    private Double acceleration;    // 目标方向加速度
    private Double longitude;       // 目标经度
    private Double latitude;        // 目标纬度
    private Double mercatorX;       // X轴平面坐标
    private Double mercatorY;       // Y轴平面坐标
    private Double frenetX;         // X轴frenet坐标
    private Double frenetY;         // Y轴frenet坐标
    private Double headingAngle;    // 目标航向角
    private Integer lane;
    private Double frenetAngle;
    private Integer roadDirect;
    private String carId;           // 牌照信息
    private Long timestamp;         // 时间戳
    private Long saveTimestamp;     // 保存时间戳(计算时延)
}
