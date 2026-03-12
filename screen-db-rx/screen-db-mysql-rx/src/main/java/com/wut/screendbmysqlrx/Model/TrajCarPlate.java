package com.wut.screendbmysqlrx.Model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("trajacarplate")
public class TrajCarPlate {
    @TableField("trajId")
    private Long trajId;
    @TableField("picLicense")
    private String picLicense;
    @TableField("saveTime")
    private Long saveTime;
    @TableField("roadDirect")
    private Integer roadDirect;
    private Long num;
}
