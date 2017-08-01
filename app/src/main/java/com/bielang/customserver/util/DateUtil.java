package com.bielang.customserver.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 获取时间工具类
 * Created by Daylight on 2017/7/6.
 */

public class DateUtil {
    private static final SimpleDateFormat DATE_FORMAT1=new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final SimpleDateFormat DATE_FORMAT2=new SimpleDateFormat("昨天", Locale.getDefault());
    private static final SimpleDateFormat DATE_FORMAT3=new SimpleDateFormat("M月dd日", Locale.getDefault());
    private static final SimpleDateFormat DATE_FORMAT4=new SimpleDateFormat("yyyy年M月dd日HH:mm",Locale.getDefault());
    public static String getTime(int Format){
        SimpleDateFormat mFormat;
        switch (Format){
            case 1:
                mFormat=DATE_FORMAT1;
                break;
            case 2:
                mFormat=DATE_FORMAT2;
                break;
            case 3:
                mFormat=DATE_FORMAT3;
                break;
            default:
                mFormat=DATE_FORMAT4;
                break;
        }
        Date date=new Date();
        return mFormat.format(date);
    }
    public static String getTime(Date date,int Format){
        SimpleDateFormat mFormat;
        switch (Format){
            case 1:
                mFormat=DATE_FORMAT1;
                break;
            case 2:
                mFormat=DATE_FORMAT2;
                break;
            case 3:
                mFormat=DATE_FORMAT3;
                break;
            default:
                mFormat=DATE_FORMAT4;
                break;
        }
        return mFormat.format(date);
    }
    public static Date getDate(){
        return new Date();
    }
    public static int getYear(){
        Date date=new Date();
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }
    public static int getYear(Date date){
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }
    public static int getMon(){
        Date date=new Date();
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MONTH);
    }
    public static int getMon(Date date){
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MONTH);
    }
    public static int getDay(){
        Date date=new Date();
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }
    public static int getDay(Date date){
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }
    public static int getHour(){
        Date date=new Date();
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.HOUR);
    }
    public static int getHour(Date date){
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.HOUR);
    }
    public static int getMin(){
        Date date=new Date();
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MINUTE);
    }
    public static int getMin(Date date){
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MINUTE);
    }
    public static String getWeekDay(Date date){
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(date);
        switch (calendar.get(Calendar.DAY_OF_WEEK)){
            case 2:
                return "星期一";
            case 3:
                return "星期二";
            case 4:
                return "星期三";
            case 5:
                return "星期四";
            case 6:
                return "星期五";
            case 7:
                return "星期六";
            case 1:
                return "星期日";
            default:
                return null;
        }
    }
    public static String AutoTransFormat(Date date){
        if (getDay() - getDay(date) == 1)
            return getTime(date,2);
        else if (getDay() - getDay(date) > 5
                || getMon() != getMon(date)
                || getYear() != getYear(date))
            return getTime(date,3);
        else if (getDay()-getDay(date)>1&&getDay()-getDay(date)<=5)
            return getWeekDay(date);
        return getTime(date,1);
    }
}
