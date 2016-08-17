package me.ele.micservice.models.lottery;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import me.ele.micservice.Config;
import org.slf4j.Logger;

import java.util.List;

/**
 * 参加抽奖的日志
 *
 * Create by jiangbo.cheng on 2016-8-8
 */
public class AttendLog extends Model<AttendLog>{
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(AttendLog.class);

    public static final AttendLog me = new AttendLog();
    public static final String TABLE = "tb_attend_log";
    public static final String ID = "id";
    private static String COUNT_SQL = "select count(1) from tb_attend_log";
    private static String CHECK_BY_ACCOUNT = "select id,account,create_time from tb_attend_log where account=?";
    private static String INSERT_SQL = "insert into tb_attend_log(account,random_num,create_time) values(?,?,now())";

    /**
     * 添加或更新数据
     * @param account
     * @return
     */
    public boolean insertOrUpdate(String account, String randomNum)throws Exception{
        int line = Db.use(Config.OTHERS_DATABASE_NAME).update(INSERT_SQL, account,randomNum);
        return line > 0;
    }

    /**
     * 根据员工账号查询抽奖日志
     * @param account
     * @return
     */
    public boolean checkByAccount(String account ){
        try {
            List<AttendLog> attendLogList = this.me.use(Config.OTHERS_DATABASE_NAME).find(CHECK_BY_ACCOUNT, account);
            return attendLogList !=null && attendLogList.size()>0 ? true:false;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    /**
     * 数据记录
     * @return
     */
    public long countLog()throws Exception{
        return Db.use(Config.OTHERS_DATABASE_NAME).queryLong(COUNT_SQL);
    }
}
