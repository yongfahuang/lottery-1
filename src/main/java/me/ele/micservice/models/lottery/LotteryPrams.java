package me.ele.micservice.models.lottery;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import me.ele.micservice.Config;
import me.ele.micservice.models.enums.STAGE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 抽奖参数
 * <p>
 * Created by jiangbo.cheng on 2016-08-08 14:48.
 */
public class LotteryPrams extends Model<LotteryPrams>{
    private static Logger logger = LoggerFactory.getLogger(LotteryPrams.class);

    public static final LotteryPrams me = new LotteryPrams();
    public static final String TABLE = "tb_params";
    public static final String ID = "id";
    private static String UPDATE_BY_STAGE_SQL = "update tb_params set current_random_range = ? where stage = ? ";
    private static String QUERY_SQL = "select id,current_random_range from tb_params where stage = ?";

    /**
     * 查询当前随机数生成范围
     * @param stage
     * @return
     */
    public int queryByStage(STAGE stage)throws Exception{
        LotteryPrams lotteryPrams = this.me.use(Config.OTHERS_DATABASE_NAME).findFirst(QUERY_SQL, stage.getVal());
        long value = lotteryPrams.getLong("current_random_range");
        if(value > 0)
            return new Long(value).intValue();
        else
            return 0;
    }

    /**
     * 更新当前随机数生成范围
     * @param rangeNum
     * @param stage
     * @return
     */
    public boolean update(long rangeNum,STAGE stage)throws Exception{
        int line = Db.use(Config.OTHERS_DATABASE_NAME).update(UPDATE_BY_STAGE_SQL, rangeNum, stage.getVal());
        return line > 0;
    }

}