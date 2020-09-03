package com.jcoffee.ethkit.common;

import java.util.Date;
import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

public class ResultMap extends HashMap {
    private static final long serialVersionUID = 8755792260930054771L;

    public String getValue(Object key) {
        Object o = super.get(key);
        if (o == null) {
            o = "";
        }

        return o + "";
    }

    public Integer getIntegerValue(Object key) {
        Object o = super.get(key);
        if (o == null) {
            o = "";
        }

        String result = o + "";
        return StringUtils.isNotBlank(result) ? Integer.parseInt(result) : null;
    }

    public boolean containsNoReplaceKey(Object key) {
        return super.containsKey(key);
    }

    public Object putNoReplace(Object key, Object value) {
        return super.put(key, value);
    }

    public Object get(Object key) {
        Object o = super.get(this.getReplaceMapKey(key));
        if (o == null) {
            o = "";
        }

        return o;
    }

    public boolean containsKey(Object key) {
        return super.containsKey(this.getReplaceMapKey(key));
    }

    public Object remove(Object key) {
        return super.remove(this.getReplaceMapKey(key));
    }

    public Object put(Object key, Object value) {
        if (this.containsKey(key)) {
            throw new IllegalArgumentException(" already contains value for " + key);
        } else if (value != null && value instanceof Date) {
            String date = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").format(value);
            return super.put(this.getReplaceMapKey(key), date);
        } else {
            return super.put(this.getReplaceMapKey(key), value);
        }
    }

    private String getReplaceMapKey(Object key) {
        return com.jcoffee.ethkit.common.ColumnConvert.getReplaceMapKey(key + "");
    }
}
