package com.jcoffee.ethkit.coin.account;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;

@Component
public class TransactionTest {
    public static void main(String[] args) throws Exception {
        String url = "https://mainnet.infura.io/v3/f5k7X36NcmyCs0UksgVd";
        url = "https://api.myetherapi.com/eth";
        Web3j web3j = Web3j.build(new HttpService(url));
        EthGasPrice ethGasPrice = (EthGasPrice)web3j.ethGasPrice().send();
        BigInteger gasPrice = ethGasPrice.getGasPrice();
        BigDecimal balanceN = new BigDecimal(gasPrice);
        BigDecimal value = balanceN.divide(new BigDecimal("1000000000"), 2, 1);
        System.err.println(value);
    }

    public static BigInteger getBalance(String accountId, Web3j web3j) {
        try {
            DefaultBlockParameter defaultBlockParameter = new DefaultBlockParameterNumber(58L);
            EthGetBalance ethGetBalance = (EthGetBalance)web3j.ethGetBalance(accountId, defaultBlockParameter).send();
            if (ethGetBalance != null) {
                return ethGetBalance.getBalance();
            }
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return null;
    }
}
