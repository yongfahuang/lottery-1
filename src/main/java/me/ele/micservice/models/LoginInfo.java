package me.ele.micservice.models;

import com.jfinal.plugin.activerecord.Model;

import java.util.Date;

/**
 * Created by alvin on 2015/10/30.
 */
public class LoginInfo extends Model<LoginInfo> {
    public static final LoginInfo me=new LoginInfo();
    public static final String TABLE="sso_logininfo";
    public static final String ID="id";

    @Override
    public boolean save() {
        this.set("created", new java.sql.Date(new Date().getTime()));
        return super.save();
    }

}
