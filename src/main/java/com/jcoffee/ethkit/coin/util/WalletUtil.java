package com.jcoffee.ethkit.coin.util;

import com.jcoffee.ethkit.coin.pojo.WalletInfo;
import com.jcoffee.ethkit.common.JsonResult;
import com.jcoffee.ethkit.util.license.LicenseUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

@Component
public class WalletUtil {
    @Value("${action.master}")
    private String master;
    @Value("${action.slave}")
    private String slave;
    @Autowired
    private com.jcoffee.ethkit.coin.util.TransactionUtil transactionUtil;
    @Autowired
    private LicenseUtil licenseUtil;

    public static void main(String[] args) throws Exception {
    }

    public JsonResult createUTC(Integer createType, Integer walletType, String pwd, String path, int count, String keystore, String privatekey) throws Exception {
        String message = "";
        long s = System.currentTimeMillis();
        JsonResult jsonResult = new JsonResult();
        List list = new ArrayList();
        if (!this.licenseUtil.checkLicense2()) {
            message = "软件授权验证未通过，请在【帮助】中获取机器公钥提供给我们，我们给您生成授权文件！";
            jsonResult.setMessage(message);
            jsonResult.setSuccess(false);
            return jsonResult;
        } else if (createType == 2) {
            jsonResult = this.importWalletFromKeystore(walletType, pwd, keystore);
            return jsonResult;
        } else if (createType == 3) {
            jsonResult = this.importWalletFrom(walletType, privatekey);
            return jsonResult;
        } else {
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String address = null;
            String txtPath = path + "/1.txt";
            String rootDir = this.transactionUtil.getRootDir();
            String masterOrSlavePath = rootDir + this.master;
            if (walletType == 2) {
                masterOrSlavePath = rootDir + this.slave;
            }

            String walletTypeStr;
            for(int i = 0; i < count; ++i) {
                String utc = WalletUtils.generateFullNewWalletFile(pwd, dir);
                address = StringUtils.substringAfterLast(utc, "-");
                address = "0x" + StringUtils.removeEnd(address, ".json");
                address = StringUtils.lowerCase(address);
                File file = new File(path + "/" + utc);
                String newFilePath = path + "/" + address + ".json";
                FileUtils.copyFile(file, new File(newFilePath));
                walletTypeStr = masterOrSlavePath + "/" + address + ".json";
                FileUtils.copyFile(file, new File(walletTypeStr));
                FileUtils.deleteQuietly(file);
                System.err.println("创建钱包:" + address);
                com.jcoffee.ethkit.coin.util.FileUtil.writeToTxt(txtPath, address);
                WalletInfo walletInfo = new WalletInfo();
                walletInfo.setAddress(address);
                walletInfo.setBalance("");
                list.add(walletInfo);
            }

            long e = System.currentTimeMillis();
            long t = (e - s) / 1000L;
            walletTypeStr = "主";
            if (walletType == 2) {
                walletTypeStr = "次";
            }

            message = "本次共创建" + count + "个" + walletTypeStr + "钱包，用时" + t + "秒";
            System.err.println(message);
            jsonResult.setMessage(message);
            jsonResult.setSuccess(true);
            jsonResult.setData(list);
            return jsonResult;
        }
    }

    public JsonResult importWalletFromKeystore(Integer walletType, String pwd, String keystore) {
        String message = "";
        String rootDir = this.transactionUtil.getRootDir();
        JsonResult jsonResult = new JsonResult();
        File file = null;

        String address;
        try {
            String tmpFile = rootDir + "/eth/tmp.json";
            file = new File(tmpFile);
            if (file.exists()) {
                file.delete();
            }

            com.jcoffee.ethkit.coin.util.FileUtil.writeToTxt(tmpFile, keystore);
            WalletUtils.loadCredentials(pwd, tmpFile);
        } catch (Exception var19) {
            var19.printStackTrace();
            address = var19.getMessage();
            if (StringUtils.contains(address, "Invalid password provided")) {
                message = "执行失败，密码错误," + var19.getMessage();
            } else {
                message = "执行失败，keystore验证未通过," + var19.getMessage();
            }

            System.err.println(message);
            jsonResult.setMessage(message);
            jsonResult.setSuccess(false);
            return jsonResult;
        }

        JSONObject object = JSON.parseObject(keystore);
        address = "0x" + object.getString("address");
        String masterPath = rootDir + this.master;
        String slavePath = rootDir + this.slave;
        File m = new File(masterPath + "/" + address + ".json");
        if (m.exists()) {
            message = "该钱包已经在主钱包列表中，无需导入";
            System.err.println(message);
            jsonResult.setMessage(message);
            jsonResult.setSuccess(false);
            return jsonResult;
        } else {
            File s = new File(slavePath + "/" + address + ".json");
            if (s.exists()) {
                message = "该钱包在已经在次钱包列表中，无需导入";
                System.err.println(message);
                jsonResult.setMessage(message);
                jsonResult.setSuccess(false);
                return jsonResult;
            } else {
                String fileDir = "";
                String walletTypeStr = "";
                if (walletType == 1) {
                    fileDir = masterPath;
                    walletTypeStr = "主";
                } else {
                    fileDir = slavePath;
                    walletTypeStr = "次";
                }

                String keystoreFilePath = fileDir + "/" + address + ".json";
                com.jcoffee.ethkit.coin.util.FileUtil.writeToTxt(keystoreFilePath, keystore);
                if (file != null) {
                    FileUtils.deleteQuietly(file);
                }

                message = "成功将地址为【" + address + "】的钱包导入,请手动刷新" + walletTypeStr + "钱包列表";
                System.err.println(message);
                jsonResult.setMessage(message);
                jsonResult.setSuccess(true);
                List list = new ArrayList();
                WalletInfo walletInfo = new WalletInfo();
                walletInfo.setAddress(address);
                walletInfo.setBalance("");
                list.add(walletInfo);
                jsonResult.setData(list);
                return jsonResult;
            }
        }
    }

    public JsonResult importWalletFrom(Integer walletType, String privatekey) {
        String message = "";
        String rootDir = this.transactionUtil.getRootDir();
        JsonResult jsonResult = new JsonResult();
        privatekey = StringUtils.trim(privatekey);
        privatekey = StringUtils.replace(privatekey, " ", "");
        privatekey = StringUtils.replace(privatekey, "：", ":");
        privatekey = StringUtils.lowerCase(privatekey);
        if (!StringUtils.contains(privatekey, ":") && !StringUtils.contains(privatekey, "|")) {
            message = "格式错误，请检查(错误代码：001)";
            System.err.println(message);
            jsonResult.setMessage(message);
            jsonResult.setSuccess(false);
            return jsonResult;
        } else {
            String[] groupArr = StringUtils.split(privatekey, "|");
            if (groupArr == null) {
                message = "格式错误，请检查(错误代码：002)";
                System.err.println(message);
                jsonResult.setMessage(message);
                jsonResult.setSuccess(false);
                return jsonResult;
            } else {
                Map addrPkMap = new HashMap();
                String[] var8 = groupArr;
                int var9 = groupArr.length;

                String fileDir;
                String address;
                for(int var10 = 0; var10 < var9; ++var10) {
                    fileDir = var8[var10];
                    String[] addrPkArr = StringUtils.split(fileDir, ":");
                    if (addrPkArr == null || addrPkArr.length != 2) {
                        message = "格式错误，请检查(错误代码：003)";
                        System.err.println(message);
                        jsonResult.setMessage(message);
                        jsonResult.setSuccess(false);
                        return jsonResult;
                    }

                    String addr = addrPkArr[0];
                    boolean isAddr = WalletUtils.isValidAddress(addr);
                    if (!isAddr) {
                        message = "【" + addr + "】不是合法的钱包地址，请检查";
                        System.err.println(message);
                        jsonResult.setMessage(message);
                        jsonResult.setSuccess(false);
                        return jsonResult;
                    }

                    address = addrPkArr[1];
                    Credentials credentials = null;

                    try {
                        credentials = Credentials.create(address);
                        String tmpAddr = credentials.getAddress();
                        System.err.println("tmpAddr:" + tmpAddr);
                        if (!StringUtils.equalsIgnoreCase(tmpAddr, addr)) {
                            message = "私钥【" + address + "】解析得到的地址是【" + tmpAddr + "】,与提供的地址【" + addr + "】不匹配，请检查";
                            jsonResult.setMessage(message);
                            jsonResult.setSuccess(false);
                            return jsonResult;
                        }

                        addrPkMap.put(addr, address);
                    } catch (Exception var24) {
                        var24.printStackTrace();
                        System.err.println(var24.getMessage());
                        message = "私钥【" + address + "】验证失败，请检查私钥是否正确，" + var24.getMessage();
                        jsonResult.setMessage(message);
                        jsonResult.setSuccess(false);
                        return jsonResult;
                    }
                }

                String masterPath = rootDir + this.master;
                String slavePath = rootDir + this.slave;
                List list = new ArrayList();
                fileDir = "";
                String walletTypeStr = "";
                if (walletType == 1) {
                    fileDir = masterPath;
                    walletTypeStr = "主";
                } else {
                    fileDir = slavePath;
                    walletTypeStr = "次";
                }

                Iterator var29 = addrPkMap.entrySet().iterator();

                while(true) {
                    while(true) {
                        while(var29.hasNext()) {
                            Entry entry = (Entry)var29.next();
                            address = ((String)entry.getKey()).toString();
                            String pk = ((String)entry.getValue()).toString();
                            File m = new File(masterPath + "/" + address + ".json");
                            File m2 = new File(masterPath + "/" + address + "_pk.json");
                            if (!m.exists() && !m2.exists()) {
                                File s = new File(slavePath + "/" + address + ".json");
                                File s2 = new File(slavePath + "/" + address + "_pk.json");
                                if (!s.exists() && !s2.exists()) {
                                    String content = address + ":" + pk;
                                    String filePath = fileDir + "/" + address + "_pk.json";
                                    com.jcoffee.ethkit.coin.util.FileUtil.writeToTxt(filePath, content);
                                    WalletInfo walletInfo = new WalletInfo();
                                    walletInfo.setAddress(address + "导入" + walletTypeStr + "成功");
                                    list.add(walletInfo);
                                } else {
                                    message = "该钱包在已经在次钱包列表中，无需导入";
                                    System.err.println(message);
                                    WalletInfo walletInfo = new WalletInfo();
                                    walletInfo.setAddress(address + "在次钱包已存在，无需导入");
                                    list.add(walletInfo);
                                }
                            } else {
                                message = "钱包【" + address + "】已经在主钱包列表中，无需导入";
                                System.err.println(message);
                                WalletInfo walletInfo = new WalletInfo();
                                walletInfo.setAddress(address + "在主钱包中已存在，无需导入");
                                list.add(walletInfo);
                            }
                        }

                        message = "成功完成" + list.size() + "个钱包导入请求,请手动刷新" + walletTypeStr + "钱包列表";
                        System.err.println(message);
                        jsonResult.setMessage(message);
                        jsonResult.setSuccess(true);
                        jsonResult.setData(list);
                        return jsonResult;
                    }
                }
            }
        }
    }

    public JsonResult moveWallet(Integer walletType, List addressList) throws Exception {
        String message = "";
        JsonResult jsonResult = new JsonResult();
        if (!this.licenseUtil.checkLicense2()) {
            System.err.println("软件授权验证未通过，请在【帮助】中获取机器公钥提供给我们，我们给您生成授权文件！");
            jsonResult.setMessage(message);
            jsonResult.setSuccess(false);
            return jsonResult;
        } else {
            String rootDir = this.transactionUtil.getRootDir();
            String masterPath = rootDir + this.master;
            String slavePath = rootDir + this.slave;
            String fileDir = "";
            String walletTypeStr = "";
            if (walletType == 1) {
                fileDir = slavePath;
                walletTypeStr = "次";
            } else {
                fileDir = masterPath;
                walletTypeStr = "主";
            }

            File walletDir = new File(fileDir);
            File[] array = walletDir.listFiles();
            String addr = "";
            String fileName = "";
            String filePath = "";
            Iterator var15 = addressList.iterator();

            while(true) {
                while(var15.hasNext()) {
                    String tmpAddr = (String)var15.next();
                    addr = StringUtils.removeStart(tmpAddr, "0x");
                    File[] var17 = array;
                    int var18 = array.length;

                    for(int var19 = 0; var19 < var18; ++var19) {
                        File tmpFile = var17[var19];
                        filePath = tmpFile.getAbsolutePath();
                        filePath = StringUtils.replace(filePath, "\\", "/");
                        if (StringUtils.containsIgnoreCase(filePath, addr)) {
                            File sourceFile = new File(filePath);
                            fileName = StringUtils.substringAfterLast(filePath, "/");
                            System.err.println("fileName:" + fileName);
                            if (walletType == 1) {
                                FileUtils.copyFile(sourceFile, new File(masterPath + "/" + fileName));
                                FileUtils.deleteQuietly(sourceFile);
                            } else {
                                FileUtils.copyFile(sourceFile, new File(slavePath + "/" + fileName));
                                FileUtils.deleteQuietly(sourceFile);
                            }
                            break;
                        }
                    }
                }

                message = "本次共移动" + addressList.size() + "个" + walletTypeStr + "钱包,请手动刷新" + walletTypeStr + "钱包列表";
                System.err.println(message);
                jsonResult.setMessage(message);
                jsonResult.setSuccess(true);
                jsonResult.setData(addressList);
                return jsonResult;
            }
        }
    }
}
