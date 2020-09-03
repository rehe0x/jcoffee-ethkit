package com.jcoffee.ethkit.util;

import java.security.MessageDigest;

public class Md5Util {
    public static String md5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte[] b = md.digest();
            StringBuffer buf = new StringBuffer("");

            for(int offset = 0; offset < b.length; ++offset) {
                int i = b[offset];
                if (i < 0) {
                    i += 256;
                }

                if (i < 16) {
                    buf.append("0");
                }

                buf.append(Integer.toHexString(i));
            }

            str = buf.toString();
        } catch (Exception var6) {
            var6.printStackTrace();
        }

        return str;
    }

    public static String getMySQLPasswordBefore(String password) {
        long nr = 1345345333L;
        long add = 7L;
        long nr2 = 305419889L;
        long tmp = 0L;

        for(int i = 0; i < password.length(); ++i) {
            tmp = (long)password.charAt(i);
            if (tmp != 32L && tmp != 9L) {
                nr ^= ((nr & 63L) + add) * tmp + (nr << 8);
                nr2 += nr2 << 8 ^ nr;
                add += tmp;
            }
        }

        long result_1 = nr & 2147483647L;
        long result_2 = nr2 & 2147483647L;
        String str1 = Long.toHexString(result_1);
        String str2 = Long.toHexString(result_2);
        return str1.concat(str2);
    }

    public static String encodeStr(String password) {
        return md5(password);
    }

    public static void main(String[] args) {
        String content = "apikey=B1F314C73891886BA74625E406AF6D05&symbol=btc_usdt&since=10";
        String md5 = encodeStr(content);
        System.err.println(md5);
    }
}
