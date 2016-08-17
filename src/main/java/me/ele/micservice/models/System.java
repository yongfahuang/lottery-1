package me.ele.micservice.models;

import com.jfinal.plugin.activerecord.Model;

import java.util.Date;
import java.util.List;

/**
 * Created by alvin on 2015/10/29.
 */
public class System extends Model<System>{
    public static final System me=new System();
    public static final String TABLE="sso_iden";
    public static final String ID="id";
    public static final String FINDBYACCESSKEY="select * from sso_iden where accesskey=?";
    public static final String FINDSYSLIST="select name,displayname,description,created,updated,iconurl from sso_iden where pcdisplay=0";
/*    public static final String RANKLIST="select a.*,b.count from sso_iden a\n" +
            "join\n" +
            "(select count(1) count,sysname from sso_logininfo \n" +
            "where trunc(created) >= trunc(sysdate - 30) and name=?\n" +
            "group by sysname) b on a.name=b.sysname\n" +
            "where rownum <=?\n" +
            "order by count desc"; */
    public static final String RANKLIST="select a.*,nvl(b.count,0) count from sso_iden a\n" +
        "left join\n" +
        "(select count(1) count,sysname from sso_logininfo \n" +
        "where trunc(created) >= trunc(sysdate - 30) and name=?\n" +
        "group by sysname) b on a.name=b.sysname\n" +
        "where a.pcdisplay = 0\n" +
        "order by nvl(count,0) desc";
    public static final String RANKHANDERLIST="select a.*,nvl(b.count,0) count from sso_iden a\n" +
            "        left join\n" +
            "        (select count(1) count,sysname from sso_logininfo\n" +
            "        where trunc(created) >= trunc(sysdate - 30) and name=?\n" +
            "        group by sysname) b on a.name=b.sysname\n" +
            "        where a.name in ('Aone') and a.pcdisplay=0\n" +
            "        order by nvl(count,0) desc";
    public static final String RANKQISHOULIST="select a.*,nvl(b.count,0) count from sso_iden a\n" +
            "        left join\n" +
            "        (select count(1) count,sysname from sso_logininfo\n" +
            "        where trunc(created) >= trunc(sysdate - 30) and name=?\n" +
            "        group by sysname) b on a.name=b.sysname\n" +
            "        where a.name in ('EHR','Act','PMS') and a.pcdisplay=0\n" +
            "        order by nvl(count,0) desc";
    public static final String PHONERANKLIST="  select a.id,a.name,a.email,a.phonereturnurl returnurl,a.displayname,a.description,a.created,a.updated,a.iconurl,a.phonedisplay,nvl(b.count,0) count from sso_iden a\n" +
            "                    left join\n" +
            "                    (select count(1) count,sysname from sso_logininfo\n" +
            "                    where trunc(created) >= trunc(sysdate - 30) and name=?\n" +
            "                    group by sysname) b on a.name=b.sysname\n" +
            "                    where a.phonedisplay = 0\n" +
            "                    order by nvl(count,0) desc";
    public static final String PHONEQISHOURANKLIST="  select a.id,a.name,a.email,a.phonereturnurl returnurl,a.displayname,a.description,a.created,a.updated,a.iconurl,a.phonedisplay,nvl(b.count,0) count from sso_iden a\n" +
            "                    left join\n" +
            "                    (select count(1) count,sysname from sso_logininfo\n" +
            "                    where trunc(created) >= trunc(sysdate - 30) and name=?\n" +
            "                    group by sysname) b on a.name=b.sysname\n" +
            "                    where a.name in ('EHR','Act','PMS') and a.phonedisplay = 0\n" +
            "                    order by nvl(count,0) desc";
    @Override
    public boolean save() {
        this.set("created",new Date());
        this.set("updated",new Date());
        return super.save();
    }

    public System findByAccesskey(String accesskey){
       return this.findFirst(FINDBYACCESSKEY,accesskey);
    }

    public List<System> findRankList(String username){
        return this.find(RANKLIST,username);
    }
    public List<System> findPhoneRankList(String username){
        return this.find(PHONERANKLIST,username);
    }
    public List<System> findPhoneQSRankList(String username){
        return this.find(PHONEQISHOURANKLIST,username);
    }
    public List<System> findHDRankList(String username){
        return this.find(RANKHANDERLIST,username);
    }

    public List<System> findSysList(){
        return this.find(FINDSYSLIST);
    }
    public List<System> findQSRankList(String username){
        return this.find(RANKQISHOULIST,username);
    }
}
