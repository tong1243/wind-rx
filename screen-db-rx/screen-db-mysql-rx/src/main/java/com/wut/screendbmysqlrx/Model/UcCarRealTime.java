package com.wut.screendbmysqlrx.Model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("uc_car_real_time")
public class UcCarRealTime {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_phone")
    private String userPhone;

    @TableField("car_license")
    private String carLicense;

    @TableField("current_pile")
    private String currentPile;

    @TableField("real_speed")
    private Integer realSpeed;

    @TableField("driving_direction")
    private String drivingDirection;

    @TableField("lane_number")
    private Integer laneNumber;

    @TableField("report_time")
    private LocalDateTime reportTime;
}
