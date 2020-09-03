package com.jcoffee.ethkit.test.h2;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Utils;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.Wallet;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @program jcoffee-ethkit 
 * @description:  
 * @author: Horng 
 * @create: 2020/09/02 13:36 
 */
public class Test {
    public static void main(String[] args) {
        NetworkParameters networkParameters = MainNetParams.get() ;
        DeterministicSeed seed = new DeterministicSeed(new SecureRandom(), 128, "", Utils.currentTimeSeconds());
        Wallet wallet;
        String mnemonics = "";
        String privateKey = "";
        String publicKey = "";
        String address = "";
        String pwd = "";
        try {
            wallet = Wallet.fromSeed(networkParameters, seed);
            //私钥
            privateKey = wallet.currentReceiveKey().getPrivateKeyAsWiF(networkParameters);
            //助记词
            mnemonics = wallet.getKeyChainSeed().getMnemonicCode().toString();
            publicKey = Hex.toHexString(ECKey.publicKeyFromPrivate(wallet.currentReceiveKey().getPrivKey(), true));
            //地址
            address = wallet.currentReceiveAddress().toBase58();
            System.out.println(address.length());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map resultMap = new LinkedHashMap();
        System.out.println(mnemonics);
        byte[] b = Base64.getDecoder().decode("MHQCAQEEIPK5ZaUsNw/qTdDf1grVN4+jXKyOP/bOnCkyko68GCnPoAcGBSuBBAAKoUQDQgAESw+vJKteU+pRRGmsUH+u4ZC6t3OeGtIo+axlxiIsakITSDU0+Q9Yvd1TZaPuTbANJoTTBrh+fhAeLS6LLjmLGg==");

        String s = new String(Base64.getDecoder().decode("MHQCAQEEIPK5ZaUsNw/qTdDf1grVN4+jXKyOP/bOnCkyko68GCnPoAcGBSuBBAAKoUQDQgAESw+vJKteU+pRRGmsUH+u4ZC6t3OeGtIo+axlxiIsakITSDU0+Q9Yvd1TZaPuTbANJoTTBrh+fhAeLS6LLjmLGg=="));
        System.out.println(privateKey);
        System.out.println(publicKey);
        System.out.println(address);

    }
}
