package me.ele.micservice.models;

import com.jfinal.plugin.activerecord.Model;

import java.util.Date;

/**
 * Created by zhuyuming on 15/8/31.
 */
public class Image extends Model<Image> {
    public static final Image me = new Image();
    // mysql
    // public static final String TABLE = "images";
    // oracle
    public static final String TABLE = "images";
    public static final String ID = "id";

    public Image findByHash(String hash) {
        return findFirst("select * from images where hash = ?", hash);
    }

    public Image setHash(String hash) {
        return set("hash", hash);
    }

    public String getHash() {
        return get("hash");
    }

    public Image setCreated(Date date) {
        return set("created", date);
    }

    public Date getCreated() {
        return get("created");
    }

    @Override
    public boolean save() {
        set("id", "SEQ_IMAGE.nextval");
        return super.save();
    }
}
