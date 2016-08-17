package me.ele.micservice.models;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * Created by alvin on 2016/7/6.
 */
public class ClazMod {
    public static class Request {

        private String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
    public static class Response {
        private int code;
        private String msg;

        public Response(int code, String msg) {
            this.code = code;
            this.msg = msg;
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



        public static class SurveyRequest extends Request{
            private List data;
            private String state;

            public List getData() {
                return data;
            }

            public void setData(List data) {
                this.data = data;
            }

            public String getState() {
                return state;
            }

            public void setState(String state) {
                this.state = state;
            }
        }
    public static class SurveyResponse extends Response{
        private List data;
        private String state;
        public SurveyResponse(int code,String msg,List data,String state){
                super(code,msg);
                this.data = data;
                this.state = state;
            }
        public List getData() {
                return data;
            }

            public String getState() {
                return state;
            }
    }

    public static class LotteryResponse extends Response{
        private Integer state;
        private String prizeCode;
        private String msg;

        public LotteryResponse(int code, Integer state, String msg) {
            super(code, msg);
            this.state = state;
            this.msg = msg;
        }

        public LotteryResponse(int code, String prizeCode, String msg) {
            super(code, msg);
            this.prizeCode = prizeCode;
            this.msg = msg;
        }

        public Integer getState() {
            return state;
        }

        public String getMsg() {
            return msg;
        }

        public String getPrizeCode() {
            return prizeCode;
        }
    }
}
