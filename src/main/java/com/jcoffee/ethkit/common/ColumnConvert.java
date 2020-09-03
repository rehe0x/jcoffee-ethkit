package com.jcoffee.ethkit.common;

import java.util.HashMap;
import java.util.Map;

public class ColumnConvert {
    private static final Map colums = new HashMap();

    public static String getReplaceMapKey(String key) {
        if (colums.containsKey(key)) {
            return (String)colums.get(key);
        } else {
            String columnName = columnToprotected(key);
            colums.put(key, columnName);
            return columnName;
        }
    }

    public static String getClassPath() {
        return ColumnConvert.class.getClassLoader().getResource("/").getFile();
    }

    private static String columnToprotected(String columnName) {
        columnName = columnName.toLowerCase();
        String[] names = columnName.split("_");
        if (names.length == 1) {
            return names[0];
        } else {
            StringBuilder sb = new StringBuilder(names[0]);

            for(int i = 1; i < names.length; ++i) {
                sb.append(coverFirstCharToUp(names[i]));
            }

            return sb.toString();
        }
    }

    private static String coverFirstCharToUp(String s) {
        String first = s.substring(0, 1);
        return first.toUpperCase() + s.substring(1);
    }
}
