package com.wut.screenfusionrx.Service.TrajFusionSubService;

import com.wut.screencommonrx.Model.TrajModel;
import com.wut.screencommonrx.Model.TrajModelLine;
import com.wut.screencommonrx.Util.DataParamParseUtil;
import com.wut.screencommonrx.Util.ModelTransformUtil;
import com.wut.screendbmysqlrx.Model.Traj;
import com.wut.screendbmysqlrx.Util.DbModelTransformUtil;
import com.wut.screenfusionrx.Context.TrajDataContext;
import com.wut.screenfusionrx.Util.TrajModelParamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.wut.screencommonrx.Static.FusionModuleStatic.*;

@Component
public class TrajModelLineCollectService {
    private final TrajDataContext trajDataContext;

    @Autowired
    public TrajModelLineCollectService(TrajDataContext trajDataContext) {
        this.trajDataContext = trajDataContext;
    }

    public List<Traj> collect(long timestamp) {
        disposeReadyToMoveData(timestamp);
        return disposeReadyToStoreData();
    }

    public void disposeReadyToMoveData(long timestamp) {
        List<TrajModelLine> readyToMoveList = new ArrayList<>();
        // 本轮时间戳轨迹队列标志位为0,说明没有匹配上任何的轨迹数据
        // 为轮空的轨迹队列补充一个预测帧,预测帧调用上一个轨迹的预测桩号作为当前桩号
        trajDataContext.getTrajModelLineList().stream().filter(trajModelLine -> {
            return trajModelLine.getState() == TRAJ_MODEL_LINE_STATUS_EMPTY;
        }).forEach(trajModelLine -> {
            int emptyFrameNum = trajModelLine.getEmptyFrameNum() + 1;
            // 添加新的预测帧后,预测帧总个数为4,说明该轨迹是非噪点的正常轨迹,在上个时间戳已经保存结束,删除该轨迹队列
            // 添加新的预测帧后,预测帧总个数为3,且队列中的总帧个数为4,则需要进一步判断
            // -> 如果该轨迹队列Linked标志位为false,说明轨迹为噪点数据,同样删除该轨迹队列
            // -> 如果该轨迹队列Linked标志位为true,说明轨迹不为噪点数据,首个数据应当作为正常数据保存,下个时间戳再进行删除
            if ((emptyFrameNum == TRAJ_MODEL_LINE_CAPACITY_LIMIT) ||
                (emptyFrameNum == TRAJ_MODEL_LINE_EMPTY_LIMIT
                // 此处还未真正加入预测帧,因此加入后轨迹队列满4帧的情况下,加入前的总帧数应该有3帧
                    && TrajModelParamUtil.getLineTrajModelSize(trajModelLine) == TRAJ_MODEL_LINE_EMPTY_LIMIT
                    && !trajModelLine.getLinked()
            )) {
                readyToMoveList.add(trajModelLine);
            } else {
                TrajModel readyToAddTrajModel = ModelTransformUtil.trajModelToFrame(TrajModelParamUtil.getLineLastTrajModel(trajModelLine), timestamp);
                readyToAddTrajModel.setFrenetXPrediction(DataParamParseUtil.getNextFrenetXPrediction(readyToAddTrajModel));
                trajModelLine.getTrajModels().add(readyToAddTrajModel);
                // 在某条轨迹的断点/结束阶段同样会遇到空帧,更改状态位以输出剩余的数据
                trajModelLine.setState(TRAJ_MODEL_LINE_STATUS_FINISH);
                trajModelLine.setEmptyFrameNum(emptyFrameNum);
            }
        });
        trajDataContext.getTrajModelLineList().removeAll(readyToMoveList);
    }

    // 提取满容量轨迹队列的首个轨迹帧
    public List<Traj> disposeReadyToStoreData() {
        List<Traj> readyToStoreList = new ArrayList<>();
        trajDataContext.getTrajModelLineList().stream().filter(trajModelLine -> {
            return trajModelLine.getState() == TRAJ_MODEL_LINE_STATUS_FINISH
                    && TrajModelParamUtil.getLineTrajModelSize(trajModelLine) == TRAJ_MODEL_LINE_CAPACITY_LIMIT;
        }).forEach(trajModelLine -> {
            // 对于正常满4个轨迹帧的轨迹队列,取出第一个有效帧(当前时间戳-600)添加进待保存列表中
            readyToStoreList.add(DbModelTransformUtil.trajModelToTraj(trajModelLine.getTrajModels().remove(0)));
        });
        // 处理完成后,重置轨迹队列的标志位
        trajDataContext.resetTrajModelLineStatus();
        return readyToStoreList;
    }

}
