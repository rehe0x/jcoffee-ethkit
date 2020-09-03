package com.jcoffee.ethkit.util.license;

import com.jcoffee.ethkit.common.JsonResult;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class PubKeyUtil {
    private static final String kk = "00-00-00-00-00-00-00-E0";

    public static JsonResult getPubKey() {
        ArrayList list = new ArrayList();

        try {
            Enumeration enumeration = NetworkInterface.getNetworkInterfaces();

            label47:
            while(true) {
                StringBuffer stringBuffer;
                byte[] bytes;
                do {
                    NetworkInterface networkInterface;
                    do {
                        if (!enumeration.hasMoreElements()) {
                            break label47;
                        }

                        stringBuffer = new StringBuffer();
                        networkInterface = (NetworkInterface)enumeration.nextElement();
                    } while(networkInterface == null);

                    bytes = networkInterface.getHardwareAddress();
                } while(bytes == null);

                for(int i = 0; i < bytes.length; ++i) {
                    if (i != 0) {
                        stringBuffer.append("-");
                    }

                    int tmp = bytes[i] & 255;
                    String str = Integer.toHexString(tmp);
                    if (str.length() == 1) {
                        stringBuffer.append("0" + str);
                    } else {
                        stringBuffer.append(str);
                    }
                }

                String mm = stringBuffer.toString().toUpperCase();
                if (!StringUtils.equals("00-00-00-00-00-00-00-E0", mm)) {
                    list.add(mm);
                }
            }
        } catch (Exception var8) {
            var8.printStackTrace();
        }

        String plainText = (String)list.stream().distinct().map((e) -> {
            return e;
        }).collect(Collectors.joining(","));
        plainText = com.jcoffee.ethkit.util.license.EncryUtil.encrypt(plainText);
        String message = "公钥:" + plainText;
        JsonResult jsonResult = new JsonResult();
        System.err.println(message);
        jsonResult.setMessage(message);
        jsonResult.setSuccess(true);
        return jsonResult;
    }

    public static Set getAllSet() {
        HashSet set = new HashSet();

        try {
            Enumeration enumeration = NetworkInterface.getNetworkInterfaces();

            while(true) {
                StringBuffer stringBuffer;
                byte[] bytes;
                do {
                    NetworkInterface networkInterface;
                    do {
                        if (!enumeration.hasMoreElements()) {
                            return set;
                        }

                        stringBuffer = new StringBuffer();
                        networkInterface = (NetworkInterface)enumeration.nextElement();
                    } while(networkInterface == null);

                    bytes = networkInterface.getHardwareAddress();
                } while(bytes == null);

                for(int i = 0; i < bytes.length; ++i) {
                    if (i != 0) {
                        stringBuffer.append("-");
                    }

                    int tmp = bytes[i] & 255;
                    String str = Integer.toHexString(tmp);
                    if (str.length() == 1) {
                        stringBuffer.append("0" + str);
                    } else {
                        stringBuffer.append(str);
                    }
                }

                String mac = stringBuffer.toString().toUpperCase();
                if (!StringUtils.equals("00-00-00-00-00-00-00-E0", mac)) {
                    set.add(mac);
                }
            }
        } catch (Exception var8) {
            var8.printStackTrace();
            return set;
        }
    }
}
