package me.ele.micservice.utils;

import java.io.*;

/**
 * Created by frankliu on 15/8/26.
 */
public class SerializableKit {

    public static byte[] toByteArray(Serializable obj) {
        byte[] bytes = null;
        ObjectOutputStream oos = null;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                    bytes = baos.toByteArray();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return bytes;
    }

    public static Serializable toObject(byte[] bytes){
        Serializable obj = null;
        ObjectInputStream ois = null;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bais);
            obj = (Serializable) ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return obj;
    }
}
