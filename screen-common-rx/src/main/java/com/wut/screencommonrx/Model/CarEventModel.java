package com.wut.screencommonrx.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarEventModel {
    @JsonProperty("trajId")
    private long trajId;
    @JsonProperty("eventType")
    private int eventType;
    @JsonProperty("eventTime")
    private int eventTime;
    @JsonProperty("startTimestamp")
    private long startTimestamp;
    @JsonProperty("endTimestamp")
    private long endTimestamp;
    @JsonProperty("picLicense")
    private String picLicense;
    @JsonProperty("lane")
    private int Lane;
    @JsonProperty("startFrenetX")
    private double startFrenetX;
    @JsonProperty("endFrenetX")
    private double endFrenetX;
    @JsonProperty("isStored")
    private boolean isStored;
    @JsonProperty("isChanged")
    private boolean isChanged;
    @JsonProperty("queueLength")
    private double queueLength;
    @JsonProperty("speedList")
    private List<Double> speedList;
}
