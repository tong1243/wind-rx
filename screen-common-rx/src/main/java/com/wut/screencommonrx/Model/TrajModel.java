package com.wut.screencommonrx.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrajModel {
    private Integer type;
    @JsonProperty("carType")
    private Integer carType;
    @JsonProperty("trajId")
    private Long trajId;
    @JsonProperty("idOri")
    private Integer idOri;
    @JsonProperty("idDup")
    private Integer idDup;
    @JsonProperty("frenetX")
    private Double frenetX;
    @JsonProperty("frenetY")
    private Double frenetY;
    @JsonProperty("speedX")
    private Double speedX;
    @JsonProperty("speedY")
    private Double speedY;
    private Double accx;
    @JsonProperty("roadDirect")
    private Integer roadDirect;
    @JsonProperty("carId")
    private String carId;
    @JsonProperty("licenseColor")
    private Integer licenseColor;
    @JsonProperty("mercatorX")
    private Double mercatorX;
    @JsonProperty("mercatorY")
    private Double mercatorY;
    private Double longitude;
    private Double latitude;
    @JsonProperty("headingAngle")
    private Double headingAngle;
    @JsonProperty("frenetXPrediction")
    private Double frenetXPrediction;
    @JsonProperty("frenetYPrediction")
    private Double frenetYPrediction;
    @JsonProperty("road")
    private Integer road;
    private Integer lane;
    @JsonProperty("rawId")
    private Integer rawId;
    private Long timestamp;
    @JsonProperty("height")
    private Double height;
}
