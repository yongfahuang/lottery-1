package me.ele.micservice.handlers;

import com.jfinal.handler.Handler;
import com.jfinal.kit.StrKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by frankliu on 15/9/2.
 * 该处理器会将version头信息转换成对应的controller，主要用来处理API多版本问题，使服务能同时存在多个版本供前端使用，比如：
 * /message header[version:0.1.0] ==> /message0_1_0
 */
public class VersionHandler extends Handler {

    private static final Logger logger = LoggerFactory.getLogger(VersionHandler.class);

    public void handle(String target, HttpServletRequest request, HttpServletResponse response, boolean[] isHandled) {

        String version = request.getHeader("version");
        if(StrKit.notBlank(version)) {
            logger.debug("API Version:" + version);
            String[] tokens = target.split("/");
            StringBuilder buf = new StringBuilder();
            for(int i = 1; i < tokens.length; i++) {
                buf.append("/" + tokens[i]);
                if(i == 1) {
                    buf.append(version.replaceAll("\\.", "_"));
                }
            }
            target = buf.toString();
        }

        nextHandler.handle(target, request, response, isHandled);
    }
}
