package com.jcoffee.ethkit.coin.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

public class FileUtil {
    public static Set getContentSet(String filePath) {
        HashSet contentSet = new HashSet();

        try {
            String encoding = "UTF-8";
            File file = new File(filePath);
            if (file.isFile() && file.exists()) {
                InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineStr = null;

                while((lineStr = bufferedReader.readLine()) != null) {
                    lineStr = StringUtils.trim(lineStr);
                    if (StringUtils.isNotBlank(lineStr)) {
                        contentSet.add(lineStr);
                    }
                }

                read.close();
            } else {
                System.err.println("找不到指定的文件," + filePath);
            }
        } catch (Exception var7) {
            System.err.println("读取文件内容出错");
            var7.printStackTrace();
        }

        return contentSet;
    }

    public static Set getContentSet2(String filePath) {
        HashSet contentSet = new HashSet();

        try {
            String encoding = "UTF-8";
            File file = new File(filePath);
            if (file.isFile() && file.exists()) {
                InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineStr = null;

                while((lineStr = bufferedReader.readLine()) != null) {
                    lineStr = StringUtils.trim(lineStr);
                    if (StringUtils.isNotBlank(lineStr)) {
                        contentSet.add(lineStr);
                    }
                }

                read.close();
            }
        } catch (Exception var7) {
            System.err.println("读取文件内容出错,file:" + filePath);
            var7.printStackTrace();
        }

        return contentSet;
    }

    public static List getContentList(String filePath) {
        ArrayList list = new ArrayList();

        try {
            String encoding = "UTF-8";
            File file = new File(filePath);
            if (file.isFile() && file.exists()) {
                InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineStr = null;

                while((lineStr = bufferedReader.readLine()) != null) {
                    lineStr = StringUtils.trim(lineStr);
                    if (StringUtils.isNotBlank(lineStr)) {
                        list.add(lineStr);
                    }
                }

                read.close();
            } else {
                System.err.println("找不到指定的文件," + filePath);
            }
        } catch (Exception var7) {
            System.err.println("读取文件内容出错");
            var7.printStackTrace();
        }

        return list;
    }

    public static List getContentList2(String filePath) {
        ArrayList list = new ArrayList();

        try {
            String encoding = "UTF-8";
            File file = new File(filePath);
            if (file.isFile() && file.exists()) {
                InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineStr = null;

                while((lineStr = bufferedReader.readLine()) != null) {
                    lineStr = StringUtils.trim(lineStr);
                    if (StringUtils.isNotBlank(lineStr)) {
                        list.add(lineStr);
                    }
                }

                read.close();
            }
        } catch (Exception var7) {
            var7.printStackTrace();
        }

        return list;
    }

    public static void writeToTxt(String filePath, String content) {
        FileWriter fw = null;

        try {
            File f = new File(filePath);
            fw = new FileWriter(f, true);
        } catch (IOException var6) {
            var6.printStackTrace();
        }

        PrintWriter pw = new PrintWriter(fw);
        pw.println(content);
        pw.flush();

        try {
            fw.flush();
            pw.close();
            fw.close();
        } catch (IOException var5) {
            var5.printStackTrace();
        }

    }
}
