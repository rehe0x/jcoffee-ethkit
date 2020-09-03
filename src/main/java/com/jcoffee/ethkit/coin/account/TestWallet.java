package com.jcoffee.ethkit.coin.account;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

public class TestWallet {
    public static void main(String[] args) throws Exception {
        BigDecimal weiFactor = BigDecimal.TEN.pow(8);
        BigDecimal dBigDecimal = (new BigDecimal("10")).multiply(weiFactor);
        BigInteger value = dBigDecimal.toBigInteger();
        System.err.println(value);
    }

    public static void loadCredentials(String pwd, File file) throws Exception {
        Credentials credentials = WalletUtils.loadCredentials(pwd, file);
        String address = credentials.getAddress();
        String publicKey = credentials.getEcKeyPair().getPublicKey().toString(16);
        String privateKey = credentials.getEcKeyPair().getPrivateKey().toString(16);
        System.err.println("address:" + address);
        System.err.println("publicKey:" + publicKey);
        System.err.println("privateKey:" + privateKey);
    }

    public static void createCredentials(String PrivateKey) throws Exception {
        Credentials credentials = Credentials.create(PrivateKey);
        String address = credentials.getAddress();
        String publicKey = credentials.getEcKeyPair().getPublicKey().toString(16);
        String privateKey = credentials.getEcKeyPair().getPrivateKey().toString(16);
        System.err.println("address:" + address);
        System.err.println("publicKey:" + publicKey);
        System.err.println("privateKey:" + privateKey);
    }
}
