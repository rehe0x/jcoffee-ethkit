package com.jcoffee.ethkit.coin.util;

import com.jcoffee.ethkit.coin.pojo.TransactionVo;
import com.jcoffee.ethkit.coin.pojo.WalletInfo;
import com.jcoffee.ethkit.common.JsonResult;
import com.jcoffee.ethkit.util.DateUtils;
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
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;
import org.web3j.utils.Convert.Unit;

@Component
public class TransactionUtil {
    private static List<String> tokenList = new ArrayList();
    private static Random rand = new Random();
    @Value("${action.rootDir}")
    private String rootDir;
    @Value("${action.transFlag}")
    private Integer transFlag;
    @Value("${action.web3jHost}")
    private String web3jHost;
    private static Web3j web3j;
    @Autowired
    private EtherscanUtil etherscanUtil;
    @Autowired
    private TransactionUtil transactionUtil;

    public synchronized Web3j getWeb3jInstance() {
        if (web3j == null) {
            web3j = Web3j.build(new HttpService(this.web3jHost+"/"+transactionUtil.getToken()));
            System.err.println("Web3j.build=====");
        }

        return web3j;
    }

    public TransactionUtil() {
    }

    public void setGas(Double gasPriceVal, Integer gasLimitVal, TransactionVo tv) {
        BigInteger gasPrice = Convert.toWei(gasPriceVal + "", Unit.GWEI).toBigInteger();
        BigInteger gasLimit = BigInteger.valueOf((long)gasLimitVal);
        tv.setGasPrice(gasPrice);
        tv.setGasLimit(gasLimit);
    }

    public TransactionVo sendTransaction(TransactionVo tv, Credentials credentials, Web3j web3j, BigInteger nonce) {
        String message = "";

        try {
            tv.setDateTime(DateUtils.fomatToTimeString(new Date()));
            if (this.transFlag == 1) {
                String toAddress = tv.getToAddress();
                BigInteger gasPrice = tv.getGasPrice();
                BigInteger gasLimit = tv.getGasLimit();
                BigInteger value = tv.getToVal();
                RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, toAddress, value);
                byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
                String hexValue = Numeric.toHexString(signedMessage);
                EthSendTransaction ethSendTransaction = (EthSendTransaction)web3j.ethSendRawTransaction(hexValue).sendAsync().get();
                String transactionHash = ethSendTransaction.getTransactionHash();
                System.err.println(transactionHash);
                tv.setTransactionHash(transactionHash);
                if (transactionHash == null) {
                    message = "转账失败，" + ethSendTransaction.getError().getMessage();
                    System.err.println(message);
                    tv.setSuccess(false);
                    tv.setMsg(message);
                }
            } else {
                tv.setTransactionHash("tx_test_" + UUID.randomUUID().toString());
            }
        } catch (Exception var15) {
            var15.printStackTrace();
            message = "转账失败,系统异常!" + var15.getMessage();
            System.err.println(var15.getMessage());
            tv.setSuccess(false);
            tv.setMsg(message);
        }

        return tv;
    }

    public static List<String> getAddrList(String fileDir) {
        ArrayList list = new ArrayList();

        try {
            File dir = new File(fileDir);
            File[] files = dir.listFiles();
            File[] var4 = files;
            int var5 = files.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                File file = var4[var6];
                if (file.isFile()) {
                    list.add(file.getAbsolutePath());
                }
            }
        } catch (Exception var8) {
            var8.printStackTrace();
        }

        return list;
    }

    public JsonResult<Map<String, Object>> getEthAdrrList(String fileDir, String coin, Integer isSearchBalance) {
        JsonResult<Map<String, Object>> jsonResult = new JsonResult();
        String message = "";
        BigDecimal total = new BigDecimal(0.0D);
        List<WalletInfo> list = new ArrayList();
        String tmpBalance = "";

        try {
            List<String> accountIdList = null;
            String contract;
            WalletInfo wi;
            if (StringUtils.startsWith(fileDir, "0x")) {
                accountIdList = Arrays.asList(StringUtils.split(fileDir, ","));
                isSearchBalance = 1;
                Iterator var22 = accountIdList.iterator();

                while(var22.hasNext()) {
                    String addr = (String)var22.next();
                    WalletInfo walletInfo = new WalletInfo();
                    walletInfo.setAddress(addr);
                    list.add(walletInfo);
                }
            } else {
                File dir = new File(fileDir);
                if (!dir.exists()) {
                    message = "钱包不存在【" + fileDir + "】不存在";
                    System.err.println(message);
                    jsonResult.setMessage(message);
                    jsonResult.setSuccess(false);
                    return jsonResult;
                }

                File[] files = dir.listFiles();
                contract = null;
                File[] var13 = files;
                int var14 = files.length;

                for(int var15 = 0; var15 < var14; ++var15) {
                    File file = var13[var15];
                    if (file.isFile()) {
                        contract = this.getAddress(file.getAbsolutePath());
                        if (!StringUtils.isBlank(contract)) {
                            wi = new WalletInfo();
                            wi.setAddress(contract);
                            wi.setBalance("待查询...");
                            list.add(wi);
                        }
                    }
                }

                accountIdList = list.stream().map((e) -> {
                    return e.getAddress();
                }).distinct().collect(Collectors.toList());
            }

            String tmpAddr = "";
            if (isSearchBalance == 1) {
                if (StringUtils.equalsIgnoreCase(coin, "ETH")) {
                    Map<String, BigDecimal> map = this.etherscanUtil.getBalance(accountIdList);
                    WalletInfo wis;
                    for(Iterator var29 = list.iterator(); var29.hasNext(); System.err.println("地址【" + wis.getAddress() + "】余额：" + wis.getBalance())) {
                        wis = (WalletInfo)var29.next();
                        tmpAddr = StringUtils.removeEnd(wis.getAddress(), "_pk");
                        BigDecimal balance = (BigDecimal)map.get(tmpAddr);
                        if (balance != null) {
                            tmpBalance = balance + "";
                            if (StringUtils.contains(tmpBalance, "E")) {
                                wis.setBalance("0");
                            } else {
                                wis.setBalance(balance + "");
                                total = total.add(new BigDecimal(balance + ""));
                            }
                        } else {
                            wis.setBalance("查询余额超时");
                        }
                    }
                } else {
                    String[] arr = StringUtils.split(coin, "_");
                    contract = arr[0];
                    int decimals = Integer.valueOf(arr[1]);
                    Web3j web3j = this.getWeb3jInstance();
                    String tmpStr = "1";

                    for(int i = 0; i < decimals; ++i) {
                        tmpStr = tmpStr + "0";
                    }

                    for(Iterator var36 = list.iterator(); var36.hasNext(); System.err.println("地址【" + wi.getAddress() + "】余额：" + wi.getBalance())) {
                        wi = (WalletInfo)var36.next();
                        tmpAddr = StringUtils.removeEnd(wi.getAddress(), "_pk");
                        BigInteger tmpVal = TokenClient.getTokenBalance(web3j, tmpAddr, contract);
                        if (tmpVal != null) {
                            BigDecimal balanceN = new BigDecimal(tmpVal);
                            BigDecimal value = balanceN.divide(new BigDecimal(tmpStr), 10, 1);
                            tmpBalance = value + "";
                            if (StringUtils.contains(tmpBalance, "E")) {
                                wi.setBalance("0");
                            } else {
                                wi.setBalance(value + "");
                                total = total.add(new BigDecimal(value + ""));
                            }
                        } else {
                            wi.setBalance("查询余额超时");
                        }
                    }
                }
            }
        } catch (Exception var21) {
            var21.printStackTrace();
        }

        if (StringUtils.contains(tmpBalance, "E")) {
            total = new BigDecimal(0.0D);
        }

        Map<String, Object> map = new HashMap();
        map.put("list", list);
        map.put("sum", total);
        jsonResult.setData(map);
        return jsonResult;
    }

    public boolean checkTransaction(String transactionHash, String token) {
        boolean success = false;

        try {
            Web3j web3j = this.getWeb3jInstance();
            EthTransaction et = (EthTransaction)web3j.ethGetTransactionByHash(transactionHash).send();
            if (et.getResult() != null) {
                success = true;
            }
        } catch (Exception var6) {
            System.err.println("checkTransaction:" + var6.getMessage());
        }

        return success;
    }

    public String getRootDir() {
        if (StringUtils.isNotBlank(this.rootDir) && !StringUtils.equals(this.rootDir, "wu")) {
            return this.rootDir;
        } else {
            this.rootDir = System.getProperty("user.dir");
            this.rootDir = StringUtils.replace(this.rootDir, "\\", "/");
            return this.rootDir;
        }
    }

    public String getToken() {
        String token = "";
        File file = new File(this.getRootDir() + "/config/token.txt");
        if (!file.exists()) {
            token = "TdImNW9lRg1Mk2WFiTUy";
            return token;
        } else {
            if (tokenList.isEmpty()) {
                tokenList = FileUtil.getContentList(this.getRootDir() + "/config/token.txt");
            }

            if (tokenList.isEmpty()) {
                token = "TdImNW9lRg1Mk2WFiTUy";
                return token;
            } else {
                int index = rand.nextInt(tokenList.size());
                token = (String)tokenList.get(index);
                return token;
            }
        }
    }

    public String getAddress(String keyFilePath) {
        String address = "";
        if (StringUtils.endsWith(keyFilePath, ".txt")) {
            return address;
        } else {
            if (StringUtils.contains(keyFilePath, "--")) {
                address = StringUtils.substringAfterLast(keyFilePath, "--");
                address = StringUtils.removeEnd(address, ".json");
                address = "0x" + address;
            } else {
                address = "0x" + StringUtils.substringAfterLast(keyFilePath, "0x");
                address = StringUtils.removeEnd(address, ".json");
            }

            address = StringUtils.lowerCase(address);
            return address;
        }
    }

    public Unit getWei(int decimals) {
        Unit unit = null;
        if (decimals == 0) {
            unit = Unit.WEI;
        } else if (decimals == 3) {
            unit = Unit.KWEI;
        } else if (decimals == 6) {
            unit = Unit.MWEI;
        } else if (decimals == 9) {
            unit = Unit.GWEI;
        } else if (decimals == 12) {
            unit = Unit.SZABO;
        } else if (decimals == 15) {
            unit = Unit.FINNEY;
        } else if (decimals == 18) {
            unit = Unit.ETHER;
        } else if (decimals == 21) {
            unit = Unit.KETHER;
        } else if (decimals == 24) {
            unit = Unit.METHER;
        } else if (decimals == 27) {
            unit = Unit.GETHER;
        }

        return unit;
    }
}

