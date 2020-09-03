package com.jcoffee.ethkit.test.h2;

import org.bitcoinj.core.Base58;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECPoint;

/**
 * @program blockchain_study 
 * @description:  
 * @author: Horng 
 * @create: 2020/09/02 16:37 
 */
public class KeyGenerator {
    // Base58 encode prefix，不同的prefix可以定制地址的首字母

    static final byte PubKeyPrefix = 00;

    static final byte PrivKeyPrefix = -128;

    static final String PrivKeyPrefixStr = "80";

    static final byte PrivKeySuffix = 0x01;

    static int keyGeneratedCount = 1;

    static boolean debug = true;

    static KeyPairGenerator sKeyGen;

    static ECGenParameterSpec sEcSpec;

    static {

            Security.addProvider(new BouncyCastleProvider());

    }

    private static boolean ParseArguments(String []argv) {

        for (int i = 0; i < argv.length - 1; i++) {

            if ("-n".equals(argv[i])) {

                try {

                    keyGeneratedCount = Integer.parseInt(argv[i + 1]);

                    i = i + 1;

                    continue;

                } catch (NumberFormatException e) {

                    e.printStackTrace();

                    return false;

                }

            } else if ("-debug".equals(argv[i])) {

                debug = true;

            } else {

                System.out.println(argv[i] + " not supported...");

                return false;

            }

        }

        return keyGeneratedCount > 0;

    }

    public static void main(String args[]) {

        if (args.length > 1) {

          if (!ParseArguments(args)) {

              System.out.println("Arguments error, please check...");

              System.exit(-1);

          }

        }

        com.jcoffee.ethkit.test.h2.Key key = new com.jcoffee.ethkit.test.h2.Key();

        key.Reset();

        KeyGenerator generator = new KeyGenerator();

        for (int i = 0; i < keyGeneratedCount; i++) {

            key.Reset();

            if (generator.GenerateKey(key)) {

                System.out.println(key.ToString());

            } else {

                System.out.println("Generate key error...");

                System.exit(-1);

            }

        }

    }

    public KeyGenerator() {

        Init();

    }

    private void Init() {

        // Initialize key generator

        // The specific elliptic curve used is the secp256k1.

        try {

            sKeyGen = KeyPairGenerator.getInstance("EC");

            sEcSpec = new ECGenParameterSpec("secp256k1");

            if (sKeyGen == null) {

                System.out.println("Error: no ec algorithm");

                System.exit(-1);

            }

            sKeyGen.initialize(sEcSpec); // 采用secp256K1标准的椭圆曲线加密算法

        } catch (InvalidAlgorithmParameterException e) {

            System.out.println("Error:" + e);

            System.exit(-1);

        } catch (NoSuchAlgorithmException e) {

            System.out.println("Error:" + e);

            System.exit(-1);

        } catch (Exception e) {

            System.out.println("Error:" + e);

            System.exit(-1);

        }

    }

    public boolean GenerateKey(Key key) {

        key.Reset();

        // Generate key pair，依据椭圆曲线算法产生公私钥对

        KeyPair kp = sKeyGen.generateKeyPair();

        PublicKey pub = kp.getPublic();

        PrivateKey pvt = kp.getPrivate();

        ECPrivateKey epvt = (ECPrivateKey)pvt;

        String sepvt = Utils.AdjustTo64(epvt.getS().toString(16)).toUpperCase(); // 私钥16进制字符串

        if (debug) {

            System.out.println("Privkey[" + sepvt.length() + "]: " + sepvt);

        }

      // 获取X，Y坐标点，“04” + sx + sy即可获得完整的公钥，但是这里我们需要压缩的公钥

        ECPublicKey epub = (ECPublicKey)pub;

        ECPoint pt = epub.getW();

        String sx = Utils.AdjustTo64(pt.getAffineX().toString(16)).toUpperCase();

        String sy = Utils.AdjustTo64(pt.getAffineY().toString(16)).toUpperCase();

        String bcPub = "04" + sx + sy;

        if (debug) {

            System.out.println("Pubkey[" + bcPub.length() + "]: " + bcPub);

        }

        // Here we get compressed pubkey

       // 获取压缩公钥的方法：Y坐标最后一个字节是偶数，则 "02" + sx，否则 "03" + sx

        byte[] by = Utils.HexStringToByteArray(sy);

        byte lastByte = by[by.length - 1];

        String compressedPk;

        if ((int)(lastByte) % 2 == 0) {

            compressedPk = "02" + sx;

        } else {

            compressedPk = "03" + sx;

        }

        if (debug) {

            System.out.println("compressed pubkey: " + compressedPk);

        }

        key.SetPubKey(compressedPk);

        // We now need to perform a SHA-256 digest on the public key,

        // followed by a RIPEMD-160 digest.

       // 对压缩的公钥做SHA256摘要

        byte[] s1 = null;

        MessageDigest sha = null;

        try {

            sha = MessageDigest.getInstance("SHA-256");

            s1 = sha.digest(Utils.HexStringToByteArray(compressedPk));

            if (debug) {

                System.out.println("sha: " + Utils.BytesToHex(s1).toUpperCase());

            }

        } catch (NoSuchAlgorithmException e) {

            System.out.println("Error:" + e);

            return false;

        }

        // We use the Bouncy Castle provider for performing the RIPEMD-160 digest

        // since JCE does not implement this algorithm.

        // SHA256摘要之后做RIPEMD-160，这里调用Bouncy Castle的库，不知道的同学百度搜一下就懂了

        byte[] r1 = null;

        byte[] r2 = null;

        try {

            MessageDigest rmd = MessageDigest.getInstance("RipeMD160", "BC");

            if (rmd == null || s1 == null) {

                System.out.println("can't get ripemd160 or sha result is null");

                return false;

            }

            r1 = rmd.digest(s1);

            r2 = new byte[r1.length + 1];

            r2[0] = PubKeyPrefix; // RipeMD160 摘要之后加上公钥前缀

            for (int i = 0; i < r1.length; i++)

                r2[i + 1] = r1[i]; // 写的有点low，大家采用System.arraycopy自行修改吧

            if (debug) {

                System.out.println("rmd: " + Utils.BytesToHex(r2).toUpperCase());

            }

        } catch (NoSuchAlgorithmException e) {

            System.out.println("Error:" + e);

            return false;

        } catch (NoSuchProviderException e) {

            System.out.println("Error:" + e);

            return false;

        }

        byte[] s2 = null; // 加上前缀之后做两次SHA256

        if (sha != null && r2 != null) {

            sha.reset();

            s2 = sha.digest(r2);

            if (debug) {

                System.out.println("sha: " + Utils.BytesToHex(s2).toUpperCase());

            }

        } else {

            System.out.println("cant't do sha-256 after ripemd160");

            return false;

        }

        byte[] s3 = null;

        if (sha != null && s2 != null) {

            sha.reset();

            s3 = sha.digest(s2);

            if (debug) {

                System.out.println("sha: " + Utils.BytesToHex(s3).toUpperCase());

            }

        } else {

            System.out.println("cant't do sha-256 after sha-256");

            return false;

        }

        // 读懂下面内容，大家仔细阅读《比特币密钥生成规则及 Go 实现》

        byte[] a1 = new byte[r2.length + 4];

        for (int i = 0 ; i < r2.length ; i++) a1[i] = r2[i];

        for (int i = 0 ; i < 4 ; i++) a1[r2.length + i] = s3[i];

        if (debug) {

            System.out.println("before base58: " + Utils.BytesToHex(a1).toUpperCase());

        }

        key.SetAddress(Base58.encode(a1)); // 到此，可以获取WIF格式的地址

        if (debug) {

            System.out.println("addr: " + Base58.encode(a1));

        }

        // Lastly, we get compressed privkey 最后获取压缩的私钥

        byte[] pkBytes = null;

        pkBytes = Utils.HexStringToByteArray("80" + sepvt + "01");//sepvt.getBytes("UTF-8");

        if (debug) {

                System.out.println("raw compressed privkey: " + Utils.BytesToHex(pkBytes).toUpperCase());

            }

        try {

            sha = MessageDigest.getInstance("SHA-256");

        } catch (NoSuchAlgorithmException e) {

            System.out.println("Error:" + e);

            return false;

        }

        sha.reset();

        byte[] shafirst  = sha.digest(pkBytes);

        sha.reset();

        byte[] shasecond = sha.digest(shafirst);

        byte[] compressedPrivKey = new byte[pkBytes.length + 4];

        for (int i = 0; i < pkBytes.length; i++) {

            compressedPrivKey[i] = pkBytes[i];

        }

        for (int j = 0; j < 4; j++) {

            compressedPrivKey[j + pkBytes.length] = shasecond[j];

        }

        //compressedPrivKey[compressedPrivKey.length - 1] = PrivKeySuffix;

        key.SetPrivKey(Base58.encode(compressedPrivKey));

        if (debug) {

            System.out.println("compressed private key: " + Base58.encode(compressedPrivKey));

        }

        return true;

    }

}
