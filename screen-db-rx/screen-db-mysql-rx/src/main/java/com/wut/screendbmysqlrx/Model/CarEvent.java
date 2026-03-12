package com.wut.screendbmysqlrx.Model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
// 事件预警信息
// 对应表名:carevent_{time}
@TableName("carevent")
public class CarEvent {
    @TableId(type = IdType.ASSIGN_ID)
    private Long uuid;
    @TableField("startTimeStamp")
    @JsonProperty("startTimestamp")
    private Long startTimestamp;
    @TableField("endTimeStamp")
    @JsonProperty("endTimestamp")
    private Long endTimestamp;
    private String id;
    @TableField("startMileage")
    @JsonProperty("startMileage")
    private String startMileage;
    @TableField("endMileage")
    @JsonProperty("endMileage")
    private String endMileage;
    private Integer lane;
    @TableField("eventType")
    @JsonProperty("eventType")
    private Integer eventType;
    @TableField("trajId")
    @JsonProperty("trajId")
    private Long trajId;
    private Integer status;
    private Integer process;
    @TableField("queueLength")
    @JsonProperty("queueLength")
    private Double queueLength;
}
