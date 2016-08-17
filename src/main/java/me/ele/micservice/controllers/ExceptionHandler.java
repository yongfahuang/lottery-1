package me.ele.micservice.controllers;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import me.ele.micservice.models.ClazMod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by alvin on 15/4/29.
 */
public class ExceptionHandler implements Interceptor {

    public static final String TOKEN_EXPIRE = "该token已失效！";

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);
    private static final Map<String, Integer> CODE = new HashMap<>();

    static {
        CODE.put(TOKEN_EXPIRE, 403);
    }

    @Override
    public void intercept(Invocation ai) {

        Controller controller = ai.getController();

        try {
            ai.invoke();
        } catch (Exception e) {
            logger.error("异常错误", e);
            String msg = e.getMessage();
            controller.renderJson(new AuthController.Response(CODE.containsKey(msg) ? CODE.get(msg): 1, msg));
        }
    }
}
