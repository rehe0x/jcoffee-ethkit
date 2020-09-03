package com.jcoffee.ethkit.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProxyXiongMao {
    @Value("${action.xiongmao}")
    private String xiongmao;

    public static void main(String[] args) {
        (new ProxyXiongMao()).startGet(1);
    }

    public List startGet(int wantedNumber) {
        String url = "http://www.xiongmaodaili.com/xiongmao-web/api/glip?" + this.xiongmao + "&count=" + wantedNumber;
        ArrayList list = new ArrayList();

        try {
            String text = this.getJson(url);
            System.err.println(text);
            JSONObject jsonObject = JSON.parseObject(text);
            JSONArray jsonArray = (JSONArray)jsonObject.get("obj");
            if (jsonArray.size() > 0) {
                for(int i = 0; i < jsonArray.size(); ++i) {
                    JSONObject job = jsonArray.getJSONObject(i);
                    String ip = (String)job.get("ip");
                    int port = Integer.valueOf((String)job.get("port"));
                    if (this.checkProxy(ip, port)) {
                        System.err.println(ip + ":" + port);
                        list.add(ip + ":" + port);
                    }
                }
            }
        } catch (Exception var11) {
            var11.printStackTrace();
        }

        return list;
    }

    private String getJson(String url) throws Exception {
        String rs = "";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:50.0) Gecko/20100101 Firefox/50.0");
        CloseableHttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        rs = EntityUtils.toString(entity, "utf-8");
        response.close();
        httpClient.close();
        return rs;
    }

    public boolean checkProxy(String ip, Integer port) {
        try {
            Jsoup.connect("https://www.baidu.com").timeout(2000).proxy(ip, port).get();
            return true;
        } catch (Exception var4) {
            return false;
        }
    }
}
