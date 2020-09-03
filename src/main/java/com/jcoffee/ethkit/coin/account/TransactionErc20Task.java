package com.jcoffee.ethkit.coin.account;

import com.jcoffee.ethkit.coin.pojo.TransactionErc20Vo;
import com.jcoffee.ethkit.coin.pojo.TransactionVo;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;

@Component
public class TransactionErc20Task {
    @Value("${action.erc20}")
    private String erc20;
    @Value("${action.master}")
    private String master;
    @Value("${action.slave}")
    private String slave;
    @Value("${action.web3jHost}")
    private String web3jHost;
    @Autowired
    private TransErc20Util transErc20Util;
    @Autowired
    private TransactionUtil transactionUtil;
    @Autowired
    private LicenseUtil licenseUtil;

    public JsonResult<Object> startConsumer(String coin, String from, String to, String toAddress, String pwd, double number, Double gasPrice, Integer gasLimit, Integer numType) {
        JsonResult<Object> jsonResult = new JsonResult();
        String message = "";
        String rootDir = this.transactionUtil.getRootDir();
        if (!this.licenseUtil.checkLicense2()) {
            message = "软件授权验证未通过，请在【帮助】中获取机器公钥提供给我们，我们给您生成授权文件！";
            System.err.println(message);
            jsonResult.setSuccess(false);
            jsonResult.setMessage(message);
            return jsonResult;
        } else {
            String erc20DirPath = rootDir + this.erc20;
            File erc20Dir = new File(erc20DirPath);
            if (!erc20Dir.exists()) {
                erc20Dir.mkdirs();
            }

            String[] arr = StringUtils.split(coin, "_");
            String contract = arr[0];
            int decimals = Integer.valueOf(arr[1]);
            String tmpStr = "1";

            for(int i = 0; i < decimals; ++i) {
                tmpStr = tmpStr + "0";
            }

            String resultFilePath = erc20DirPath + "/" + contract + "_" + toAddress + ".txt";
            long startTime = (new Date()).getTime();
            List<TransactionVo> transactionVos = new ArrayList();
            int count = 0;

            try {
                Map<String, String> keyFileMap = new HashMap();
                String address = null;
                List<String> addrs = new ArrayList();
                List addrs2;
                String tmpGasPriceVal;
                List keyFile;
                Iterator var31;
                String content;
                if (StringUtils.isNotBlank(from)) {
                    addrs2 = Arrays.asList(StringUtils.split(from, ","));
                    addrs.addAll(addrs2);
                    tmpGasPriceVal = this.transactionUtil.getRootDir() + this.master;
                    keyFile = TransactionUtil.getAddrList(tmpGasPriceVal);
                    var31 = keyFile.iterator();

                    while(var31.hasNext()) {
                        content = (String)var31.next();
                        address = this.transactionUtil.getAddress(content);
                        if (!StringUtils.isBlank(address)) {
                            keyFileMap.put(address, content);
                        }
                    }
                }

                if (StringUtils.isNotBlank(to)) {
                    addrs2 = Arrays.asList(StringUtils.split(to, ","));
                    addrs.addAll(addrs2);
                    tmpGasPriceVal = this.transactionUtil.getRootDir() + this.slave;
                    keyFile = TransactionUtil.getAddrList(tmpGasPriceVal);
                    var31 = keyFile.iterator();

                    while(var31.hasNext()) {
                        content = (String)var31.next();
                        address = this.transactionUtil.getAddress(content);
                        if (!StringUtils.isBlank(address)) {
                            keyFileMap.put(address, content);
                        }
                    }
                }

                int size = addrs.size();
                Web3j web3j = this.transactionUtil.getWeb3jInstance();

                for(int i = 0; i < size; ++i) {
                    String fromAddress = addrs.get(i);
                    String fromAddr = StringUtils.removeEnd(fromAddress, "_pk");
                    String keyFileStr = keyFileMap.get(fromAddress);
                    Credentials credentials = null;

                    String rs;
                    try {
                        if (!StringUtils.endsWith(fromAddress, "_pk")) {
                            credentials = WalletUtils.loadCredentials(pwd, keyFileStr);
                        } else {
                            List<String> list = FileUtil.getContentList(keyFileStr);
                            if (list.isEmpty()) {
                                message = "钱包【" + fromAddr + "】私钥文件格式不正确，错误代码：001";
                                System.err.println(message);
                                jsonResult.setMessage(message);
                                jsonResult.setSuccess(false);
                                return jsonResult;
                            }

                            content = (String)list.get(0);
                            content = StringUtils.replace(content, ".", ":");
                            String[] arrs = StringUtils.split(content, ":");
                            if (arrs.length != 2) {
                                message = "钱包【" + fromAddr + "】私钥文件格式不正确，错误代码：002";
                                System.err.println(message);
                                jsonResult.setMessage(message);
                                jsonResult.setSuccess(false);
                                return jsonResult;
                            }

                            if (!StringUtils.equalsIgnoreCase(arrs[0], fromAddr)) {
                                message = "钱包【" + fromAddr + "】私钥文件格式不正确，错误代码：003";
                                System.err.println(message + ",【" + arrs[0] + "】");
                                jsonResult.setMessage(message);
                                jsonResult.setSuccess(false);
                                return jsonResult;
                            }

                            credentials = Credentials.create(arrs[1]);
                        }
                    } catch (Exception var42) {
                        var42.printStackTrace();
                        rs = var42.getMessage();
                        if (StringUtils.contains(rs, "Invalid password provided")) {
                            message = "执行失败，钱包【" + fromAddr + "】密码错误," + var42.getMessage();
                        } else {
                            message = "执行失败，钱包【" + fromAddr + "】钱包文件验证未通过," + var42.getMessage();
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
                            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(fromAddr, DefaultBlockParameterName.LATEST).sendAsync().get();
                            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
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
            } catch (Exception var43) {
                var43.printStackTrace();
                message = "执行失败，" + var43.getMessage();
                System.err.println(message);
                jsonResult.setMessage(message);
                jsonResult.setSuccess(false);
                return jsonResult;
            }

            long curTime = (new Date()).getTime();
            long totalTime = (curTime - startTime) / 1000L;
            message = "向地址【" + toAddress + "】转账合约地址为【" + contract + "】的代币任务已提交到以太坊网络,具体处理情况需要根据交易hash到etherscan.io查询,任务数量:" + count + ",耗时:" + totalTime + "秒";
            System.err.println(message);
            jsonResult.setMessage(message);
            jsonResult.setSuccess(true);
            jsonResult.setData(transactionVos);
            return jsonResult;
        }
    }
}