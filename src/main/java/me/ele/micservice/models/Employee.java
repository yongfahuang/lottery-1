package me.ele.micservice.models;

import com.jfinal.plugin.activerecord.Model;

/**
 * Created by alvin on 2015/9/23.
 */
public class Employee extends Model<Employee> {
    public static final Employee me = new Employee();
    public static final String TABLE = "tb_employee";
    public static final String ID = "PK_PSNDOC";
    public static final String FINDEMPLOYEE="select psnname,psncode,email,sex,user_code,mobile,user_code from tb_employee where UPPER(user_code)=UPPER(?) or UPPER(psncode)=UPPER(?)";
    public static final String FINDEMPLOYEEPSNCODE="select psnname,psncode,email,sex,user_code,mobile from tb_employee where UPPER(psncode)=UPPER(?)";
    public static final String GETPERSONINFO="select psncode,psnname,mobile,email,city_name,onedeptname,onedeptcode,deptname,deptcode,user_code from tb_employee where UPPER(user_code)=UPPER(?) or UPPER(psncode)=UPPER(?) ";

    public Employee findByUserCode(String user_code){
        return this.use("qlds").findFirst(FINDEMPLOYEE,user_code,user_code);
    }
    public Employee findByPsncode(String user_code){
        return this.use("qlds").findFirst(FINDEMPLOYEEPSNCODE,user_code);
    }
    public Employee getPersonInfo(String user_code){
        return this.use("qlds").findFirst(GETPERSONINFO,user_code,user_code);
    }
}
