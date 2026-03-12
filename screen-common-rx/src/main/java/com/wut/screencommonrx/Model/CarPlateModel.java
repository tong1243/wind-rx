package com.wut.screencommonrx.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarPlateModel {
    private String ip;                  // 相机IP
    @JsonProperty("picId")
    private String picId;               // 牌识流水号+相机编号+批次号+批次内序号
    @JsonProperty("gantryId")
    private String gantryId;            // 门牌序号
    @JsonProperty("cameraNum")
    private Integer cameraNum;          // 相机编号
    @JsonProperty("laneNum")
    private Integer laneNum;            // 物理车道编号
    @JsonProperty("vehicleId")
    private Integer vehicleId;          // 车辆编号
    @JsonProperty("picLicense")
    private String picLicense;          // 车辆牌照
    @JsonProperty("licenseColor")
    private Integer licenseColor;       // 车辆牌照颜色
    @JsonProperty("vehicleSpeed")
    private Double vehicleSpeed;        // 车辆速度
    private String start;               // 起始frenetX坐标
    private String end;                 // 结束frenetX坐标
    @JsonProperty("roadDirect")
    private String roadDirect;          // 道路幅向
    private String state;
    @JsonProperty("picTime")
    private String picTime;             // 抓拍时间
    @JsonProperty("saveTime")
    private String saveTime;            // 保存时间
    private Long timestamp;             // 时间戳
}
