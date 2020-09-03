package com.jcoffee.ethkit.interceptor;

import com.jcoffee.ethkit.util.IpUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String origin = request.getHeader("Origin");
        String domain = request.getServerName();
        String ip = IpUtils.getIpAddr(request);
        if (!IpUtils.getWhiteSet().contains(domain) && !IpUtils.getWhiteSet().contains(ip)) {
            System.err.println("preHandle--->ip====" + ip);
            System.err.println("preHandle--->domain====" + domain);
            return false;
        } else {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Allow", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
            response.addHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, PATCH");
            response.addHeader("Access-Control-Allow-Headers", "api-key, authorization, Origin, No-Cache, X-Requested-With, If-Modified-Since, Pragma, Last-Modified, Cache-Control, Expires, Content-Type, X-E4M-With");

            try {
                return true;
            } catch (Exception var8) {
                System.err.println(var8.getMessage());
                return false;
            }
        }
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }
}
