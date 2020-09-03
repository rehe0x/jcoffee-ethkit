package com.jcoffee.ethkit.coin.account;

import com.jcoffee.ethkit.coin.pojo.TransactionVo;
import com.jcoffee.ethkit.coin.util.EtherscanUtil;
import com.jcoffee.ethkit.coin.util.FileUtil;
import com.jcoffee.ethkit.coin.util.TransactionUtil;
import com.jcoffee.ethkit.common.JsonResult;
import com.jcoffee.ethkit.util.license.LicenseUtil;
import com.alibaba.fastjson.JSON;
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
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Convert.Unit;

@Component
public class TransactionTask2 {
    @Value("${action.toOne}")
    private String toOne;
    @Value("${action.master}")
    private String master;
    @Value("${action.slave}")
    private String slave;
    @Value("${action.web3jHost}")
    private String web3jHost;
    @Autowired
    private TransactionUtil transactionUtil;
    @Autowired
    private EtherscanUtil etherscanUtil;
    @Autowired
    private LicenseUtil licenseUtil;

    public TransactionTask2() {
    }

    public JsonResult<Object> startConsumer(String from, String to, String toAddress, String pwd, double number, Double gasPrice, Integer gasLimit, Integer numType) {
        String rootDir = this.transactionUtil.getRootDir();
        String resultFilePath = rootDir + this.toOne + "/" + toAddress + ".txt";
        long startTime = (new Date()).getTime();
        JsonResult<Object> jsonResult = new JsonResult();
        String message = "";
        List<TransactionVo> transactionVos = new ArrayList();
        if (!this.licenseUtil.checkLicense2()) {
            message = "软件授权验证未通过，请在【帮助】中获取机器公钥提供给我们，我们给您生成授权文件！";
            System.err.println(message);
            jsonResult.setSuccess(false);
            jsonResult.setMessage(message);
            return jsonResult;
        } else {
            int count = 0;

            try {
                Map<String, String> keyFileMap = new HashMap();
                String address = null;
                List<String> addrs = new ArrayList();
                List addrs2;
                String slaveKeyFilePath;
                List slaveKeyFilePathList;
                Iterator tmpVal;
                String keyFile;
                if (StringUtils.isNotBlank(from)) {
                    addrs2 = Arrays.asList(StringUtils.split(from, ","));
                    addrs.addAll(addrs2);
                    slaveKeyFilePath = this.transactionUtil.getRootDir() + this.master;
                    slaveKeyFilePathList = TransactionUtil.getAddrList(slaveKeyFilePath);
                    tmpVal = slaveKeyFilePathList.iterator();

                    while(tmpVal.hasNext()) {
                        keyFile = (String)tmpVal.next();
                        address = this.transactionUtil.getAddress(keyFile);
                        if (!StringUtils.isBlank(address)) {
                            keyFileMap.put(address, keyFile);
                        }
                    }
                }

                if (StringUtils.isNotBlank(to)) {
                    addrs2 = Arrays.asList(StringUtils.split(to, ","));
                    addrs.addAll(addrs2);
                    slaveKeyFilePath = this.transactionUtil.getRootDir() + this.slave;
                    slaveKeyFilePathList = TransactionUtil.getAddrList(slaveKeyFilePath);
                    tmpVal = slaveKeyFilePathList.iterator();

                    while(tmpVal.hasNext()) {
                        keyFile = (String)tmpVal.next();
                        address = this.transactionUtil.getAddress(keyFile);
                        if (!StringUtils.isBlank(address)) {
                            keyFileMap.put(address, keyFile);
                        }
                    }
                }

                Map<String, BigDecimal> balanceMap = this.etherscanUtil.getBalance(addrs);
                BigInteger value = Convert.toWei(number + "", Unit.ETHER).toBigInteger();
                int size = addrs.size();
                Web3j web3j = Web3j.build(new HttpService(this.web3jHost+"/"+transactionUtil.getToken()));
                String content = "";

                for(int i = 0; i < size; ++i) {
                    String fromAddress = addrs.get(i);
                    String fromAddr = StringUtils.removeEnd(fromAddress, "_pk");
                    if (StringUtils.equalsIgnoreCase(fromAddr, toAddress)) {
                        System.err.println("转币地址(" + fromAddr + ")与收转币地址(" + toAddress + ")不能相同");
                    } else {
                        keyFile = keyFileMap.get(fromAddress);
                        Credentials credentials = null;

                        try {
                            if (!StringUtils.endsWith(fromAddress, "_pk")) {
                                credentials = WalletUtils.loadCredentials(pwd, keyFile);
                            } else {
                                List<String> list = FileUtil.getContentList(keyFile);
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
                                    System.err.println(message);
                                    jsonResult.setMessage(message);
                                    jsonResult.setSuccess(false);
                                    return jsonResult;
                                }

                                credentials = Credentials.create(arrs[1]);
                            }
                        } catch (Exception var37) {
                            var37.printStackTrace();
                            String msg = var37.getMessage();
                            if (StringUtils.contains(msg, "Invalid password provided")) {
                                message = "执行失败，钱包【" + fromAddr + "】密码错误," + var37.getMessage();
                            } else {
                                message = "执行失败，钱包【" + fromAddr + "】钱包文件验证未通过," + var37.getMessage();
                            }

                            System.err.println(message);
                            jsonResult.setMessage(message);
                            jsonResult.setSuccess(false);
                            return jsonResult;
                        }

                        TransactionVo tv = new TransactionVo();
                        tv.setFromAddress(fromAddr);
                        tv.setToAddress(toAddress);
                        BigDecimal val = (BigDecimal)balanceMap.get(fromAddr);
                        if (val.doubleValue() == 0.0D) {
                            System.err.println("地址【" + fromAddr + "】ETH余额为0，无需转出");
                        } else {
                            if (numType == 2) {
                                tv.setValue(val.doubleValue());
                                BigInteger toVal = Convert.toWei(val + "", Unit.ETHER).toBigInteger();
                                tv.setToVal(toVal);
                            } else {
                                tv.setToVal(value);
                                tv.setValue(number);
                            }

                            EthGetTransactionCount ethGetTransactionCount = (EthGetTransactionCount)web3j.ethGetTransactionCount(fromAddr, DefaultBlockParameterName.LATEST).sendAsync().get();
                            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
                            this.transactionUtil.setGas(gasPrice, gasLimit, tv);
                            this.transactionUtil.sendTransaction(tv, credentials, web3j, nonce);
                            tv.setGasPrice(BigInteger.valueOf(Long.valueOf(tv.getGasPrice().longValue() / 1000000000L)));
                            transactionVos.add(tv);
                            String rs = JSON.toJSONString(tv);
                            System.err.println(rs);
                            ++count;
                            FileUtil.writeToTxt(resultFilePath, rs);
                        }
                    }
                }
            } catch (Exception var38) {
                var38.printStackTrace();
                message = "执行失败，" + var38.getMessage();
                System.err.println(message);
                jsonResult.setMessage(message);
                jsonResult.setSuccess(false);
                return jsonResult;
            }

            long curTime = (new Date()).getTime();
            long totalTime = (curTime - startTime) / 1000L;
            message = "向地址【" + toAddress + "】转ETH任务已提交到以太坊网络,具体处理情况需要根据交易hash到etherscan.io查询,任务数量:" + count + ",耗时:" + totalTime + "秒";
            System.err.println(message);
            jsonResult.setMessage(message);
            jsonResult.setSuccess(true);
            jsonResult.setData(transactionVos);
            return jsonResult;
        }
    }
}

