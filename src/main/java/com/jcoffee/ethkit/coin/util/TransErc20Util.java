package com.jcoffee.ethkit.coin.util;

import com.jcoffee.ethkit.coin.pojo.ContractInfo;
import com.jcoffee.ethkit.coin.pojo.TransactionErc20Vo;
import com.jcoffee.ethkit.common.JsonResult;
import com.jcoffee.ethkit.util.DateUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;
import org.web3j.utils.Convert.Unit;

@Component
public class TransErc20Util {
    @Value("${action.transFlag}")
    private Integer transFlag;
    @Autowired
    private TransactionUtil transactionUtil;
    @Autowired
    private EtherscanUtil etherscanUtil;
    private static final String ETHERSCAN_API = "https://api.etherscan.io/api?module=account&action=tokenbalance&contractaddress={0}&address={1}&tag=latest&apikey=CR93A3EVF4W2KAEBEAUVRQD4CQVBAICYA8";

    public static void main(String[] args) throws Exception {
        double number = 542.225D;
        double tmpNumber = number * 1.0E9D;
        BigInteger value = Convert.toWei(tmpNumber + "", Unit.GWEI).toBigInteger();
        System.err.println(value);
    }

    public JsonResult getSumBalance(String contractAddress, List addressList) {
        BigDecimal totalAmount = new BigDecimal(0);
        boolean success = true;
        JsonResult jsonResult = new JsonResult();
        String tmpBalance = "";
        if (StringUtils.equalsIgnoreCase(contractAddress, "ETH")) {
            Map map = this.etherscanUtil.getBalance(addressList);
            BigDecimal balance = null;
            Iterator var9 = map.entrySet().iterator();

            while(var9.hasNext()) {
                Entry entry = (Entry)var9.next();
                balance = (BigDecimal)entry.getValue();
                if (balance != null) {
                    tmpBalance = balance + "";
                    if (!StringUtils.contains(tmpBalance, "E")) {
                        totalAmount = totalAmount.add(new BigDecimal(balance + ""));
                    }
                }
            }
        } else {
            String[] arr = StringUtils.split(contractAddress, "_");
            String contract = arr[0];
            int decimals = Integer.valueOf(arr[1]);
            Web3j web3j = this.transactionUtil.getWeb3jInstance();
            String tmpStr = "1";

            for(int i = 0; i < decimals; ++i) {
                tmpStr = tmpStr + "0";
            }

            Iterator var21 = addressList.iterator();

            while(var21.hasNext()) {
                String address = (String)var21.next();
                address = StringUtils.removeEnd(address, "_pk");
                BigInteger tmpVal = TokenClient.getTokenBalance(web3j, address, contract);
                if (tmpVal != null) {
                    BigDecimal balanceN = new BigDecimal(tmpVal);
                    BigDecimal value = balanceN.divide(new BigDecimal(tmpStr), 10, 1);
                    tmpBalance = value + "";
                    if (!StringUtils.contains(tmpBalance, "E")) {
                        totalAmount = totalAmount.add(new BigDecimal(value + ""));
                    }
                }
            }
        }

        jsonResult.setSuccess(success);
        jsonResult.setData(totalAmount);
        return jsonResult;
    }

    public JsonResult getSumBalancebak(String contractAddress, List addressList) {
        String rs = "";
        BigDecimal totalAmount = new BigDecimal(0);
        boolean success = true;
        JsonResult jsonResult = new JsonResult();
        ContractInfo contractInfo = this.etherscanUtil.getErc20ContractInfo(contractAddress);
        if (contractInfo == null) {
            success = false;
            rs = "ERC20代币合约地址有误或者网络请求超时";
            jsonResult.setMessage(rs);
            jsonResult.setData(rs);
            jsonResult.setSuccess(success);
            return jsonResult;
        } else {
            int decimals = contractInfo.getDecimals();
            Web3j web3j = this.transactionUtil.getWeb3jInstance();

            BigInteger tmpValue;
            for(Iterator var10 = addressList.iterator(); var10.hasNext(); totalAmount = totalAmount.add(new BigDecimal(tmpValue + ""))) {
                String address = (String)var10.next();
                tmpValue = TokenClient.getTokenBalance(web3j, address, contractAddress);
                BigDecimal balance = new BigDecimal(tmpValue);
                balance.divide(BigDecimal.TEN.pow(decimals));
            }

            rs = totalAmount + "【" + contractInfo.getSymbol() + "】";
            jsonResult.setSuccess(success);
            jsonResult.setData(rs);
            return jsonResult;
        }
    }

    public BigDecimal getBalance2(String contractAddress, String address, int decimals) {
        BigDecimal value = null;
        String url = MessageFormat.format("https://api.etherscan.io/api?module=account&action=tokenbalance&contractaddress={0}&address={1}&tag=latest&apikey=CR93A3EVF4W2KAEBEAUVRQD4CQVBAICYA8", contractAddress, address);
        String balanceInfo = "";

        try {
            balanceInfo = EtherscanUtil.get(url);
        } catch (Exception var13) {
            System.err.println("获取帐户余额失败," + var13.getMessage());
            var13.printStackTrace();
            return value;
        }

        System.err.println(balanceInfo);
        JSONObject jsb = JSON.parseObject(balanceInfo);
        Integer status = jsb.getInteger("status");
        if (status != 1) {
            System.err.println("获取帐户余额失败");
            return value;
        } else {
            String balance = jsb.getString("result");
            BigDecimal balanceN = new BigDecimal(balance);
            String tmpStr = "1";

            for(int i = 0; i < decimals; ++i) {
                tmpStr = tmpStr + "0";
            }

            value = balanceN.divide(new BigDecimal(tmpStr), 5, 1);
            return value;
        }
    }

    public BigInteger getBalanceBak(String contractAddress, String address) {
        BigInteger value = null;
        String url = MessageFormat.format("https://api.etherscan.io/api?module=account&action=tokenbalance&contractaddress={0}&address={1}&tag=latest&apikey=CR93A3EVF4W2KAEBEAUVRQD4CQVBAICYA8", contractAddress, address);
        String balanceInfo = "";

        try {
            balanceInfo = EtherscanUtil.get(url);
        } catch (Exception var9) {
            System.err.println("获取帐户余额失败," + var9.getMessage());
            var9.printStackTrace();
            return value;
        }

        System.err.println(balanceInfo);
        JSONObject jsb = JSON.parseObject(balanceInfo);
        Integer status = jsb.getInteger("status");
        if (status != 1) {
            System.err.println("获取帐户余额失败");
            return value;
        } else {
            String balance = jsb.getString("result");
            value = Convert.toWei(balance, Unit.WEI).toBigInteger();
            return value;
        }
    }

    public TransactionErc20Vo sendTransaction(TransactionErc20Vo tv, Credentials credentials, Web3j web3j, BigInteger nonce) {
        String message = "";

        try {
            tv.setDateTime(DateUtils.fomatToTimeString(new Date()));
            if (this.transFlag == 1) {
                String toAddress = tv.getToAddress();
                BigInteger gasPrice = tv.getGasPrice();
                BigInteger gasLimit = tv.getGasLimit();
                BigInteger value = tv.getToVal();
                String contractAddress = tv.getContractAddress();
                Address address = new Address(toAddress);
                Uint256 uintValue = new Uint256(value);
                List parametersList = new ArrayList();
                parametersList.add(address);
                parametersList.add(uintValue);
                List outList = new ArrayList();
                Function function = new Function("transfer", parametersList, outList);
                String encodedFunction = FunctionEncoder.encode(function);
                RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, contractAddress, encodedFunction);
                byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
                String hexValue = Numeric.toHexString(signedMessage);
                EthSendTransaction ethSendTransaction = (EthSendTransaction)web3j.ethSendRawTransaction(hexValue).sendAsync().get();
                String transactionHash = ethSendTransaction.getTransactionHash();
                System.err.println(transactionHash);
                tv.setTransactionHash(transactionHash);
                if (transactionHash == null) {
                    message = "转账失败,原因【" + ethSendTransaction.getError().getMessage() + "】";
                    System.err.println(message);
                    tv.setSuccess(false);
                    tv.setMsg(message);
                }
            } else {
                tv.setTransactionHash("tx_test_" + UUID.randomUUID().toString());
            }
        } catch (Exception var22) {
            var22.printStackTrace();
            System.err.println(var22.getMessage());
            tv.setSuccess(false);
            tv.setMsg("转账失败,网络异常!" + message);
        }

        return tv;
    }
}
