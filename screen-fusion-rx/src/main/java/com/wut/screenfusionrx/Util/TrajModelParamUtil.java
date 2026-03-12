package com.wut.screenfusionrx.Util;

import com.wut.screencommonrx.Model.EventTypeModel;
import com.wut.screencommonrx.Model.TrajModel;
import com.wut.screencommonrx.Model.TrajModelLine;
import com.wut.screendbmysqlrx.Model.Traj;

public class TrajModelParamUtil {
    public static int getLineTrajModelSize(TrajModelLine trajModelLine) {
        return trajModelLine.getTrajModels().size();
    }

    public static int getLineLastTrajModelIndex(TrajModelLine trajModelLine) {
        return trajModelLine.getTrajModels().size() - 1;
    }

    public static TrajModel getLineLastTrajModel(TrajModelLine trajModelLine) {
        return trajModelLine.getTrajModels().get(
            getLineLastTrajModelIndex(trajModelLine)
        );
    }

    public static String getTrajIdToPicLicense(long id) {
        id %= 1000;
        if (id < 10) { return "鄂A1000" + id + "*"; }
        if (id < 100) { return "鄂A100" + id + "*"; }
        return "鄂A10" + id + "*";
    }

    public static String getEventMapKey(Traj traj, EventTypeModel entity) {
        return traj.getTrajId() + entity.suffix;
    }

}
