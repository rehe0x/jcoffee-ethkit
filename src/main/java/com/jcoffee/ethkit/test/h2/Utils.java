package com.jcoffee.ethkit.test.h2;
/**
 * @program blockchain_study 
 * @description:  
 * @author: Horng 
 * @create: 2020/09/02 16:39 
 */
public class Utils {
      public static String AdjustTo64(String s) {

        switch(s.length()) {

            case 62: return "00" + s;

            case 63: return "0" + s;

            case 64: return s;

            default:

                throw new IllegalArgumentException("not a valid key: " + s);

        }

    }

    public static String BytesToHex(byte[] src) {

        StringBuilder stringBuilder = new StringBuilder("");

        if (src == null || src.length <= 0) {

            return null;

        }

        for (int i = 0; i < src.length; i++) {

            int v = src[i] & 0xFF;

            String hv = Integer.toHexString(v);

            if (hv.length() < 2) {

                stringBuilder.append(0);

            }

            stringBuilder.append(hv);

        }

        return stringBuilder.toString();

    }

    public static byte[] HexStringToByteArray(String s) {

        int len = s.length();

        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {

            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)

                + Character.digit(s.charAt(i+1), 16));

        }

        return data;

    }


    /**
     * 两个byte[]数组相加
     * @param data1
     * @param data2
     * @return
     */
    public static byte[] add(byte[] data1, byte[] data2) {

        byte[] result = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, result, 0, data1.length);
        System.arraycopy(data2, 0, result, data1.length, data2.length);

        return result;
    }
}
