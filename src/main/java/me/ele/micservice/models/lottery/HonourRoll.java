package me.ele.micservice.models.lottery;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.tx.TxByMethods;
import me.ele.micservice.Config;
import me.ele.micservice.models.enums.STAGE;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

/**
 * 中奖光荣榜
 * <p>
 * Created by jiangbo.cheng on 2016-08-08 13:57.
 */
public class HonourRoll extends Model<HonourRoll> {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(HonourRoll.class);

    public static final HonourRoll me = new HonourRoll();
    public static final String TABLE = "tb_honour_roll";
    public static final String ID = "id";
    private static String INSERT_SQL = "insert into tb_honour_roll(account,prize_code,create_time,lucky_no)values(?,?,now(),?)";
    private static String QUERY_BY_EMPLOYEEID_SQL = "select id,account,prize_code,create_time,lucky_no from tb_honour_roll where account = ?";
    private static String QUERY_BY_LUCKYNO_SQL = "select id,create_time,lucky_no from tb_honour_roll where lucky_no = ?";

    /**
     * 插入中奖名单
     * @param account
     * @param prizeCode
     * @param luckyNo
     * @return
     */
    public boolean insert(String account, String prizeCode, String luckyNo) throws Exception{
        if(!checkLuckyNo(luckyNo)) {
            int result = Db.use(Config.OTHERS_DATABASE_NAME).update(INSERT_SQL, account, prizeCode, luckyNo);
            return result > 0;
        }else{
            throw new Exception("HonourRoll->insert(...) exception:中奖幸运号" + luckyNo + "重复, 程序错误");
        }
    }

    /**
     * 检查插入的中奖幸运号是否正常
     * @param luckyNo
     * @return
     */
    public boolean checkLuckyNo(String luckyNo)throws Exception{
        List<HonourRoll> list = this.me.use(Config.OTHERS_DATABASE_NAME).find(QUERY_BY_LUCKYNO_SQL, luckyNo);
        return list != null && list.size() > 0 ? true:false;
    }

    /**
     * 根据员工账号查询中奖名单
     * @param account
     * @return
     */
    public HonourRoll queryByAccount(String account){
        try {
            List<HonourRoll> list = this.me.use(Config.OTHERS_DATABASE_NAME).find(QUERY_BY_EMPLOYEEID_SQL, account);
            return list != null && list.size() > 0 ? list.get(0):null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 抽奖
     * @param account
     * @throws Exception
     */
    @Before(TxByMethods.class)
    public Map<String, String> lotteryDraw(String account){

            try {
                Map<String,String> resultMap = Maps.newHashMap();

                Map<String,String> prizeMap = Maps.newHashMap();
                Map<String,Integer> prizeCountMap = Maps.newHashMap();
                List<String> codeAndNumList = Lists.newArrayList();

                //初始化数据
                Prize.me.initData(prizeMap, prizeCountMap, codeAndNumList);

                if(prizeMap != null && prizeCountMap != null && codeAndNumList != null){

                    //codeArray奖品代号数组
                    String[] codeArray = new String[prizeCountMap.size()];
                    //根据奖品的中间概率计算得出的区间数组
                    Integer[] sectionArray = new Integer[prizeCountMap.size()];

                    STAGE stage = STAGE.ONE;
                    //当前随机数范围，奖品的数量减1，随机数减10
                    int randomRange = LotteryPrams.me.queryByStage(stage);

                    long persion_times = AttendLog.me.countLog();//已经抽奖的人次
                    if(persion_times > Prize.STEAGE_FIRST_PERSION_TIMES){//超过第一阶段4000人次抽奖，就开始第二阶段的抽奖
                        stage = STAGE.TWO;
                        randomRange = LotteryPrams.me.queryByStage(stage);

                        logger.info("###第"+stage.getVal()+"阶段抽奖###");
                    }

                    //奖品数
                    //int prizeCount = Prize.me.countPrize(stage);
                    int times = 10;//抽奖人次是奖品的多少倍

                    //区间计算
                    sectionCalculate(prizeCountMap, codeAndNumList, codeArray, sectionArray);

                    //List<Integer> list = Lists.newArrayList();//记录之前已经生成并中奖的随机数
                    List<String> honourRoll = Lists.newArrayList();//光荣榜
                    long time  = System.currentTimeMillis();


                    logger.info(new StringBuffer().append("......第").append(persion_times+1).append("次抽奖......").toString());

                    int randomNum = 0;
                    StringBuilder luckyNo =  new StringBuilder();
                    if(randomRange > 0){//当奖品已抽完，随机数将变成0
                        randomNum =(new Random().nextInt(randomRange))+1;//随机数
                        int isZero = randomNum%times;//当随机数是10的倍数时抽奖有戏
                        luckyNo.append(stage.getVal()).append("-").append(time).append("-").append(randomNum);

                        String lotteryStr = isZero== 0 && randomNum != 0 ? "成功":"失败";
                        logger.info(new StringBuffer().append("生成随机数[").append(randomNum).append("],中奖").append(lotteryStr).append("->").append("剩下奖品种类：").append(prizeCountMap.size()).toString());

                        if(isZero == 0){//当随机数是10的倍数时抽奖有戏
                            logger.info("...记录随机数...");
                            //list.add(randomNum);

                            int lotteryNum = randomNum/10;//中奖数值

                            //查找奖项
                            int index = sectionSearch(sectionArray, lotteryNum);
                            String code = codeArray[index];

                            logger.info(new StringBuffer().append("恭喜获得的奖品是【").append(prizeMap.get(code)).append("】").toString());
                            resultMap.put("prizeCode", code);//记录中奖信息
                            resultMap.put("luckyNo",luckyNo.toString());//记录中奖信息

                            honourRoll.add("序列号：" + luckyNo + "\t获得奖品：" + prizeMap.get(code));
                            if(HonourRoll.me.insert(account, code, luckyNo.toString())){
                                logger.info(new StringBuffer().append("账号【").append(account).append("】获得奖品：").append(prizeMap.get(code)).toString());
                            }else{
                                logger.error(new StringBuffer().append("账号【").append(account).append("】获得奖品：").append(prizeMap.get(code)).append("入库失败异常").toString());
                            }

                            if(prizeCountMap.size() == 0) {
                                logger.info(new StringBuffer().append("...阶段").append(stage.getVal()).append("奖品已抽完...").toString());
                            }else{
                                //商品数量减一,随机数也减去10
                                if(Prize.me.update(code, stage)) {
                                    if(LotteryPrams.me.update(randomRange -= 10, stage)){
                                        logger.info(new StringBuffer().append("...抽奖阶段").append(stage.getVal()).append("更新抽奖随机数范围为：").append(randomRange).toString());
                                    }else{
                                        logger.error("HonourRoll->lotteryDraw(...) exception:{}",new StringBuffer().append("抽奖阶段").append(stage.getVal()).append("更新抽奖数范围范围为：").append(randomRange).append("失败异常").toString());
                                    }
                                }else{
                                    logger.error("HonourRoll->lotteryDraw(...) exception:{}","奖品库存减一异常");
                                }
                            }
                        }
                    }else{
                        luckyNo.append(stage.getVal()).append("-").append(time).append("-").append(randomNum);
                        logger.info("...阶段" + stage.getVal() + "抽奖人次已达限制...");
                    }

                    if(AttendLog.me.insertOrUpdate(account, luckyNo.toString()))
                        logger.info("记录抽奖日志成功");
                    else
                        logger.error("HonourRoll->lotteryDraw(...) exception:记录抽奖日志失败,异常");

                    logger.info(new StringBuilder().append("...抽了").append(persion_times+1).append("次奖...").toString());
                }

                return resultMap.size() == 0 ? null:resultMap;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
    }

    /**
     * 区间计算
     * @param prizeCountMap
     * @param codeAndNumList
     * @param codeArray
     * @param sectionArray
     */
    public static void sectionCalculate(Map<String,Integer> prizeCountMap,
                                List<String> codeAndNumList,
                                String[] codeArray,
                                Integer[] sectionArray){

        logger.info("......抽奖数据区间计算开始......");
        //从prizeCountMap获取数量放进数组
        Collection<Integer> numList = prizeCountMap.values();
        Object[] objArray = numList.toArray();
        Integer[] numArray = new Integer[objArray.length];
        for(int i=0;i<objArray.length;i++){
            numArray[i]=(Integer)objArray[i];
        }

        logger.info("排序前的奖品数量---------------");
        HonourRoll.printArray(numArray);

        logger.info("排序后的奖品数量---------------");
        Arrays.sort(numArray);
        HonourRoll.printArray(numArray);
        logger.info("序号相应排序后---------------");
        for(int j=0;j<numArray.length;j++){
            for(int i = 0;i<codeAndNumList.size();i++){
                String[] _array = codeAndNumList.get(i).split("-");
                int _code = Integer.parseInt(_array[0]);
                int num = Integer.parseInt(_array[1]);

                if(numArray[j].intValue() == num){
                    codeArray[j]=_array[0];
                    codeAndNumList.remove(i);
                    break;//规避数量重复的奖品
                }
            }
        }
        HonourRoll.printArray(codeArray);

        for(int i=0;i<numArray.length;i++){
            if(i>0){
                sectionArray[i]=sectionArray[i-1]+numArray[i];
            }else{
                sectionArray[i] = numArray[i];
            }
        }

        logger.info("通过区间算法得出的数组如下：");
        printArray(sectionArray);

        logger.info("......抽奖数据区间计算结束......");
    }

    /**
     * 区间查找
     * @param array
     * @param num
     * @return
     */
    public static int sectionSearch(Integer[] array,int num){
        int result = -1;
        for(int i = 0;array != null && i<array.length;i++){
            if(num <= array[i]){
                result = i;
                break;
            }else if(i != array.length-1 && num > array[i] && num <= array[i+1]){
                result = i+1;
                break;
            }
        }

        if(result == -1){
            logger.error("HonourRoll->sectionSearch() exception:区间值未查到："+num);
        }

        return result;
    }

    public static void printArray(Serializable[] array){
        if(array != null && array.length>0){
            for(int i=0;i<array.length;i++){
                System.out.print(array[i]+"\t");
            }
        }
        System.out.println();
    }

}
