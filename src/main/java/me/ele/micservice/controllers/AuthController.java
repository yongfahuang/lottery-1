package me.ele.micservice.controllers;

import com.alibaba.fastjson.JSON;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.ext.interceptor.GET;
import com.jfinal.ext.interceptor.POST;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import me.ele.micservice.models.ClazMod;
import me.ele.micservice.plugins.jedis.JedisKit;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Created by alvin on 2015/10/16.
 */
public class AuthController extends Controller {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    protected <T extends ClazMod.Request> T getParaObject(Class<T> clazz) {

        String data;

        try {
            data = IOUtils.toString(getRequest().getInputStream(), "UTF-8");

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        T o = JSON.parseObject(data, clazz);

        logger.info("request:{}", data);

        return o;
    }

    protected String getIdentity(String token) {

        String account = JedisKit.hget("token:" + token, "userid");
        if(StrKit.isBlank(account)) {
            throw new RuntimeException(ExceptionHandler.TOKEN_EXPIRE);
        } else {
            return account;
        }

    }

    public static class Response {
        private int code;
        private String msg;

        public Response(int code, String msg) {
            this.code = code;
            this.msg = msg;
            logger.info("response:code[{}], msg[{}]", code, msg);
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }
}
