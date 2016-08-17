package me.ele.micservice.controllers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.ext.interceptor.POST;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import me.ele.micservice.models.ClazMod;
import me.ele.micservice.plugins.jedis.JedisKit;
import me.ele.micservice.utils.CSVUtils;
import me.ele.micservice.utils.HttpclientUtil;
import me.ele.micservice.utils.MD5Util;
import me.ele.micservice.utils.TokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by alvin on 2016/3/4.
 */
public class InvesController extends AuthController{
    private static final Logger logger = LoggerFactory.getLogger(InvesController.class);

    /**
     * 保存答题结果
     *  state: 0:未答题 1:未答完 2:已答完
     */
    @Before(POST.class)
    public  void setSurvey() {
        ClazMod.SurveyRequest request = getParaObject(ClazMod.SurveyRequest.class);
        logger.info(getClass().getMethods()+" || 请求："+JSON.toJSONString(request));
        if(!JedisKit.exists("token:"+request.getToken())){
            throw new RuntimeException("token无效");
        }
        //state: 0:未答题 1:未答完 2:已答完
        String account = JedisKit.hget("token:"+request.getToken(),"userid");
        List person = getUInfo(request.getToken());
        if(person!=null && getUInfo(request.getToken()).size()>0){
            JedisKit.hset("account:" + account, "data", String.valueOf(request.getData()));
            JedisKit.hset("account:" + account, "info",JSON.toJSONString(person));
            Map dataMap = new HashMap();
            dataMap.put("data",request.getData());
            dataMap.put("info",person);
            JedisKit.hset("surveypersons","account"+account,JSON.toJSONString(dataMap));
            if(request.getData() == null){
                JedisKit.hset("account:" + account, "state","0");
            }else{
                JedisKit.hset("account:" + account, "state", JSON.parseArray(request.getData().toString()).size()==PropKit.getInt("survey.count")  ? "2":JSON.parseArray(request.getData().toString()).size()==0?"0":"1");
            }
        }else {
            throw new RuntimeException("未查到对应人员信息");
        }
        logger.info(getClass().getMethods()+" || 返回："+JSON.toJSONString(new ClazMod.SurveyResponse(0,"success",new ArrayList(),JedisKit.hget("account:" + account, "state"))));
        renderJson(new ClazMod.SurveyResponse(0,"success",new ArrayList(),JedisKit.hget("account:" + account, "state")));

    }

    /**
     * 获取答题结果
     *  state: 0:未答题 1:未答完 2:已答完
     */
    @Before(POST.class)
    public  void getSurvey() {
        ClazMod.SurveyRequest request = getParaObject(ClazMod.SurveyRequest.class);
        logger.info(getClass().getMethods()+" || 请求："+JSON.toJSONString(request));
        if(!JedisKit.exists("token:"+request.getToken())){
            throw new RuntimeException("token无效");
        }
        String account = JedisKit.hget("token:"+request.getToken(),"userid");
        List data = new ArrayList<>();
        String state = "";
        if(JedisKit.exists("account:"+account)){
            data = JSON.parseArray(JedisKit.hget("account:" + account, "data"));
            state = JedisKit.hget("account:" + account, "state");
        }else {
            data=new ArrayList();
            state = "0";
        }
        logger.info(getClass().getMethods()+" || 返回："+JSON.toJSONString(new ClazMod.SurveyResponse(0,"success",data,state)));
            renderJson(new ClazMod.SurveyResponse(0,"success",data,state));
    }
    public void exportExcel() throws IOException{
        List head = new ArrayList<>();
        List body = new ArrayList<>();
        head.add("一级部门");
        head.add("直属部门");
        head.add("工号");
        head.add("姓名");
        head.add("邮箱");
        head.add("手机号");
//        head.add("岗位");
        for(int i = 1;i<PropKit.getInt("survey.count")+1;i++){
            head.add(i);
        }
        Map<String,String> keys = JedisKit.hgetall("surveypersons");
        //遍历map中的值

        for (String value : keys.values()) {
            Map perdata = JSON.parseObject(value);
            List data = new ArrayList<>();
            List lt = JSON.parseArray(perdata.get("data").toString());
            List row = JSON.parseArray(perdata.get("info").toString());
            if(lt.size()==PropKit.getInt("survey.count")){
                for(int i=0;i<lt.size();i++ ){
                    JSONObject obj =  JSON.parseObject(lt.get(i).toString());
                    for (Object oj: obj.values()) {
                        data.add(oj);
                    }
                }
                row.addAll(data);
                body.add(row);
            }
        }
//        for(String key : keys){
//            List data = new ArrayList<>();
//            List lt = JSON.parseArray(JedisKit.hget(key, "data"));
//            List row = JSON.parseArray(JedisKit.hget(key, "info"));
//            String state = StrKit.isBlank(JedisKit.hget(key, "state"))?lt.size()==PropKit.getInt("survey.count")?"2":"0":JedisKit.hget(key, "state");
//            if(lt.size()==PropKit.getInt("survey.count")){
//                for(int i=0;i<lt.size();i++ ){
//                    JSONObject obj =  JSON.parseObject(lt.get(i).toString());
//                    for (Object oj: obj.values()) {
//                        data.add(oj);
//                    }
//                }
//                row.addAll(data);
//                body.add(row);
//            }
//        }

        File csvFile = CSVUtils.createCSVFile(head,body,"files","survey");

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            getResponse().setHeader("Content-disposition", "attachment; filename=" + URLEncoder.encode(csvFile.getName(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        getResponse().setHeader("Content-Length", String.valueOf(csvFile.length()));
        bis = new BufferedInputStream(new FileInputStream(csvFile));
        bos = new BufferedOutputStream(getResponse().getOutputStream());
        byte[] buff = new byte[2048];
        while (true) {
            int bytesRead;
            if (-1 == (bytesRead = bis.read(buff, 0, buff.length))) break;
            bos.write(buff, 0, bytesRead);
        }
        bis.close();
        bos.close();
    }

    private List getUInfo(String token){
        Map<String,String> para = new HashMap<>();
        Map<String,Object> request = new HashMap<>();
        para.put("Content-Type","application/json");
        request.put("token", token);
        long timeStamp = new Date().getTime();
        request.put("timeStamp", timeStamp);
        request.put("sign", MD5Util.string2MD5(token+timeStamp+PropKit.get("sso.accesskey")));
//        logger.info(getClass().getMethods() + " || sso请求：" + request);
        Map<String,String> response = null;
        try {
            response = HttpclientUtil.postJSON(PropKit.get("sso.infourl"), para, request, Map.class);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        List info = new ArrayList<>();
        if(response != null && response.containsKey("code")) {
            if(String.valueOf(response.get("code")).equals("0")){
                com.alibaba.fastjson.JSONObject o = JSON.parseObject(String.valueOf(response.get("employee")));
                info.add(o.getString("onedeptname")!=null?o.getString("onedeptname"):"");
                info.add(o.getString("deptname")!=null?o.getString("deptname"):"");
                info.add(o.getString("psncode")!=null?o.getString("psncode"):"");
                info.add(o.getString("psnname")!=null?o.getString("psnname"):"");
                info.add(o.getString("email")!=null?o.getString("email"):"");
                info.add(o.getString("mobile")!=null?o.getString("mobile"):"");
                         }
        }
        logger.info(getClass().getMethods()+" || sso返回："+response);
        return info;
    }
}
