package com.jcoffee.ethkit.util;

import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.springframework.util.StringUtils;

public class WebDateUtils {
    private static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String getWebsiteDatetime(String webuUrl, String format) {
        try {
            if (!StringUtils.isEmpty(webuUrl)) {
                URL url = new URL(webuUrl);
                URLConnection uc = url.openConnection();
                uc.connect();
                long ld = uc.getDate();
                Date date = new Date(ld);
                SimpleDateFormat sdf = new SimpleDateFormat(format != null ? format : "yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                return sdf.format(date);
            }

            System.out.println("URL Error!!!");
        } catch (Exception var8) {
            var8.printStackTrace();
        }

        return null;
    }

    public static void main(String[] args) {
    }
}
