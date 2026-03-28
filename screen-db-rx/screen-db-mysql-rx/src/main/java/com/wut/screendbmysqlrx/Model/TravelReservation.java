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
@TableName("travel_reservation")
public class TravelReservation {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_phone")
    private String userPhone;

    @TableField("car_license")
    private String carLicense;
}
