package com.jcoffee.ethkit.util;

import com.jcoffee.ethkit.coin.util.FileUtil;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class IpUtils {
    private static Set whiteSet = new HashSet();
    public static String localHost;

    public static void initWhiteSet() {
        String rootDir = System.getProperty("user.dir");
        rootDir = StringUtils.replace(rootDir, "\\", "/");
        String licensePath = rootDir + "/config/white_list.txt";
        whiteSet = FileUtil.getContentSet2(licensePath);
        whiteSet.add("localhost");
        whiteSet.add("127.0.0.1");
    }

    public static void clearWhiteSet() {
        whiteSet.clear();
    }

    public static Set getWhiteSet() {
        if (whiteSet.isEmpty()) {
            initWhiteSet();
        }

        return whiteSet;
    }

    private IpUtils() {
    }

    public static String getIpAddr() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
            return getIpAddr(request);
        } catch (Exception var1) {
            return "127.0.0.1";
        }
    }

    public static String getIpAddr(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        } else {
            String ip = request.getHeader("x-forwarded-for");
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }

            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Forwarded-For");
            }

            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }

            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Real-IP");
            }

            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }

            return ip;
        }
    }

    public static String getLocalHost() {
        try {
            String hostAddress = Inet4Address.getLocalHost().getHostAddress();
            return hostAddress;
        } catch (UnknownHostException var1) {
            var1.printStackTrace();
            return "localhost";
        }
    }

    static {
        initWhiteSet();
        localHost = getLocalHost();
    }
}
