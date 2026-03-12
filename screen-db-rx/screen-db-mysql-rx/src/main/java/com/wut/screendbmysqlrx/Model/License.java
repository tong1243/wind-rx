package com.wut.screendbmysqlrx.Model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("license")
public class License {
    @TableId(type = IdType.INPUT)
    private Integer lid;
    @TableField("deviceCode")
    private String deviceCode;
    @TableField("installPlace")
    private String installPlace;
    @TableField("roadDirect")
    private Integer roadDirect;
    private Double start;
    private Double end;
    @TableField("laneNumber")
    private Integer laneNumber;
    private Integer state;
}
