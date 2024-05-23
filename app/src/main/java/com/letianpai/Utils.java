package com.letianpai;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.provider.Settings;
import android.text.TextUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public final class Utils {
    private static final ExecutorService UTIL_POOL = Executors.newFixedThreadPool(3);
    static final Handler UTIL_HANDLER = new Handler(Looper.getMainLooper());
    @SuppressLint({"StaticFieldLeak"})
    private static Application sApplication;

    private Utils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public static Application getApp() {
        return sApplication != null ? sApplication : getApplicationByReflect();
    }

    public static float getCpuThermal() {

        List<String> result = new ArrayList<>();
        BufferedReader br = null;
        float temp = 0;

        try {
            File dir = new File("/sys/class/thermal/");

            File[] files = dir.listFiles(file -> {
                if (Pattern.matches("thermal_zone[0-9]+", file.getName())) {
                    return true;
                }
                return false;
            });
            final int SIZE = files.length;
            String line;
            for (int i = 0; i < SIZE; i++) {
                br = new BufferedReader(new FileReader("/sys/class/thermal/thermal_zone" + i + "/temp"));
                line = br.readLine();
                if (line != null) {
                    long temperature = Long.parseLong(line);
                    if (temperature < 0) {
                        temp = -1f;
                        return temp;
                    } else {
                        temp = (float) (temperature / 1000.0);
                        return temp;
                    }
                }
            }
            return temp;
        } catch (FileNotFoundException e) {
            result.add(e.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return temp;
    }


    /**
     * 5.修改Setting 中屏幕亮度值
     *
     * 修改Setting的值需要动态申请权限 <uses-permission
     * android:name="android.permission.WRITE_SETTINGS"/>
     * **/
    private static void ModifySettingsScreenBrightness(Context context,
                                                int birghtessValue) {
        // 首先需要设置为手动调节屏幕亮度模式
        setScreenManualMode(context);
        ContentResolver contentResolver = context.getContentResolver();
        Settings.System.putInt(contentResolver,
                Settings.System.SCREEN_BRIGHTNESS, birghtessValue);
    }

    /**
     * 3.关闭光感，设置手动调节背光模式
     *
     * SCREEN_BRIGHTNESS_MODE_AUTOMATIC 自动调节屏幕亮度模式值为1
     *
     * SCREEN_BRIGHTNESS_MODE_MANUAL 手动调节屏幕亮度模式值为0
     * **/
    public static void setScreenManualMode(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        try {
            int mode = Settings.System.getInt(contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
            if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                Settings.System.putInt(contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    static boolean isAppForeground() {
        ActivityManager am = (ActivityManager)getApp().getSystemService("activity");
        if (am == null) {
            return false;
        } else {
            List<ActivityManager.RunningAppProcessInfo> info = am.getRunningAppProcesses();
            if (info != null && info.size() != 0) {
                Iterator var2 = info.iterator();

                ActivityManager.RunningAppProcessInfo aInfo;
                do {
                    if (!var2.hasNext()) {
                        return false;
                    }

                    aInfo = (ActivityManager.RunningAppProcessInfo)var2.next();
                } while(aInfo.importance != 100 || !aInfo.processName.equals(getApp().getPackageName()));

                return true;
            } else {
                return false;
            }
        }
    }

    static <T> Task<T> doAsync(Task<T> task) {
        UTIL_POOL.execute(task);
        return task;
    }

    public static void runOnUiThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            UTIL_HANDLER.post(runnable);
        }

    }

    public static void runOnUiThreadDelayed(Runnable runnable, long delayMillis) {
        UTIL_HANDLER.postDelayed(runnable, delayMillis);
    }

    static String getCurrentProcessName() {
        String name = getCurrentProcessNameByFile();
        if (!TextUtils.isEmpty(name)) {
            return name;
        } else {
            name = getCurrentProcessNameByAms();
            if (!TextUtils.isEmpty(name)) {
                return name;
            } else {
                name = getCurrentProcessNameByReflect();
                return name;
            }
        }
    }

    private static String getCurrentProcessNameByFile() {
        try {
            File file = new File("/proc/" + Process.myPid() + "/cmdline");
            BufferedReader mBufferedReader = new BufferedReader(new FileReader(file));
            String processName = mBufferedReader.readLine().trim();
            mBufferedReader.close();
            return processName;
        } catch (Exception var3) {
            var3.printStackTrace();
            return "";
        }
    }

    private static String getCurrentProcessNameByAms() {
        ActivityManager am = (ActivityManager)getApp().getSystemService("activity");
        if (am == null) {
            return "";
        } else {
            List<ActivityManager.RunningAppProcessInfo> info = am.getRunningAppProcesses();
            if (info != null && info.size() != 0) {
                int pid = Process.myPid();
                Iterator var3 = info.iterator();

                ActivityManager.RunningAppProcessInfo aInfo;
                do {
                    if (!var3.hasNext()) {
                        return "";
                    }

                    aInfo = (ActivityManager.RunningAppProcessInfo)var3.next();
                } while(aInfo.pid != pid || aInfo.processName == null);

                return aInfo.processName;
            } else {
                return "";
            }
        }
    }

    private static String getCurrentProcessNameByReflect() {
        String processName = "";

        try {
            Application app = getApp();
            Field loadedApkField = app.getClass().getField("mLoadedApk");
            loadedApkField.setAccessible(true);
            Object loadedApk = loadedApkField.get(app);
            Field activityThreadField = loadedApk.getClass().getDeclaredField("mActivityThread");
            activityThreadField.setAccessible(true);
            Object activityThread = activityThreadField.get(loadedApk);
            Method getProcessName = activityThread.getClass().getDeclaredMethod("getProcessName");
            processName = (String)getProcessName.invoke(activityThread);
        } catch (Exception var7) {
            var7.printStackTrace();
        }

        return processName;
    }

    private static Application getApplicationByReflect() {
        try {
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Object thread = activityThread.getMethod("currentActivityThread").invoke((Object)null);
            Object app = activityThread.getMethod("getApplication").invoke(thread);
            if (app == null) {
                throw new NullPointerException("u should init first");
            }

            return (Application)app;
        } catch (NoSuchMethodException var3) {
            var3.printStackTrace();
        } catch (IllegalAccessException var4) {
            var4.printStackTrace();
        } catch (InvocationTargetException var5) {
            var5.printStackTrace();
        } catch (ClassNotFoundException var6) {
            var6.printStackTrace();
        }

        throw new NullPointerException("u should init first");
    }

    public interface Callback<T> {
        void onCall(T var1);
    }

    public abstract static class Task<Result> implements Runnable {
        private static final int NEW = 0;
        private static final int COMPLETING = 1;
        private static final int CANCELLED = 2;
        private static final int EXCEPTIONAL = 3;
        private volatile int state = 0;
        private Callback<Result> mCallback;

        abstract Result doInBackground();

        public Task(Callback<Result> callback) {
            this.mCallback = callback;
        }

        public void run() {
            try {
                final Result t = this.doInBackground();
                if (this.state != 0) {
                    return;
                }

                this.state = 1;
                Utils.UTIL_HANDLER.post(new Runnable() {
                    public void run() {
                        Task.this.mCallback.onCall(t);
                    }
                });
            } catch (Throwable var2) {
                if (this.state != 0) {
                    return;
                }

                this.state = 3;
            }

        }

        public void cancel() {
            this.state = 2;
        }

        public boolean isDone() {
            return this.state != 0;
        }

        public boolean isCanceled() {
            return this.state == 2;
        }
    }
}

