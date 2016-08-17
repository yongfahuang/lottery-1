package me.ele.micservice.models.lottery;

import com.jfinal.aop.Before;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.TxByMethods;
import me.ele.micservice.Config;
import me.ele.micservice.models.enums.STAGE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 奖品
 * <p>
 * Created by jiangbo.cheng on 2016-08-08 14:48.
 */
public class Prize extends Model<Prize>{
    private static Logger logger = LoggerFactory.getLogger(Prize.class);

    public static final Prize me = new Prize();
    public static final String TABLE = "tb_prize";
    public static final String ID = "id";
    private static String QUERY_BY_STAGE_SQL = "select id,name,code,number,stage from tb_prize where stage = ?";
    private static String QUERY_BY_CODE_SQL = "select id,name,code,number,stage from tb_prize where code = ?";
    private static String QUERY_ALL_SQL = "select id,`name`,`code`,number,stage from tb_prize where number > 0 and stage = ?";
    private static String SUM_BY_STAGE_SQL = "select SUM(number) as amount from tb_prize where stage =?";
    private static String COPY_PRIZE_SQL = "select `name`,`code`,number from tb_prize where stage = ? and number > 0";
    private static String INSERT_PRIZE_SQL="insert into tb_prize(`name`,`code`,number,stage) values(?,?,?,?)";
    private static String UPDATE_BY_STAGE_SQL = "update tb_prize set number =? where stage = ?";
    private static String UPDATE_BY_STAGEANDCODE_SQL = "update tb_prize set number =? where stage = ? and code =?";
    private static String QUERY_BY_CODEANDSTAGE_SQL  = "select id,name,code,number,stage from tb_prize where code = ? and stage = ?";

    //阶段一的人次
    public static int STEAGE_FIRST_PERSION_TIMES = PropKit.getInt("lottery.stage.first.persion-times");
    //阶段二的人次
    public static int STEAGE_SECOND_PERSION_TIMES = PropKit.getInt("lottery.stage.second.persion-times");

    /**
     * 查询所有的奖品(查询商品数量大于1的奖品)
     * @return
     */
    public List<Prize> queryAll(STAGE stage)throws Exception{
        return this.me.use(Config.OTHERS_DATABASE_NAME).find(QUERY_ALL_SQL,stage.getVal());
    }

    /**
     * 根据阶段查询奖品
     * @param stage
     * @return
     */
    public List<Prize> queryByStage(STAGE stage)throws Exception{
        return this.me.use(Config.OTHERS_DATABASE_NAME).find(QUERY_ALL_SQL,stage.getVal());
    }

    /**
     * 根据奖品代号查询
     * @param code
     * @return
     */
    public List<Prize> queryByCode(String code)throws Exception{
        return this.me.use(Config.OTHERS_DATABASE_NAME).find(QUERY_BY_CODE_SQL, code);
    }

    /**
     * 根据抽奖阶段获得阶段的奖品总数量
     * @param stage
     * @return
     */
    public int countPrize(STAGE stage)throws Exception{
        List<Record> list = Db.use(Config.OTHERS_DATABASE_NAME).find(SUM_BY_STAGE_SQL, stage.getVal());
        if(list != null && list.size() > 0){
            return list.get(0).getBigDecimal("amount").intValue();
        }
        return -1;
    }

    /**
     * 更新奖品数量
     * @param code
     * @return
     */
    public boolean update(String code,STAGE stage)throws Exception{
        int line = Db.use(Config.OTHERS_DATABASE_NAME).update("update tb_prize set number = number-1 where (number -1) >-1 and `code` = ? and stage = ?", code, stage.getVal());
        return line > 0;
    }

    /**
     * 开始第二轮抽奖的同时，将第一轮剩余的奖品放到第二奖品袋里
     * @param stage
     * @return 拷贝了多少个剩余的奖品到第二轮
     */
    @Before(TxByMethods.class)
    public int copyPrizeToNextStage(STAGE stage) throws Exception{
        int surplusNumber = 0;

        List<Prize> stagePrizeList =  this.me.use(Config.OTHERS_DATABASE_NAME).find(COPY_PRIZE_SQL, stage.getVal());
        if(stagePrizeList !=null && stagePrizeList.size() > 0){
            for(Prize prize :stagePrizeList){
                String name = prize.getStr("name");
                String code = prize.getStr("code");
                int number = prize.getInt("number");

                List<Prize> _prizeList = this.me.use(Config.OTHERS_DATABASE_NAME).find(QUERY_BY_CODEANDSTAGE_SQL,code,STAGE.TWO.getVal());
                if(_prizeList != null && _prizeList.size() > 0 ){
                    if(_prizeList.size() == 1){
                        String _code = _prizeList.get(0).getStr("code");
                        int _number = _prizeList.get(0).getInt("number");
                        if(Db.use(Config.OTHERS_DATABASE_NAME).update(UPDATE_BY_STAGEANDCODE_SQL, number+_number, STAGE.TWO.getVal(), code) > 0){
                            logger.info("...更新阶段"+STAGE.TWO.getVal()+"奖品代号"+code+"库存成功...");
                        }else{
                            logger.error("Prize->copyPrizeToNextStage(...) exception:","更新阶段"+STAGE.TWO.getVal()+"奖品代号"+code+"库存失败");
                        }
                    }else{
                        throw new Exception("Prize->copyPrizeToNextStage(...) exception:抽奖阶段"+stage.getVal()+"的奖品代号"+code+"存在多条数据，数据库数据错误");
                    }
                }else {
                    if(Db.use(Config.OTHERS_DATABASE_NAME).update(INSERT_PRIZE_SQL, name, code, number, STAGE.TWO.getVal()) > 0)
                        logger.info("...更新阶段"+STAGE.TWO.getVal()+"奖品代号"+code+"库存成功...");
                    else
                        logger.error("Prize->copyPrizeToNextStage(...) exception:","更新阶段"+STAGE.TWO.getVal()+"奖品代号"+code+"库存失败");
                }

                surplusNumber+=number;
            }
        }

        if(surplusNumber > 0){
            if(Db.use(Config.OTHERS_DATABASE_NAME).update(UPDATE_BY_STAGE_SQL, 0, stage.getVal()) == 0){
                throw new Exception("Prize->copyPrizeToNextStage(...) exception:上一轮的抽奖奖品归零失败");
            }else{
                logger.info("...上一轮抽奖剩余的奖品已经放到下一轮奖品袋里，同时上一轮奖品归零成功...");
            }
        }

        return surplusNumber;
    }

    /**
     * 初始化数据
     * @param prizeMap
     * @param prizeCountMap
     * @param codeAndNumList
     * @throws Exception
     */
    @Before(TxByMethods.class)
    public void initData(Map<String,String> prizeMap, Map<String,Integer> prizeCountMap, List<String> codeAndNumList)
    throws Exception{
        STAGE stage= STAGE.ONE;

        int surplusNumber = 0;//第一轮抽奖剩余的奖品

        long persion_times = AttendLog.me.countLog();//已经抽奖的人次
        if(persion_times > STEAGE_FIRST_PERSION_TIMES){//超过第一阶段的人次，就开始加载第二阶段的人次

            surplusNumber = Prize.me.copyPrizeToNextStage(stage);
            if(surplusNumber > 0){
                logger.info("...检测到上一轮抽奖有剩余"+surplusNumber+"个奖品，放至第二轮奖品袋里...");
            }

            stage= STAGE.TWO;
        }

        long prizeCount = 0;
        List<Prize> prizeList = this.queryAll(stage);
        if(prizeList !=null && prizeList.size()>0 && prizeMap != null && prizeCountMap != null && codeAndNumList != null){
            for(Prize prize:prizeList){
                String code = prize.getStr("code");
                String name = prize.getStr("name");
                int number = prize.getInt("number");
                //int _stage = prize.getInt("stage");

                prizeMap.put(code,name);
                prizeCountMap.put(code, number);
                codeAndNumList.add(code + "-" + number);
                prizeCount+=number;//统计奖品个数
            }
        }

        if(surplusNumber > 0){
            logger.info("...上一轮抽奖的剩余的奖品放到下一轮后，下一轮的抽奖随机数范围也相应是剩余奖品个数的10倍...");
            if(LotteryPrams.me.update(prizeCount*10, stage)){
                logger.info("Prize->initData(...) exception:第二轮抽奖奖品数据更新成功");
            }
        }
    }
}