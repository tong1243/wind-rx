package com.wut.screenfusionrx.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrajMatchMarkModel {
    private int radarId;        // 光纤数据匹配的雷达ID
    private boolean status;     // 上个时间戳的匹配状态
}
