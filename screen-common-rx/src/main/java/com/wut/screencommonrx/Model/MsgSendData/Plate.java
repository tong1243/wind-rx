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
// 相机牌照数据模型
// 对应数据表:carplatev3model_{time}
public class Plate {
    private Integer id;
    private String ip;
    @JsonProperty("picId")
    private String picId;
    @JsonProperty("gantryId")
    private String gantryId;        // 门牌序号
    @JsonProperty("cameraNum")
    private Integer cameraNum;      // 相机编号
    @JsonProperty("laneNum")
    private Integer laneNum;        // 物理车道编码(行驶方向由内向外顺序递增,跨多车道时组合编号)
    @JsonProperty("vehicleId")
    private Integer vehicleId;      // 车辆编号
    @JsonProperty("picLicense")
    private String picLicense;      // 车牌号码
    @JsonProperty("licenseColor")
    private Integer licenseColor;   // 车牌颜色
    @JsonProperty("vehSpeed")
    private Integer vehSpeed;       // 车辆速度
    private String start;
    private String end;
    @JsonProperty("roadDirect")
    private String roadDirect;
    private String state;
    @JsonProperty("picTime")
    private String picTime;             // 抓拍时间
    @JsonProperty("saveTime")
    private String saveTime;
    @JsonProperty("globalTimestamp")
    private Double globalTimestamp;     // 检测时间(unix时间戳,距离1970年时间)
    @JsonProperty("saveTimestamp")
    private Double saveTimestamp;
}
