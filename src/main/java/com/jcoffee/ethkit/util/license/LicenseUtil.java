package com.jcoffee.ethkit.util.license;

import com.jcoffee.ethkit.coin.util.FileUtil;
import com.jcoffee.ethkit.coin.util.TransactionUtil;
import com.jcoffee.ethkit.util.DateUtils;
import com.jcoffee.ethkit.util.WebDateUtils;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LicenseUtil {
    public static final String LICENSE_MESSAGE = "软件授权验证未通过，请在【帮助】中获取机器公钥提供给我们，我们给您生成授权文件！";
    @Autowired
    private TransactionUtil transactionUtil;

    private String desDecode(String str, String key) {
        String t = com.jcoffee.ethkit.util.license.EncryUtil.decrypt(str, key);
        return t;
    }

    public boolean checkLicense2() {
        return this.checkLicense("xxddxx");
    }

    private boolean checkLicense(String key) {
        String rootDir = this.transactionUtil.getRootDir();
        String licensePath = rootDir + "/config/license.txt";
        List list = FileUtil.getContentList(licensePath);
        if (!list.isEmpty()) {
            String license = (String)list.get(0);
            String[] arrs = StringUtils.split(license, ",");
            if (arrs == null || arrs.length != 3) {
                return false;
            }

            String t = this.desDecode(arrs[2], key);
            if (StringUtils.isBlank(t)) {
                return false;
            }

            t = this.desDecode(t, key);
            if (StringUtils.isBlank(t)) {
                return false;
            }

            String[] arr = StringUtils.split(t, ",");
            String dateStr = arr[0];
            Date date = DateUtils.parseDate(dateStr, (String)null);
            if (date == null) {
                return false;
            }

            String format = "yyyy-MM-dd HH:mm:ss";
            String curDateStr = WebDateUtils.getWebsiteDatetime("http://www.baidu.com", format);
            Date curDate = DateUtils.parseDate(curDateStr, (String)null);
            if (curDate.getTime() > date.getTime()) {
                System.err.println("软件授权已过期，到期时间:" + dateStr);
                return false;
            }

            Set macSet = PubKeyUtil.getAllSet();
            String[] var15 = arr;
            int var16 = arr.length;

            for(int var17 = 0; var17 < var16; ++var17) {
                String mac = var15[var17];
                if (macSet.contains(mac)) {
                    return true;
                }
            }
        }

        return true;
    }
}
