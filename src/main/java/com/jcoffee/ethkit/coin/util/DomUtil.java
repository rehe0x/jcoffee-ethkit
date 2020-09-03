package com.jcoffee.ethkit.coin.util;

import com.jcoffee.ethkit.coin.pojo.ContractInfo;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class DomUtil {
    public static List parse(String html) {
        List list = new ArrayList();
        Document doc = Jsoup.parse(html);
        Elements addrs = doc.select("a[class].address-tag");
        Iterator var4 = addrs.iterator();

        while(var4.hasNext()) {
            Element userName = (Element)var4.next();
            String attr = userName.text();
            System.err.println(attr);
            list.add(attr);
        }

        return list;
    }

    public static ContractInfo getContractInfo(String html) {
        ContractInfo contractInfo = new ContractInfo();
        new ArrayList();
        Document doc = Jsoup.parse(html);
        Elements tables = doc.select("table");
        String name = "";
        int count = 0;
        Iterator var7 = tables.iterator();

        while(var7.hasNext()) {
            Element table = (Element)var7.next();
            Elements tds = table.select("td");
            Iterator var10 = tds.iterator();

            while(var10.hasNext()) {
                Element td = (Element)var10.next();
                String tdHtml = td.html();
                if (StringUtils.contains(tdHtml, "name &nbsp;")) {
                    name = StringUtils.substringAfter(tdHtml, "<i class=\"fa fa-long-arrow-right\"></i>");
                    name = StringUtils.substringBefore(name, "<i><font color=\"silver\">");
                    name = StringUtils.trim(name);
                    contractInfo.setName(name);
                    ++count;
                } else {
                    String symbol;
                    if (StringUtils.contains(tdHtml, "totalSupply &nbsp;")) {
                        symbol = StringUtils.substringAfter(tdHtml, "<i class=\"fa fa-long-arrow-right\"></i>");
                        symbol = StringUtils.substringBefore(symbol, "<i><font color=\"silver\">");
                        symbol = StringUtils.trim(symbol);
                        contractInfo.setTotalSupply(symbol);
                        ++count;
                    } else if (StringUtils.contains(tdHtml, "decimals &nbsp;")) {
                        symbol = StringUtils.substringAfter(tdHtml, "<i class=\"fa fa-long-arrow-right\"></i>");
                        symbol = StringUtils.substringBefore(symbol, "<i><font color=\"silver\">");
                        symbol = StringUtils.trim(symbol);
                        contractInfo.setDecimals(Integer.valueOf(symbol));
                        if (StringUtils.isBlank(symbol)) {
                            return null;
                        }

                        ++count;
                    } else if (StringUtils.contains(tdHtml, "symbol &nbsp;")) {
                        symbol = StringUtils.substringAfter(tdHtml, "<i class=\"fa fa-long-arrow-right\"></i>");
                        symbol = StringUtils.substringBefore(symbol, "<i><font color=\"silver\">");
                        symbol = StringUtils.trim(symbol);
                        contractInfo.setSymbol(symbol);
                        if (StringUtils.isBlank(symbol)) {
                            return null;
                        }

                        ++count;
                    }
                }
            }
        }

        if (count != 4) {
            return null;
        } else {
            return contractInfo;
        }
    }

    public static ContractInfo getContractInfo2(String html) {
        ContractInfo contractInfo = new ContractInfo();
        new ArrayList();
        Document doc = Jsoup.parse(html);
        Elements tables = doc.select("table[class].table");
        String name = "";
        String decimals = "";
        int count = 0;
        String content = "";
        Iterator var9 = tables.iterator();

        while(var9.hasNext()) {
            Element table = (Element)var9.next();
            Elements tds = table.select("td");
            int idx = 0;

            for(Iterator var13 = tds.iterator(); var13.hasNext(); ++idx) {
                Element td = (Element)var13.next();
                if (count == 2) {
                    break;
                }

                content = td.text();
                if (!StringUtils.contains(content, ".") && !StringUtils.contains(content, ",")) {
                    if (StringUtils.contains(content, "Decimals:")) {
                        decimals = StringUtils.trim(((Element)tds.get(idx + 1)).text());
                        contractInfo.setDecimals(Integer.valueOf(decimals));
                        ++count;
                    }
                } else {
                    name = StringUtils.substringAfter(content, " ");
                    name = StringUtils.trim(name);
                    contractInfo.setName(name);
                    contractInfo.setSymbol(name);
                    ++count;
                }
            }
        }

        if (count != 2) {
            return null;
        } else {
            return contractInfo;
        }
    }
}
