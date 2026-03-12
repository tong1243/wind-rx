package com.wut.screencommonrx.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventTypeModel {
    public int value;
    public String suffix;
    public int time;
    public long timeout;
}
