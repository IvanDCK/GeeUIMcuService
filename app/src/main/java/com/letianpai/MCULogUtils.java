package com.letianpai;


import android.util.Log;
import com.renhejia.robot.commandlib.utils.SystemUtil;


public class MCULogUtils {
    private static final String propLogKey = "geeui.log.mcuservice";
    public static void logi(String tag, String message) {
        String isLog = SystemUtil.get(propLogKey,"1");
        if (isLog.equals("1")){
            Log.i(tag, message);
        }
    }

    public static void logw(String tag, String message) {
        String isLog = SystemUtil.get(propLogKey,"0");
        if (isLog.equals("1")){
            Log.w(tag, message);
        }
    }

    public static void logd(String tag, String message) {
        String isLog = SystemUtil.get(propLogKey,"1");
        if (isLog.equals("1")){
            Log.d("larry", "tag:" + tag + "  message:" + message);
        }
    }

    public static void loge(String tag, String message) {
        String isLog = SystemUtil.get(propLogKey,"0");
        if (isLog.equals("1")){
            Log.e(tag, message);
        }
    }

    private static final int showLength = 1000;

    public static void showLargeLog(String tag, String logContent) {
        if (logContent.length() > showLength) {
            String show = logContent.substring(0, showLength);
            logd(tag, show);
            /*剩余的字符串如果大于规定显示的长度，截取剩余字符串进行递归，否则打印结果*/
            if ((logContent.length() - showLength) > showLength) {
                String partLog = logContent.substring(showLength, logContent.length());
                showLargeLog(tag, partLog);
            } else {
                String printLog = logContent.substring(showLength, logContent.length());
                logd(tag, printLog);
            }
        } else {
            logd(tag, logContent);
        }
    }
}
