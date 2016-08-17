package me.ele.micservice;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.util.JdbcConstants;
import com.alibaba.druid.wall.WallFilter;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.ext.handler.ContextPathHandler;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.CaseInsensitiveContainerFactory;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.activerecord.dialect.OracleDialect;
import com.jfinal.plugin.druid.DruidPlugin;
import me.ele.micservice.controllers.AuthController;
import me.ele.micservice.controllers.ExceptionHandler;
import me.ele.micservice.models.*;
import me.ele.micservice.handlers.AddHeaderHandler;
import me.ele.micservice.handlers.VersionHandler;
import me.ele.micservice.models.lottery.AttendLog;
import me.ele.micservice.models.lottery.HonourRoll;
import me.ele.micservice.models.lottery.LotteryPrams;
import me.ele.micservice.models.lottery.Prize;
import me.ele.micservice.plugins.jedis.JedisPlugin;
import me.ele.micservice.plugins.mongo.MongoPlugin;
import me.ele.micservice.routes.AutoBindRoutes;

/**
 * Created by Administrator on 2015/3/28.
 */
public class Config extends JFinalConfig {
    public static final String OTHERS_DATABASE_NAME = "others";


    public void configConstant(Constants constants) {
//        constants.setDevMode(true);
        constants.setEncoding("UTF-8");
    }

    @Override
    public void configRoute(Routes routes) {

        routes.add(new AutoBindRoutes("me/ele/micservice/controllers"));
    }

    @Override
    public void configPlugin(Plugins me) {

        loadPropertyFile("conf.properties", "utf-8");
        JedisPlugin jedisPlugin = new JedisPlugin();
        DruidPlugin druidPlugin = new DruidPlugin(getProperty("mysql.url"), getProperty("mysql.username"), getProperty("mysql.password"));

        ActiveRecordPlugin activeRecordPlugin = new ActiveRecordPlugin(OTHERS_DATABASE_NAME, druidPlugin);
        activeRecordPlugin.setDialect(new MysqlDialect());
        activeRecordPlugin.setContainerFactory(new CaseInsensitiveContainerFactory(true)).setShowSql(true);

        activeRecordPlugin.addMapping(AttendLog.TABLE, AttendLog.ID, AttendLog.class)
        .addMapping(HonourRoll.TABLE, HonourRoll.ID, HonourRoll.class)
        .addMapping(Prize.TABLE, Prize.ID, Prize.class)
        .addMapping(LotteryPrams.TABLE, LotteryPrams.ID, LotteryPrams.class);

        me.add(druidPlugin)
                .add(activeRecordPlugin)
                .add(jedisPlugin);
    }

    @Override
    public void configInterceptor(Interceptors interceptors) {
        interceptors.add(new ExceptionHandler());
    }

    @Override
        public void configHandler(Handlers handlers) {
        handlers.add(new VersionHandler())
                .add(new AddHeaderHandler().addHeader("Access-Control-Allow-Origin", "*"))
                .add(new AddHeaderHandler().addHeader("Access-Control-Allow-Headers", "version"))
                .add(new AddHeaderHandler().addHeader("Access-Control-Allow-Headers", "X-Requested-With,X_Requested_With"));
    }

}
