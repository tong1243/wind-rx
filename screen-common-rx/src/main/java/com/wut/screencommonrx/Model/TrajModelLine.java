package com.wut.screencommonrx.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrajModelLine {
    private Integer state;
    @JsonProperty("emptyFrameNum")
    private Integer emptyFrameNum;
    @JsonProperty("carIdTimestamp")
    private Long carIdTimestamp;
    @JsonProperty("earlyWarningFrame")
    private Integer earlyWarningFrame;
    @JsonProperty("earlyWarningType")
    private Integer earlyWarningType;
    private Boolean linked;
    @JsonProperty("trajModels")
    private List<TrajModel> trajModels;
}
