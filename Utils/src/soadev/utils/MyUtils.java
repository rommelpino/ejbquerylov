package soadev.utils;

import java.lang.reflect.Method;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;

public class MyUtils {
    
    public static Object getProperty(Object object, String property) {
        try {
            Class clazz = object.getClass();
            Method accessor =
                clazz.getMethod(getAccessorMethodName(property), new Class[0]);
            return accessor.invoke(object);
        } catch (Exception e) {
            // TODO: Add better catch code
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    public static String getAccessorMethodName(String property) {
        StringBuilder builder = new StringBuilder("get");
        builder.append(Character.toUpperCase(property.charAt(0)));
        builder.append(property.substring(1));
        return builder.toString();
    }
    
    public static Date removeTimeElement(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    
    public static Date addOneDay(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.roll(Calendar.DATE, true);
        return cal.getTime();
    }
    
    
    public static boolean unbox(Boolean value){
        if(value == null){
            return false;
        }
        return value;
    }
    
      
}
