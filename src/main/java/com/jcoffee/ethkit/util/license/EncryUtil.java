package com.jcoffee.ethkit.util.license;

public class EncryUtil {
    public static final String key = "xxddxx";
    public static final String message = "1.x为基础版本不支持向多个外部地址转账，如有需要请购买2.x版本";

    public static String encrypt(String plainText) {
        try {
            return (new DES("xxddxx")).encrypt(plainText);
        } catch (Exception var2) {
            return null;
        }
    }

    public static String decrypt(String plainText, String key) {
        try {
            return (new DES(key)).decrypt(plainText);
        } catch (Exception var3) {
            return null;
        }
    }
}
