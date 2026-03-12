package com.wut.screencommonrx.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrajRecordModel {
    private int num;
    private double speed;
    private int type;
}
