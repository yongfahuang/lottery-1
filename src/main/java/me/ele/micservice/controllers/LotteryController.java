package me.ele.micservice.controllers;

import com.jfinal.aop.Before;
import com.jfinal.ext.interceptor.POST;
import me.ele.micservice.models.ClazMod;
import me.ele.micservice.models.lottery.AttendLog;
import me.ele.micservice.models.lottery.HonourRoll;
import org.apache.activemq.management.PollCountStatisticImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 抽奖业务
 * <p>
 * Created by jiangbo.cheng on 2016-08-08 16:17.
 */
public class LotteryController extends AuthController {
    private static Logger logger = LoggerFactory.getLogger(LotteryController.class);

    private static Integer INTERFACE_NORMAL = 0;//接口正常
    private static String LOTTERY_THANKYOU = "0";//谢谢参与

    /**
     * 检查是否抽过奖
     * <p>
     * Created by jiangbo.cheng on 2016-08-08 17:00.
     */
    @Before(POST.class)
    public void checkAttend(){
        ClazMod.Request request = getParaObject(ClazMod.Request.class);
        String account = getIdentity(request.getToken());
        if(AttendLog.me.checkByAccount(account)){
            renderJson(new ClazMod.LotteryResponse(INTERFACE_NORMAL, 1, "抽过奖"));
        }else{
            renderJson(new ClazMod.LotteryResponse(INTERFACE_NORMAL, 0, "未抽过奖"));
        }

    }

    /**
     * 查看我的中奖榜单
     */
    @Before(POST.class)
    public void showMyHonourRoll(){

        ClazMod.Request request = getParaObject(ClazMod.Request.class);
        HonourRoll honourRoll = HonourRoll.me.queryByAccount(getIdentity(request.getToken()));
        if(honourRoll !=null){
            renderJson(new ClazMod.LotteryResponse(INTERFACE_NORMAL, honourRoll.getStr("prize_code"), "中奖编号:"+honourRoll.getStr("lucky_no")));
        }else{
            renderJson(new ClazMod.LotteryResponse(INTERFACE_NORMAL, LOTTERY_THANKYOU, "谢谢参与"));
        }

    }

    /**
     * 抽奖方法
     */
    @Before(POST.class)
    public void lotteryDraw(){

        ClazMod.Request request = getParaObject(ClazMod.Request.class);
        String account = getIdentity(request.getToken());

        if(!com.alibaba.druid.util.StringUtils.isEmpty(account)){

            ////////同一账号测试使用代码 begin//////////
//                if(true){
//                    account = account + "_" + System.currentTimeMillis();
//                }
            ////////同一账号测试使用代码 end//////////

            if(!AttendLog.me.checkByAccount(account)){
                Map<String, String> resultMap = HonourRoll.me.lotteryDraw(account);

                if(resultMap != null && resultMap.size() > 0){
                    renderJson(new ClazMod.LotteryResponse(INTERFACE_NORMAL, resultMap.get("prizeCode"), resultMap.get("luckyNo")));
                }else{
                    renderJson(new ClazMod.LotteryResponse(INTERFACE_NORMAL, LOTTERY_THANKYOU, "谢谢参与"));
                }
            }else{
                renderJson(new ClazMod.LotteryResponse(INTERFACE_NORMAL, LOTTERY_THANKYOU, "抽过奖"));
            }
        }
    }
}
