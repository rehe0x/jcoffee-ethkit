package com.jcoffee.ethkit.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class DateUtils {
    public static Date getNowDate() {
        Calendar c = Calendar.getInstance();
        return c.getTime();
    }

    public static Date parseDate(String date, String dateFormat) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (StringUtils.isNoneBlank(new CharSequence[]{dateFormat})) {
                format = new SimpleDateFormat(dateFormat);
            }

            return format.parse(date);
        } catch (ParseException var3) {
            var3.printStackTrace();
            return null;
        }
    }

    public static List getMonthListBetween(String minDate, String maxDate) throws ParseException {
        minDate = minDate + "-01";
        maxDate = maxDate + "-01";
        ArrayList result = new ArrayList();
        SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        Date date1 = null;
        Date date2 = null;

        try {
            date1 = yyyyMMdd.parse(minDate);
            date2 = yyyyMMdd.parse(maxDate);
            Calendar c1 = Calendar.getInstance();
            Calendar c2 = Calendar.getInstance();
            c1.setTime(date1);
            c2.setTime(date2);

            while(c1.compareTo(c2) <= 0) {
                Date ss = c1.getTime();
                String str = sdf.format(ss);
                result.add(str);
                c1.add(2, 1);
            }
        } catch (ParseException var11) {
            var11.printStackTrace();
        }

        return result;
    }

    public static Date parseStartDate(String date) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            date = date + " 00:00:00";
            return format.parse(date);
        } catch (ParseException var2) {
            var2.printStackTrace();
            return null;
        }
    }

    public static Date parseEndDate(String date) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            date = date + " 23:59:59";
            return format.parse(date);
        } catch (ParseException var2) {
            var2.printStackTrace();
            return null;
        }
    }

    public static Map getMonthDay(int month) {
        Map result = new HashMap();
        Calendar c = Calendar.getInstance();
        c.add(2, month);
        c.set(5, 1);
        c.set(11, 0);
        c.set(12, 0);
        c.set(13, 0);
        Date startTime = c.getTime();
        c.set(5, c.getActualMaximum(5));
        c.set(11, 23);
        c.set(12, 59);
        c.set(13, 59);
        Date endTime = c.getTime();
        result.put("startTime", startTime);
        result.put("endTime", endTime);
        return result;
    }

    public static Date getMonthLastDay(int month) {
        Calendar c = Calendar.getInstance();
        c.add(2, month);
        c.set(5, c.getActualMaximum(5));
        c.set(11, 23);
        c.set(12, 59);
        c.set(13, 59);
        Date endTime = c.getTime();
        return endTime;
    }

    public static Date getMonthFirstDay(int month) {
        Calendar c = Calendar.getInstance();
        c.add(2, month);
        c.set(5, 1);
        c.set(11, 0);
        c.set(12, 0);
        c.set(13, 0);
        Date startTime = c.getTime();
        return startTime;
    }

    public static Map getYearDay(int year) {
        Map result = new HashMap();
        Calendar c = Calendar.getInstance();
        c.add(1, year);
        c.set(6, 1);
        c.set(11, 0);
        c.set(12, 0);
        c.set(13, 0);
        Date startTime = c.getTime();
        c.set(6, c.getActualMaximum(6));
        c.set(11, 23);
        c.set(12, 59);
        c.set(13, 59);
        Date endTime = c.getTime();
        result.put("startTime", startTime);
        result.put("endTime", endTime);
        return result;
    }

    public static Map getWeekDay(int week) {
        Map result = new HashMap();
        Calendar c = Calendar.getInstance();
        c.add(3, week);
        c.set(7, 1);
        c.set(11, 0);
        c.set(12, 0);
        c.set(13, 0);
        c.add(6, 1);
        Date startTime = c.getTime();
        c.set(7, c.getActualMaximum(7));
        c.set(11, 23);
        c.set(12, 59);
        c.set(13, 59);
        c.add(6, 1);
        Date endTime = c.getTime();
        result.put("startTime", startTime);
        result.put("endTime", endTime);
        return result;
    }

    public static Map getDay(int day) {
        Map result = new HashMap();
        Calendar c = Calendar.getInstance();
        c.add(6, day);
        c.set(11, 0);
        c.set(12, 0);
        c.set(13, 0);
        Date startTime = c.getTime();
        c.set(11, 23);
        c.set(12, 59);
        c.set(13, 59);
        Date endTime = c.getTime();
        result.put("startTime", startTime);
        result.put("endTime", endTime);
        return result;
    }

    public static Date getStartByDay(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(11, 0);
        c.set(12, 0);
        c.set(13, 0);
        return c.getTime();
    }

    public static Date getEndByDay(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(11, 23);
        c.set(12, 59);
        c.set(13, 59);
        return c.getTime();
    }

    public static String fomatToTimeString(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return date != null ? format.format(date) : null;
    }

    public static String fomatToTimeStr(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        return date != null ? format.format(date) : null;
    }

    public static String fomatToTimeStr(Date date, String formatStr) {
        SimpleDateFormat format = new SimpleDateFormat(formatStr);
        return date != null ? format.format(date) : null;
    }

    public static String formatToDateString(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return date != null ? format.format(date) : null;
    }

    public static String getMonthBegin(Date date) {
        if (date != null) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(5, 1);
            cal.set(11, 0);
            cal.set(12, 0);
            cal.set(13, 0);
            return format.format(cal.getTime());
        } else {
            return null;
        }
    }

    public static Date getMonthBegin_date(Date date) throws ParseException {
        if (date != null) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(5, 1);
            cal.set(11, 0);
            cal.set(12, 0);
            cal.set(13, 0);
            String result = format.format(cal.getTime());
            Date resultdate = format.parse(result);
            return resultdate;
        } else {
            return null;
        }
    }

    public static Date getMonthEnd_date(Date date) throws ParseException {
        if (date != null) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(5, cal.getActualMaximum(5));
            cal.set(11, 23);
            cal.set(12, 59);
            cal.set(13, 59);
            String result = format.format(cal.getTime());
            Date resultdate = format.parse(result);
            return resultdate;
        } else {
            return null;
        }
    }

    public static String getMonthEnd(Date date) {
        if (date != null) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(5, cal.getActualMaximum(5));
            cal.set(11, 23);
            cal.set(12, 59);
            cal.set(13, 59);
            return format.format(cal.getTime());
        } else {
            return null;
        }
    }

    public static int daysBetween(Date startdate, Date enddate) throws ParseException {
        if (startdate != null && enddate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            startdate = sdf.parse(sdf.format(startdate));
            enddate = sdf.parse(sdf.format(enddate));
            Calendar cal = Calendar.getInstance();
            cal.setTime(startdate);
            long time1 = cal.getTimeInMillis();
            cal.setTime(enddate);
            long time2 = cal.getTimeInMillis();
            long between_days = (time2 - time1) / 86400000L;
            return Integer.parseInt(String.valueOf(between_days));
        } else {
            return 0;
        }
    }

    public static String formateDate(Date date, String dateFormat) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (StringUtils.isNoneBlank(new CharSequence[]{dateFormat})) {
                format = new SimpleDateFormat(dateFormat);
            }

            return format.format(date);
        } catch (Exception var3) {
            var3.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.err.println(format.format(getStartByDay(new Date())));
        System.err.println(format.format(getEndByDay(new Date())));
        System.err.println(format.format((Date)getYearDay(-1).get("startTime")));
        System.err.println(format.format((Date)getYearDay(0).get("endTime")));
        Map day = getWeekDay(-1);
        System.err.println(format.format((Date)day.get("startTime")));
        System.err.println(format.format((Date)day.get("endTime")));
        Map day1 = getDay(-1);
        System.err.println(format.format((Date)day1.get("startTime")));
        System.err.println(format.format((Date)day1.get("endTime")));
    }
}
