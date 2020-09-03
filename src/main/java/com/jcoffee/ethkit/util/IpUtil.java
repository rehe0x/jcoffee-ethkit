package com.jcoffee.ethkit.util;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.Proxy.Type;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class IpUtil {
    private static final String domain = "https://api.coinpark.cc";
    private static final ExecutorService producerPool = Executors.newFixedThreadPool(1);
    public static final LinkedBlockingQueue taskQueue = new LinkedBlockingQueue(1000);
    @Autowired
    private ProxyXiongMao proxyXiongMao;

    public static void main(String[] args) throws Exception {
        IpUtil ipUtil = new IpUtil();
        RestTemplate restTemplate = ipUtil.getRestTemplate(false);
        String cookies = "__cfduid=d87d823343098c179625923cfb339b20c1530025833; session_id=6bd4831e110a6aca047e4724cf88f8bad5991961; Hm_lvt_b2901b172e2b998b35f14c68572e57ea=1530025845,1530457147; Hm_lpvt_b2901b172e2b998b35f14c68572e57ea=1530572724; users=";
        String url = "https://api.coinpark.cc/v1/orderpending";
        MultiValueMap params = new LinkedMultiValueMap();
        String cmds = "";
        cmds = "%5B%7B%22cmd%22%3A%22user%2FuserInfo%22%2C%22body%22%3A%7B%7D%7D%5D";
        params.add("cmds", cmds);
        cookies = "__cfduid=d7deae0764e113acc4dea66abedb8759d1530523136; _ga=GA1.2.1594533598.1530523146; _gid=GA1.2.640227990.1530523146; i18next=zh_Hans_CN; token=8D501B3761554428870EC36E8869D338; 173163_hideAssets=true; _gat=1";
        url = "https://www.coinex.com/res/user/";
        excuteUrl(url, cookies, (MultiValueMap)null, restTemplate, HttpMethod.GET);
    }

    public RestTemplate getRestTemplate(boolean isUseProxy) {
        RestTemplate restTemplate = null;
        if (isUseProxy) {
            List ipList = this.proxyXiongMao.startGet(1);
            String ipPort = (String)ipList.get(0);
            restTemplate = new RestTemplate(this.getHttpClientFactory(ipPort));
        } else {
            restTemplate = new RestTemplate(new HttpsClientRequestFactory());
        }

        return restTemplate;
    }

    private static String post(String url, String cookies, MultiValueMap params, RestTemplate restTemplate) {
        String rString = "";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("content-type", "application/x-www-form-urlencoded");
            List cookieList = Arrays.asList(StringUtils.split(cookies, ";"));
            headers.add("cookie", cookies);
            headers.add("origin", "https://www.coinpark.cc");
            HttpEntity requestEntity = new HttpEntity(params, headers);
            ResponseEntity rss = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class, new Object[0]);
            rString = (String)rss.getBody();
        } catch (Exception var9) {
            var9.printStackTrace();
        }

        return rString;
    }

    private static String get(String url, String cookies, RestTemplate restTemplate) {
        String rString = "";

        try {
            HttpHeaders headers = new HttpHeaders();
            List cookieList = Arrays.asList(StringUtils.split(cookies, ";"));
            headers.put("Cookie", cookieList);
            HttpEntity requestEntity = new HttpEntity((Object)null, headers);
            ResponseEntity rss = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class, new Object[0]);
            rString = (String)rss.getBody();
            System.err.println(rString);
        } catch (Exception var8) {
            var8.printStackTrace();
        }

        return rString;
    }

    public static String excuteUrl(String url, String cookies, MultiValueMap params, RestTemplate restTemplate, HttpMethod httpMethod) {
        String rString = "";

        try {
            HttpHeaders headers = new HttpHeaders();
            if (httpMethod == HttpMethod.POST) {
                headers.add("content-type", "application/x-www-form-urlencoded");
            }

            headers.add("cookie", cookies);
            HttpEntity requestEntity = new HttpEntity(params, headers);
            ResponseEntity rss = restTemplate.exchange(url, httpMethod, requestEntity, String.class, new Object[0]);
            rString = (String)rss.getBody();
            System.err.println(rString);
        } catch (Exception var9) {
            var9.printStackTrace();
        }

        return rString;
    }

    private HttpsClientRequestFactory getHttpClientFactory(String ip_port) {
        System.err.println("ip_port:" + ip_port);
        String[] arr = StringUtils.split(ip_port, ":");
        String host = arr[0];
        Integer port = Integer.valueOf(arr[1]);
        HttpsClientRequestFactory httpRequestFactory = new HttpsClientRequestFactory();
        httpRequestFactory.setReadTimeout(15000);
        httpRequestFactory.setConnectTimeout(5000);
        SocketAddress address = new InetSocketAddress(host, port);
        Proxy proxy = new Proxy(Type.HTTP, address);
        httpRequestFactory.setProxy(proxy);
        return httpRequestFactory;
    }
}
