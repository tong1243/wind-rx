package com.wut.screenfusionrx.Service.TrajFusionSubService;

import com.wut.screencommonrx.Model.TrajModel;
import com.wut.screencommonrx.Model.TrajModelLine;
import com.wut.screencommonrx.Util.CollectionEmptyUtil;
import com.wut.screencommonrx.Util.DataParamParseUtil;
import com.wut.screenfusionrx.Context.TrajDataContext;
import com.wut.screenfusionrx.Util.TrajModelParamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static com.wut.screencommonrx.Static.FusionModuleStatic.*;

@Component
public class TrajModelLineConnectService {
    private final TrajDataContext trajDataContext;

    @Autowired
    public TrajModelLineConnectService(TrajDataContext trajDataContext) {
        this.trajDataContext = trajDataContext;
    }

    // 清洗后原始数据填充历史轨迹队列
    public void connect(List<TrajModel> flushModelList, long timestamp) {
        // 当前累计的车辆数信息
        AtomicInteger carNumCountToWH = trajDataContext.getCarNumCountToWH();
        AtomicInteger carNumCountToEZ = trajDataContext.getCarNumCountToEZ();
        // 寻找清洗后原始数据匹配的历史轨迹队列,并记录累计车辆数
        if (!CollectionEmptyUtil.forList(flushModelList)) {
            flushModelList.stream().forEach(model -> {
                model.setFrenetXPrediction(DataParamParseUtil.getNextFrenetXPrediction(model));
                findMatchTrajModelLine(model, carNumCountToWH, carNumCountToEZ);
            });
        }
        // 向轨迹数据上下文存储当前时间戳下的累计车辆数信息
        trajDataContext.pushNewRecordCarNum(timestamp);
    }

    // 寻找清洗后原始数据匹配的历史轨迹队列
    public void findMatchTrajModelLine(TrajModel model, AtomicInteger carNumCountToWH, AtomicInteger carNumCountToEZ) {
        // 精确匹配历史轨迹队列
        trajDataContext.getTrajModelLineList().stream().filter(trajModelLine -> trajModelLine.getState() == TRAJ_MODEL_LINE_STATUS_EMPTY
                // 只需要原始ID相同即可,雷达ID即使不同,也只说明参与了不同的多设备去重过程
                && Objects.equals(TrajModelParamUtil.getLineLastTrajModel(trajModelLine).getIdOri(), model.getIdOri())
        ).findAny().ifPresentOrElse(
                (modelLine) -> setPriorityTrajModelLineParam(model, modelLine),
                () -> {
                    // 未找到匹配的历史轨迹队列时
                    // -> 如果之前有对应的轨迹号,说明出现了超过范围的断帧现象,重新创建轨迹队列,沿用之前的轨迹号,也不记录累计车辆数
                    // -> 如果之前也没有对应的轨迹号,向轨迹队列的列表中创建新的轨迹队列,并新增对应方向上的累计车辆数
                    if (trajDataContext.getTrajRawIdRecordMap().containsKey(model.getRawId())) {
                        model.setTrajId(trajDataContext.getTrajRawIdRecordMap().get(model.getRawId()));
                        trajDataContext.pushNewTrajModelLineWithExistTrajId(model);
                    } else {
                        trajDataContext.pushNewTrajModelLine(model);
                        switch (model.getRoadDirect()) {
                            case ROAD_DIRECT_TO_WH -> carNumCountToWH.incrementAndGet();
                            case ROAD_DIRECT_TO_EZ -> carNumCountToEZ.incrementAndGet();
                        }
                    }
                }
        );
    }

    // 轨迹队列成功匹配时修改参数的通用方法
    public void setPriorityTrajModelLineParam(TrajModel trajModel, TrajModelLine trajModelLine) {
        TrajModel framTrajModel = TrajModelParamUtil.getLineLastTrajModel(trajModelLine);
        double accx = (trajModel.getSpeedX() - framTrajModel.getSpeedX()) * FRENETX_PREDICT_FACTOR;
        framTrajModel.setAccx(accx);
        // 沿用轨迹队列上一帧(必定为同ID数据)的轨迹号,牌照,牌照颜色等信息
        trajModel.setTrajId(framTrajModel.getTrajId());
        trajModel.setCarId(framTrajModel.getCarId());
        trajModel.setLicenseColor(framTrajModel.getLicenseColor());
        trajModelLine.setState(TRAJ_MODEL_LINE_STATUS_FINISH);
        trajModelLine.setEmptyFrameNum(0);
        trajModelLine.setLinked(true);
        trajModelLine.getTrajModels().add(trajModel);
    }

}
