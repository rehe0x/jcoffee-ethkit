package com.jcoffee.ethkit.coin.account;

import com.jcoffee.ethkit.coin.pojo.TransactionVo;
import com.jcoffee.ethkit.coin.util.FileUtil;
import com.jcoffee.ethkit.coin.util.TransactionUtil;
import com.jcoffee.ethkit.common.JsonResult;
import com.jcoffee.ethkit.util.license.LicenseUtil;
import com.alibaba.fastjson.JSON;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.utils.Convert;
import org.web3j.utils.Convert.Unit;

@Component
public class TransactionTask {
    @Value("${action.toMany}")
    private String toMany;
    @Value("${action.master}")
    private String master;
    @Value("${action.slave}")
    private String slave;
    @Autowired
    private TransactionUtil transactionUtil;
    @Autowired
    private LicenseUtil licenseUtil;

    public JsonResult startProducer(String from, String to, String pwd, double number, Double gasPrice, Integer gasLimit) {
        String resultFilePath = this.transactionUtil.getRootDir() + this.toMany;
        String message = "";
        JsonResult jsonResult = new JsonResult();
        long startTime = (new Date()).getTime();
        List transactionVos = new ArrayList();
        if (StringUtils.split(from, ",").length != StringUtils.split(to, ",").length) {
            message = "向多个地址转账需要选择相同数量的转出钱包";
            System.err.println(message);
            jsonResult.setMessage(message);
            jsonResult.setSuccess(false);
            return jsonResult;
        } else if (!this.licenseUtil.checkLicense2()) {
            String rootDir = this.transactionUtil.getRootDir();
            message = "软件授权验证未通过，请在【帮助】中获取机器公钥提供给我们，我们给您生成授权文件！";
            System.err.println(message);
            jsonResult.setMessage(message);
            jsonResult.setSuccess(false);
            return jsonResult;
        } else {
            BigInteger value = Convert.toWei(number + "", Unit.ETHER).toBigInteger();
            LinkedBlockingQueue toAddressQueue = new LinkedBlockingQueue(10000);
            int count = 0;
            List addrList = Arrays.asList(StringUtils.split(to, ","));
            int size = addrList.size();
            String toAddress = "";

            try {
                for(int i = 0; i < size; ++i) {
                    toAddress = (String)addrList.get(i);
                    toAddressQueue.put(toAddress);
                }

                String keyFilePath = this.transactionUtil.getRootDir() + this.master;
                String keyFilePathSlave = this.transactionUtil.getRootDir() + this.slave;
                List keyFilePathList = TransactionUtil.getAddrList(keyFilePath);
                List keyFilePathListSlave = TransactionUtil.getAddrList(keyFilePathSlave);
                keyFilePathList.addAll(keyFilePathListSlave);
                String address = null;
                Map keyFileMap = new HashMap();
                Iterator var26 = keyFilePathList.iterator();

                while(var26.hasNext()) {
                    String keyFile = (String)var26.next();
                    address = this.transactionUtil.getAddress(keyFile);
                    if (!StringUtils.isBlank(address)) {
                        keyFileMap.put(address, keyFile);
                    }
                }

                List fromAddrs = Arrays.asList(StringUtils.split(from, ","));
                int fromAddrsSize = fromAddrs.size();
                BigInteger tmpVal = null;
                String keyFile = null;
                Web3j web3j = this.transactionUtil.getWeb3jInstance();
                String content = "";

                for(int i = 0; i < fromAddrsSize; ++i) {
                    String fromAddress = (String)fromAddrs.get(i);
                    keyFile = (String)keyFileMap.get(fromAddress);
                    toAddress = (String)toAddressQueue.take();
                    if (toAddress == null) {
                        break;
                    }

                    String fromAddr = StringUtils.removeEnd(fromAddress, "_pk");
                    String toAddr = StringUtils.removeEnd(toAddress, "_pk");
                    if (StringUtils.equalsIgnoreCase(fromAddr, toAddr)) {
                        System.err.println("转币地址(" + fromAddress + ")与收币地址(" + toAddress + ")不能相同");
                    } else {
                        Credentials credentials = null;

                        try {
                            if (!StringUtils.endsWith(fromAddress, "_pk")) {
                                credentials = WalletUtils.loadCredentials(pwd, keyFile);
                            } else {
                                List list = FileUtil.getContentList(keyFile);
                                if (list.isEmpty()) {
                                    message = "钱包【" + fromAddr + "】私钥文件格式不正确，错误代码：001";
                                    System.err.println(message);
                                    jsonResult.setMessage(message);
                                    jsonResult.setSuccess(false);
                                    return jsonResult;
                                }

                                content = (String)list.get(0);
                                content = StringUtils.replace(content, ".", ":");
                                String[] arr = StringUtils.split(content, ":");
                                if (arr.length != 2) {
                                    message = "钱包【" + fromAddr + "】私钥文件格式不正确，错误代码：002";
                                    System.err.println(message);
                                    jsonResult.setMessage(message);
                                    jsonResult.setSuccess(false);
                                    return jsonResult;
                                }

                                if (!StringUtils.equalsIgnoreCase(arr[0], fromAddr)) {
                                    message = "钱包【" + fromAddr + "】私钥文件格式不正确，错误代码：003";
                                    System.err.println(message);
                                    jsonResult.setMessage(message);
                                    jsonResult.setSuccess(false);
                                    return jsonResult;
                                }

                                credentials = Credentials.create(arr[1]);
                            }
                        } catch (Exception var41) {
                            var41.printStackTrace();
                            String msg = var41.getMessage();
                            if (StringUtils.contains(msg, "Invalid password provided")) {
                                message = "执行失败，钱包【" + fromAddr + "】密码错误," + var41.getMessage();
                            } else {
                                message = "执行失败，钱包【" + fromAddr + "】钱包文件验证未通过," + var41.getMessage();
                            }

                            System.err.println(message);
                            jsonResult.setMessage(message);
                            jsonResult.setSuccess(false);
                            return jsonResult;
                        }

                        TransactionVo tv = new TransactionVo();
                        tv.setFromAddress(fromAddr);
                        tv.setToAddress(toAddr);
                        tv.setToVal(value);
                        tv.setValue(number);
                        this.transactionUtil.setGas(gasPrice, gasLimit, tv);
                        EthGetTransactionCount ethGetTransactionCount = (EthGetTransactionCount)web3j.ethGetTransactionCount(fromAddr, DefaultBlockParameterName.LATEST).sendAsync().get();
                        BigInteger nonce = ethGetTransactionCount.getTransactionCount();
                        this.transactionUtil.sendTransaction(tv, credentials, web3j, nonce);
                        tmpVal = BigInteger.valueOf(Long.valueOf(tv.getGasPrice().longValue() / 1000000000L));
                        tv.setGasPrice(tmpVal);
                        ++count;
                        transactionVos.add(tv);
                        String rs = JSON.toJSONString(tv);
                        System.err.println(rs);
                        FileUtil.writeToTxt(resultFilePath, rs);
                    }
                }

                long curTime = (new Date()).getTime();
                long totalTime = (curTime - startTime) / 1000L;
                message = "转账请求已提交到以太坊网络,具体处理情况需要根据交易hash到etherscan.io查询,数量:" + count + ",耗时:" + totalTime + "秒";
                System.err.println(message);
                jsonResult.setMessage(message);
                jsonResult.setSuccess(true);
                jsonResult.setData(transactionVos);
            } catch (Exception var42) {
                var42.printStackTrace();
                message = "执行失败，" + var42.getMessage();
                System.err.println(message);
                jsonResult.setMessage(message);
                jsonResult.setSuccess(false);
            }

            return jsonResult;
        }
    }
}
