package com.jcoffee.ethkit.coin.util;

import com.jcoffee.ethkit.coin.pojo.ContractInfo;
import com.jcoffee.ethkit.common.JsonResult;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Component
public class EtherscanUtilBak {
    private static final String ETHERSCAN_API = "https://api.etherscan.io/api?module=account&action=balancemulti&address={0}&tag=latest&apikey=CR93A3EVF4W2KAEBEAUVRQD4CQVBAICYA8";
    private static final String ETHERSCAN_CONTRACT_API = "https://etherscan.io/readcontract?a={0}";
    private static final String ETHERSCAN_READ_CONTRACT_API = "https://etherscan.io/token/{0}";
    @Autowired
    private TransactionUtil transactionUtil;

    public static Map getBalance(List accountList) {
        HashMap map = new HashMap();

        try {
            int size = accountList.size();
            int count = 1;
            String accountStr = "";
            String tmpAccount = "";

            for(int j = 0; j < size; ++j) {
                tmpAccount = StringUtils.removeEnd((String)accountList.get(j), "_pk");
                accountStr = accountStr + "," + tmpAccount;
                if (count % 20 == 0) {
                    accountStr = StringUtils.removeStart(accountStr, ",");
                    excuteGetBalance(accountStr, map);
                    accountStr = "";
                }

                ++count;
            }

            if (StringUtils.isNotBlank(accountStr)) {
                accountStr = StringUtils.removeStart(accountStr, ",");
                excuteGetBalance(accountStr, map);
            }
        } catch (Exception var7) {
            var7.printStackTrace();
        }

        return map;
    }

    private static void excuteGetBalance(String accountStr, Map map) throws Exception {
        String url = MessageFormat.format("https://api.etherscan.io/api?module=account&action=balancemulti&address={0}&tag=latest&apikey=CR93A3EVF4W2KAEBEAUVRQD4CQVBAICYA8", accountStr);
        String jsonStr = get(url);
        if (StringUtils.startsWith(jsonStr, "{")) {
            JSONObject jsb = JSON.parseObject(jsonStr);
            Integer status = jsb.getInteger("status");
            if (status == 1) {
                JSONArray data = jsb.getJSONArray("result");
                String account = null;
                String balance = null;
                BigDecimal balanceN = null;

                for(int i = 0; i < data.size(); ++i) {
                    JSONObject job = data.getJSONObject(i);
                    account = job.getString("account");
                    balance = job.getString("balance");
                    if (StringUtils.equals(balance, "0")) {
                        balanceN = new BigDecimal("0");
                        map.put(account, balanceN);
                    } else {
                        balanceN = new BigDecimal(balance);
                        BigDecimal nbalance = balanceN.divide(new BigDecimal("1000000000000000000"), 10, 1);
                        map.put(account, nbalance);
                    }
                }

            }
        }
    }

    public static String get(String url) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10000).setSocketTimeout(10000).setConnectionRequestTimeout(5000).build();
        httpGet.setConfig(requestConfig);
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:50.0) Gecko/20100101 Firefox/50.0");
        CloseableHttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        String content = EntityUtils.toString(entity, "utf-8");
        content = StringUtils.trim(content);
        response.close();
        httpClient.close();
        return content;
    }

    public List getContractList() {
        String contractFile = this.transactionUtil.getRootDir() + "/config/tokenContract.txt";
        List contractInfoList = new ArrayList();
        List list = FileUtil.getContentList(contractFile);
        ContractInfo eth = new ContractInfo();
        eth.setAddress("ETH");
        eth.setSymbol("ETH");
        contractInfoList.add(eth);
        Iterator var5 = list.iterator();

        while(var5.hasNext()) {
            String contract = (String)var5.next();
            if (StringUtils.startsWith(contract, "{")) {
                ContractInfo item = (ContractInfo)JSON.parseObject(contract, ContractInfo.class);
                item.setSymbol(item.getSymbol() + "(" + item.getAddress() + ")");
                item.setAddress(item.getAddress() + "_" + item.getDecimals());
                contractInfoList.add(item);
            }
        }

        return contractInfoList;
    }

    public ContractInfo getErc20ContractInfo(String contractAddress) {
        ContractInfo contractInfo = null;

        try {
            Map map = new HashMap();
            String contractFile = this.transactionUtil.getRootDir() + "/config/tokenContract.txt";
            List list = FileUtil.getContentList(contractFile);
            Iterator var6 = list.iterator();

            while(var6.hasNext()) {
                String contract = (String)var6.next();
                if (StringUtils.startsWith(contract, "{")) {
                    ContractInfo item = (ContractInfo)JSON.parseObject(contract, ContractInfo.class);
                    map.put(item.getAddress(), item);
                }
            }

            contractInfo = (ContractInfo)map.get(contractAddress);
            if (contractInfo == null) {
                String token = this.transactionUtil.getToken();
                Web3j web3j = Web3j.build(new HttpService("https://mainnet.infura.io/v3/" + token));
                int decimals = TokenClient.getTokenDecimals(web3j, contractAddress);
                if (decimals == -1) {
                    System.err.println("查询地址【" + contractAddress + "】的智能合约信息失败");
                }

                String symbol = TokenClient.getTokenSymbol(web3j, contractAddress);
                String tokenName = TokenClient.getTokenName(web3j, contractAddress);
                contractInfo = new ContractInfo();
                contractInfo.setSymbol(symbol);
                contractInfo.setDecimals(decimals);
                contractInfo.setName(tokenName);
                contractInfo.setAddress(contractAddress);
                String json = JSON.toJSONString(contractInfo);
                FileUtil.writeToTxt(contractFile, json);
            }
        } catch (Exception var12) {
            var12.printStackTrace();
            System.err.println("查询地址【" + contractAddress + "】的智能合约信息失败");
        }

        return contractInfo;
    }

    public synchronized JsonResult addErc20ContractInfo(String contractAddress) {
        contractAddress = StringUtils.trim(contractAddress);
        contractAddress = StringUtils.lowerCase(contractAddress);
        JsonResult jsonResult = new JsonResult();
        String message = "";
        boolean success = true;
        ContractInfo contractInfo = null;

        try {
            Map map = new HashMap();
            String contractFile = this.transactionUtil.getRootDir() + "/config/tokenContract.txt";
            List list = FileUtil.getContentList(contractFile);
            Iterator var9 = list.iterator();

            while(var9.hasNext()) {
                String contract = (String)var9.next();
                if (StringUtils.startsWith(contract, "{")) {
                    ContractInfo item = (ContractInfo)JSON.parseObject(contract, ContractInfo.class);
                    map.put(item.getAddress(), item);
                }
            }

            contractInfo = (ContractInfo)map.get(contractAddress);
            if (contractInfo != null) {
                success = false;
                message = "代币【" + contractInfo.getSymbol() + "】已经添加过，无需重复添加";
                jsonResult.setMessage(message);
                jsonResult.setSuccess(success);
                return jsonResult;
            }

            if (contractInfo == null) {
                String token = this.transactionUtil.getToken();
                Web3j web3j = Web3j.build(new HttpService("https://mainnet.infura.io/v3/" + token));
                int decimals = TokenClient.getTokenDecimals(web3j, contractAddress);
                if (decimals == -1) {
                    success = false;
                    message = "查询地址【" + contractAddress + "】的智能合约信息失败";
                    System.err.println(message);
                    jsonResult.setMessage(message);
                    jsonResult.setSuccess(success);
                    return jsonResult;
                }

                String symbol = TokenClient.getTokenSymbol(web3j, contractAddress);
                String tokenName = TokenClient.getTokenName(web3j, contractAddress);
                contractInfo = new ContractInfo();
                contractInfo.setSymbol(symbol);
                contractInfo.setDecimals(decimals);
                contractInfo.setName(tokenName);
                contractInfo.setAddress(contractAddress);
                String json = JSON.toJSONString(contractInfo);
                FileUtil.writeToTxt(contractFile, json);
            }
        } catch (Exception var15) {
            var15.printStackTrace();
            success = false;
            message = "查询地址【" + contractAddress + "】的智能合约信息失败";
            System.err.println(message);
            jsonResult.setMessage(message);
            jsonResult.setSuccess(success);
            return jsonResult;
        }

        message = "添加代币成功，代币符号【" + contractInfo.getSymbol() + "】";
        System.err.println(message);
        jsonResult.setMessage(message);
        jsonResult.setSuccess(success);
        return jsonResult;
    }

    public static void main(String[] args) throws Exception {
        String account = "0x4ccc3759eb48faf1c6cfadad2619e7038db6b212";
        ContractInfo info = (new EtherscanUtilBak()).getErc20ContractInfo(account);
        System.err.println(JSON.toJSONString(info));
    }
}
