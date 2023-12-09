package net.micode.notes.tool;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Calc {
    /**
     * Java中的md5
     * @param content 输入的值
     * @return 输出md5加密后的值
     */
    public static String md5Java(String content) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(content.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithmException", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) {
                hex.append(0);
            }
            hex.append(Integer.toHexString(b & 0xff));
        }

        return hex.toString();
    }
}
