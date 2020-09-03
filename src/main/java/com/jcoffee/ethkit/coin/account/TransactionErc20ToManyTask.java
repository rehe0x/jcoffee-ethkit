package com.jcoffee.ethkit.coin.account;

import com.jcoffee.ethkit.coin.pojo.TransactionErc20Vo;
import com.jcoffee.ethkit.coin.util.FileUtil;
import com.jcoffee.ethkit.coin.util.TokenClient;
import com.jcoffee.ethkit.coin.util.TransErc20Util;
import com.jcoffee.ethkit.coin.util.TransactionUtil;
import com.jcoffee.ethkit.common.JsonResult;
import com.jcoffee.ethkit.util.license.LicenseUtil;
import com.alibaba.fastjson.JSON;
import java.io.File;
import java.math.BigDecimal;
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
import org.web3j.protocol.http.HttpService;

@Component
public class TransactionErc20ToManyTask {
    @Value("${action.erc20}")
    private String erc20;
    @Value("${action.toMany}")
    private String toMany;
    @Value("${action.master}")
    private String master;
    @Value("${action.slave}")
    private String slave;
    @Autowired
    private TransErc20Util transErc20Util;
    @Autowired
    private TransactionUtil transactionUtil;
    @Autowired
    private LicenseUtil licenseUtil;

    public JsonResult one2many(String coin, String from, String toAddress, String pwd, double number, Double gasPrice, Integer gasLimit, Integer numType) {
        String resultFilePath = this.transactionUtil.getRootDir() + this.toMany;
        String message = "";
        JsonResult jsonResult = new JsonResult();
        long startTime = (new Date()).getTime();
        List transactionVos = new ArrayList();
        String key2 = "xxddxx";
        if (!StringUtils.contains(key2, "20")) {
            message = "1.x为基础版本不支持向多个外部地址转账，如有需要请购买2.x版本";
            System.err.println(message);
            jsonResult.setMessage(message);
            jsonResult.setSuccess(false);
            return jsonResult;
        } else {
            String rootDir = this.transactionUtil.getRootDir();
            if (!this.licenseUtil.checkLicense2()) {
                message = "软件授权验证未通过，请在【帮助】中获取机器公钥提供给我们，我们给您生成授权文件！";
                System.err.println(message);
                jsonResult.setMessage(message);
                jsonResult.setSuccess(false);
                return jsonResult;
            } else {
                String erc20DirPath = rootDir + this.erc20;
                File erc20Dir = new File(erc20DirPath);
                if (!erc20Dir.exists()) {
                    erc20Dir.mkdirs();
                }

                String[] coinArr = StringUtils.split(coin, "_");
                String contract = coinArr[0];
                int decimals = Integer.valueOf(coinArr[1]);
                String tmpStr = "1";

                for(int i = 0; i < decimals; ++i) {
                    tmpStr = tmpStr + "0";
                }

                LinkedBlockingQueue toAddressQueue = new LinkedBlockingQueue(10000);
                int count = 0;
                List toAddressList = Arrays.asList(StringUtils.split(toAddress, ","));
                int size = toAddressList.size();

                try {
                    for(int i = 0; i < size; ++i) {
                        toAddress = (String)toAddressList.get(i);
                        boolean isAddr = WalletUtils.isValidAddress(toAddress);
                        if (!isAddr) {
                            message = "【" + isAddr + "】不是合法的钱包地址";
                            System.err.println(message);
                            jsonResult.setMessage(message);
                            jsonResult.setSuccess(false);
                            return jsonResult;
                        }

                        toAddressQueue.put(toAddress);
                    }

                    String keyFilePath = this.transactionUtil.getRootDir() + this.master;
                    String keyFilePathSlave = this.transactionUtil.getRootDir() + this.slave;
                    List keyFilePathList = TransactionUtil.getAddrList(keyFilePath);
                    List keyFilePathListSlave = TransactionUtil.getAddrList(keyFilePathSlave);
                    String address = null;
                    Map keyFileMap = new HashMap();
                    Iterator tmpGasPriceVal = keyFilePathList.iterator();

                    String keyFile;
                    while(tmpGasPriceVal.hasNext()) {
                        keyFile = (String)tmpGasPriceVal.next();
                        address = this.transactionUtil.getAddress(keyFile);
                        if (!StringUtils.isBlank(address)) {
                            keyFileMap.put(address, keyFile);
                        }
                    }

                    tmpGasPriceVal = keyFilePathListSlave.iterator();

                    while(tmpGasPriceVal.hasNext()) {
                        keyFile = (String)tmpGasPriceVal.next();
                        address = this.transactionUtil.getAddress(keyFile);
                        if (!StringUtils.isBlank(address)) {
                            keyFileMap.put(address, keyFile);
                        }
                    }

                    tmpGasPriceVal = null;
                    keyFile = null;
                    Web3j web3j = this.transactionUtil.getWeb3jInstance();
                    EthGetTransactionCount ethGetTransactionCount = (EthGetTransactionCount)web3j.ethGetTransactionCount(StringUtils.removeEnd(from, "_pk"), DefaultBlockParameterName.LATEST).sendAsync().get();
                    BigInteger nonce = ethGetTransactionCount.getTransactionCount();
                    long idx = nonce.longValue();
                    String content = "";

                    for(int i = 0; i < toAddressList.size(); ++i) {
                        System.err.println("idx====" + idx);
                        keyFile = (String)keyFileMap.get(from);
                        toAddress = (String)toAddressQueue.take();
                        if (toAddress == null) {
                            break;
                        }

                        String fromAddr = StringUtils.removeEnd(from, "_pk");
                        String toAddr = StringUtils.removeEnd(toAddress, "_pk");
                        if (StringUtils.equalsIgnoreCase(fromAddr, toAddr)) {
                            System.err.println("转币地址(" + fromAddr + ")与收币地址(" + toAddress + ")不能相同");
                        } else {
                            Credentials credentials = null;

                            String rs;
                            try {
                                if (!StringUtils.endsWith(from, "_pk")) {
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
                            } catch (Exception var51) {
                                var51.printStackTrace();
                                rs = var51.getMessage();
                                if (StringUtils.contains(rs, "Invalid password provided")) {
                                    message = "执行失败，钱包【" + fromAddr + "】密码错误," + var51.getMessage();
                                } else {
                                    message = "执行失败，钱包【" + fromAddr + "】钱包文件验证未通过," + var51.getMessage();
                                }

                                System.err.println(message);
                                jsonResult.setMessage(message);
                                jsonResult.setSuccess(false);
                                return jsonResult;
                            }

                            TransactionErc20Vo tv = new TransactionErc20Vo();
                            tv.setContractAddress(contract);
                            tv.setFromAddress(fromAddr);
                            tv.setToAddress(toAddress);
                            if (StringUtils.equalsIgnoreCase(fromAddr, toAddress)) {
                                System.err.println("转币地址(" + fromAddr + ")与收币地址(" + toAddress + ")不能相同");
                                tv.setSuccess(false);
                                tv.setMsg("转币地址(" + fromAddr + ")与收币地址(" + toAddress + ")不能相同");
                            } else {
                                BigInteger Erc20Value = TokenClient.getTokenBalance(web3j, fromAddr, contract);
                                if (Erc20Value == null) {
                                    tv.setSuccess(false);
                                    tv.setMsg("转账失败，查询代币余额超时");
                                    System.err.println("从地址【" + fromAddr + "】转出代币失败，查询余额超时");
                                } else if (Erc20Value.intValue() == 0) {
                                    tv.setSuccess(false);
                                    tv.setMsg("要转出的代币余额为0，无需转账");
                                    System.err.println("从地址【" + fromAddr + "】转出代币失败，余额为0，无需转账");
                                } else {
                                    BigDecimal weiFactor;
                                    BigDecimal dBigDecimal;
                                    if (numType == 1) {
                                        tv.setValue(number);
                                        weiFactor = BigDecimal.TEN.pow(decimals);
                                        dBigDecimal = (new BigDecimal(number + "")).multiply(weiFactor);
                                        BigInteger value = dBigDecimal.toBigInteger();
                                        tv.setToVal(value);
                                    } else {
                                        tv.setToVal(Erc20Value);
                                        weiFactor = new BigDecimal(Erc20Value);
                                        dBigDecimal = weiFactor.divide(new BigDecimal(tmpStr), 10, 1);
                                        tv.setValue(dBigDecimal.doubleValue());
                                    }

                                    tv.setGasPrice(BigInteger.valueOf(Long.valueOf(tv.getGasPrice().longValue() / 1000000000L)));
                                    nonce = BigInteger.valueOf(idx);
                                    this.transactionUtil.setGas(gasPrice, gasLimit, tv);
                                    this.transErc20Util.sendTransaction(tv, credentials, web3j, nonce);
                                    ++count;
                                    ++idx;
                                }
                            }

                            transactionVos.add(tv);
                            rs = JSON.toJSONString(tv);
                            System.err.println(rs);
                            ++count;
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
                } catch (Exception var52) {
                    var52.printStackTrace();
                    message = "执行失败，" + var52.getMessage();
                    System.err.println(message);
                    jsonResult.setMessage(message);
                    jsonResult.setSuccess(false);
                }

                return jsonResult;
            }
        }
    }

    public JsonResult many2many(String coin, String fromAddresss, String toAddress, String pwd, double number, Double gasPrice, Integer gasLimit, Integer numType) {
        String rootDir = this.transactionUtil.getRootDir();
        String resultFilePath = this.transactionUtil.getRootDir() + this.toMany;
        String message = "";
        JsonResult jsonResult = new JsonResult();
        long startTime = (new Date()).getTime();
        List transactionVos = new ArrayList();
        String key2 = "xxddxx";
        if (!StringUtils.contains(key2, "20")) {
            message = "1.x为基础版本不支持向多个外部地址转账，如有需要请购买2.x版本";
            System.err.println(message);
            jsonResult.setMessage(message);
            jsonResult.setSuccess(false);
            return jsonResult;
        } else if (!this.licenseUtil.checkLicense2()) {
            message = "软件授权验证未通过，请在【帮助】中获取机器公钥提供给我们，我们给您生成授权文件！";
            System.err.println(message);
            jsonResult.setMessage(message);
            jsonResult.setSuccess(false);
            return jsonResult;
        } else if (StringUtils.split(fromAddresss, ",").length != StringUtils.split(toAddress, ",").length) {
            message = "向多个地址转账需要选择相同数量的转出钱包";
            System.err.println(message);
            jsonResult.setMessage(message);
            jsonResult.setSuccess(false);
            return jsonResult;
        } else {
            String erc20DirPath = rootDir + this.erc20;
            File erc20Dir = new File(erc20DirPath);
            if (!erc20Dir.exists()) {
                erc20Dir.mkdirs();
            }

            String[] coinArr = StringUtils.split(coin, "_");
            String contract = coinArr[0];
            int decimals = Integer.valueOf(coinArr[1]);
            String tmpStr = "1";

            for(int i = 0; i < decimals; ++i) {
                tmpStr = tmpStr + "0";
            }

            LinkedBlockingQueue toAddressQueue = new LinkedBlockingQueue(10000);
            int count = 0;
            List toAddressList = Arrays.asList(StringUtils.split(toAddress, ","));
            int size = toAddressList.size();

            try {
                for(int i = 0; i < size; ++i) {
                    toAddress = (String)toAddressList.get(i);
                    boolean isAddr = WalletUtils.isValidAddress(toAddress);
                    if (!isAddr) {
                        message = "【" + isAddr + "】不是合法的钱包地址";
                        System.err.println(message);
                        jsonResult.setMessage(message);
                        jsonResult.setSuccess(false);
                        return jsonResult;
                    }

                    toAddressQueue.put(toAddress);
                }

                String keyFilePath = this.transactionUtil.getRootDir() + this.master;
                String keyFilePathSlave = this.transactionUtil.getRootDir() + this.slave;
                List keyFilePathList = TransactionUtil.getAddrList(keyFilePath);
                List keyFilePathListSlave = TransactionUtil.getAddrList(keyFilePathSlave);
                String address = null;
                Map keyFileMap = new HashMap();
                Iterator var34 = keyFilePathList.iterator();

                String keyFile;
                while(var34.hasNext()) {
                    keyFile = (String)var34.next();
                    address = this.transactionUtil.getAddress(keyFile);
                    if (!StringUtils.isBlank(address)) {
                        keyFileMap.put(address, keyFile);
                    }
                }

                var34 = keyFilePathListSlave.iterator();

                while(var34.hasNext()) {
                    keyFile = (String)var34.next();
                    address = this.transactionUtil.getAddress(keyFile);
                    if (!StringUtils.isBlank(address)) {
                        keyFileMap.put(address, keyFile);
                    }
                }

                List fromAddrs = Arrays.asList(StringUtils.split(fromAddresss, ","));
                int fromAddrsSize = fromAddrs.size();
                String keyFileStr = null;
                String token = this.transactionUtil.getToken();
                Web3j web3j = Web3j.build(new HttpService("https://mainnet.infura.io/v3/" + token));
                String content = "";

                for(int i = 0; i < fromAddrsSize; ++i) {
                    String fromAddress = (String)fromAddrs.get(i);
                    keyFileStr = (String)keyFileMap.get(fromAddress);
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

                        String rs;
                        try {
                            if (!StringUtils.endsWith(fromAddress, "_pk")) {
                                credentials = WalletUtils.loadCredentials(pwd, keyFileStr);
                                BigInteger pk = credentials.getEcKeyPair().getPrivateKey();
                                System.err.println(fromAddr + "." + pk);
                                System.err.println(fromAddr + "." + credentials.getEcKeyPair().getPublicKey());
                            } else {
                                List list = FileUtil.getContentList(keyFileStr);
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
                        } catch (Exception var50) {
                            var50.printStackTrace();
                            rs = var50.getMessage();
                            if (StringUtils.contains(rs, "Invalid password provided")) {
                                message = "执行失败，钱包【" + fromAddr + "】密码错误," + var50.getMessage();
                            } else {
                                message = "执行失败，钱包【" + fromAddr + "】钱包文件验证未通过," + var50.getMessage();
                            }

                            System.err.println(message);
                            jsonResult.setMessage(message);
                            jsonResult.setSuccess(false);
                            return jsonResult;
                        }

                        TransactionErc20Vo tv = new TransactionErc20Vo();
                        tv.setContractAddress(contract);
                        tv.setFromAddress(fromAddr);
                        tv.setToAddress(toAddress);
                        if (StringUtils.equalsIgnoreCase(fromAddr, toAddress)) {
                            System.err.println("转币地址(" + fromAddr + ")与收转币地址(" + toAddress + ")不能相同");
                            tv.setSuccess(false);
                            tv.setMsg("转币地址与收转币地址相同");
                        } else {
                            BigInteger Erc20Value = TokenClient.getTokenBalance(web3j, fromAddr, contract);
                            if (Erc20Value == null) {
                                tv.setSuccess(false);
                                tv.setMsg("转账失败，查询代币余额超时");
                                System.err.println("从地址【" + fromAddr + "】转出代币失败，查询余额超时");
                            } else if (Erc20Value.intValue() == 0) {
                                tv.setSuccess(false);
                                tv.setMsg("要转出的代币余额为0，无需转账");
                                System.err.println("从地址【" + fromAddr + "】转出代币失败，余额为0，无需转账");
                            } else {
                                BigDecimal weiFactor;
                                BigDecimal dBigDecimal;
                                BigInteger nonce;
                                if (numType == 1) {
                                    tv.setValue(number);
                                    weiFactor = BigDecimal.TEN.pow(decimals);
                                    dBigDecimal = (new BigDecimal(number + "")).multiply(weiFactor);
                                    nonce = dBigDecimal.toBigInteger();
                                    tv.setToVal(nonce);
                                } else {
                                    tv.setToVal(Erc20Value);
                                    weiFactor = new BigDecimal(Erc20Value);
                                    dBigDecimal = weiFactor.divide(new BigDecimal(tmpStr), 10, 1);
                                    tv.setValue(dBigDecimal.doubleValue());
                                }

                                BigInteger tmpGasPriceVal = BigInteger.valueOf(Long.valueOf(tv.getGasPrice().longValue() / 1000000000L));
                                tv.setGasPrice(tmpGasPriceVal);
                                EthGetTransactionCount ethGetTransactionCount = (EthGetTransactionCount)web3j.ethGetTransactionCount(fromAddr, DefaultBlockParameterName.LATEST).sendAsync().get();
                                nonce = ethGetTransactionCount.getTransactionCount();
                                this.transactionUtil.setGas(gasPrice, gasLimit, tv);
                                this.transErc20Util.sendTransaction(tv, credentials, web3j, nonce);
                            }
                        }

                        transactionVos.add(tv);
                        rs = JSON.toJSONString(tv);
                        System.err.println(rs);
                        ++count;
                        FileUtil.writeToTxt(resultFilePath, rs);
                    }
                }

                long curTime = (new Date()).getTime();
                long totalTime = (curTime - startTime) / 1000L;
                message = "合约地址为【" + contract + "】的代币转账任务已提交到以太坊网络,具体处理情况需要根据交易hash到etherscan.io查询,任务数量:" + count + ",耗时:" + totalTime + "秒";
                System.err.println(message);
                jsonResult.setMessage(message);
                jsonResult.setSuccess(true);
                jsonResult.setData(transactionVos);
            } catch (Exception var51) {
                var51.printStackTrace();
                message = "执行失败，" + var51.getMessage();
                System.err.println(message);
                jsonResult.setMessage(message);
                jsonResult.setSuccess(false);
            }

            return jsonResult;
        }
    }
}
