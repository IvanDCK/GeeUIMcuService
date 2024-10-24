package com.letianpai

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.text.TextUtils
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.regex.Pattern

class Utils private constructor() {
    init {
        throw UnsupportedOperationException("u can't instantiate me...")
    }

    interface Callback<T> {
        fun onCall(var1: T)
    }

    abstract class Task<Result>(callback: Callback<Result?>) : Runnable {
        @Volatile
        private var state = 0
        private val mCallback: Callback<Result?> = callback

        abstract fun doInBackground(): Result

        override fun run() {
            try {
                val t = this.doInBackground()
                if (this.state != 0) {
                    return
                }

                this.state = 1
                UTIL_HANDLER.post {
                    mCallback.onCall(
                        t
                    )
                }
            } catch (var2: Throwable) {
                if (this.state != 0) {
                    return
                }

                this.state = 3
            }
        }

        fun cancel() {
            this.state = 2
        }

        val isDone: Boolean
            get() = this.state != 0

        val isCanceled: Boolean
            get() = this.state == 2

        companion object {
            private const val NEW = 0
            private const val COMPLETING = 1
            private const val CANCELLED = 2
            private const val EXCEPTIONAL = 3
        }
    }

    companion object {
        private val UTIL_POOL: ExecutorService = Executors.newFixedThreadPool(3)
        val UTIL_HANDLER: Handler = Handler(Looper.getMainLooper())

        @SuppressLint("StaticFieldLeak")
        private val sApplication: Application? = null

        val app: Application
            get() = sApplication ?: applicationByReflect

        @JvmStatic
        val cpuThermal: Float
            get() {
                val result: MutableList<String> =
                    ArrayList()
                var br: BufferedReader? = null
                var temp = 0f

                try {
                    val dir = File("/sys/class/thermal/")

                    val files =
                        dir.listFiles { file: File ->
                            if (Pattern.matches(
                                    "thermal_zone[0-9]+",
                                    file.name
                                )
                            ) {
                                return@listFiles true
                            }
                            false
                        }
                    val SIZE = files!!.size
                    var line: String?
                    for (i in 0 until SIZE) {
                        br =
                            BufferedReader(FileReader("/sys/class/thermal/thermal_zone$i/temp"))
                        line = br.readLine()
                        if (line != null) {
                            val temperature = line.toLong()
                            if (temperature < 0) {
                                temp = -1f
                                return temp
                            } else {
                                temp = (temperature / 1000.0).toFloat()
                                return temp
                            }
                        }
                    }
                    return temp
                } catch (e: FileNotFoundException) {
                    result.add(e.toString())
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    if (br != null) {
                        try {
                            br.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
                return temp
            }


        /**
         * 5.修改Setting 中屏幕亮度值
         *
         * 修改Setting的值需要动态申请权限 <uses-permission android:name="android.permission.WRITE_SETTINGS"></uses-permission>
         */
        private fun ModifySettingsScreenBrightness(
            context: Context,
            birghtessValue: Int
        ) {
            // 首先需要设置为手动调节屏幕亮度模式
            setScreenManualMode(context)
            val contentResolver = context.contentResolver
            Settings.System.putInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS, birghtessValue
            )
        }

        /**
         * 3.关闭光感，设置手动调节背光模式
         *
         * SCREEN_BRIGHTNESS_MODE_AUTOMATIC 自动调节屏幕亮度模式值为1
         *
         * SCREEN_BRIGHTNESS_MODE_MANUAL 手动调节屏幕亮度模式值为0
         */
        fun setScreenManualMode(context: Context) {
            val contentResolver = context.contentResolver
            try {
                val mode = Settings.System.getInt(
                    contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE
                )
                if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                    Settings.System.putInt(
                        contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                    )
                }
            } catch (e: SettingNotFoundException) {
                e.printStackTrace()
            }
        }

        val isAppForeground: Boolean
            get() {
                val am =
                    app.getSystemService("activity") as ActivityManager
                if (am == null) {
                    return false
                } else {
                    val info =
                        am.runningAppProcesses
                    if (info != null && info.size != 0) {
                        val var2: Iterator<*> = info.iterator()

                        var aInfo: RunningAppProcessInfo
                        do {
                            if (!var2.hasNext()) {
                                return false
                            }

                            aInfo = var2.next() as RunningAppProcessInfo
                        } while (aInfo.importance != 100 || aInfo.processName != app.packageName)

                        return true
                    } else {
                        return false
                    }
                }
            }

        fun <T> doAsync(task: Task<T>): Task<T> {
            UTIL_POOL.execute(task)
            return task
        }

        fun runOnUiThread(runnable: Runnable) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                runnable.run()
            } else {
                UTIL_HANDLER.post(runnable)
            }
        }

        fun runOnUiThreadDelayed(runnable: Runnable, delayMillis: Long) {
            UTIL_HANDLER.postDelayed(runnable, delayMillis)
        }

        val currentProcessName: String?
            get() {
                var name: String? = currentProcessNameByFile
                if (!TextUtils.isEmpty(name)) {
                    return name
                } else {
                    name = currentProcessNameByAms
                    if (!TextUtils.isEmpty(name)) {
                        return name
                    } else {
                        name = currentProcessNameByReflect
                        return name
                    }
                }
            }

        private val currentProcessNameByFile: String
            get() {
                try {
                    val file =
                        File("/proc/" + Process.myPid() + "/cmdline")
                    val mBufferedReader =
                        BufferedReader(FileReader(file))
                    val processName = mBufferedReader.readLine().trim { it <= ' ' }
                    mBufferedReader.close()
                    return processName
                } catch (var3: Exception) {
                    var3.printStackTrace()
                    return ""
                }
            }

        private val currentProcessNameByAms: String
            get() {
                val am =
                    app.getSystemService("activity") as ActivityManager
                if (am == null) {
                    return ""
                } else {
                    val info =
                        am.runningAppProcesses
                    if (info != null && info.size != 0) {
                        val pid = Process.myPid()
                        val var3: Iterator<*> = info.iterator()

                        var aInfo: RunningAppProcessInfo
                        do {
                            if (!var3.hasNext()) {
                                return ""
                            }

                            aInfo = var3.next() as RunningAppProcessInfo
                        } while (aInfo.pid != pid || aInfo.processName == null)

                        return aInfo.processName
                    } else {
                        return ""
                    }
                }
            }

        private val currentProcessNameByReflect: String?
            get() {
                var processName = ""

                try {
                    val app = app
                    val loadedApkField =
                        app.javaClass.getField("mLoadedApk")
                    loadedApkField.isAccessible = true
                    val loadedApk = loadedApkField[app]
                    val activityThreadField =
                        loadedApk!!.javaClass.getDeclaredField("mActivityThread")
                    activityThreadField.isAccessible = true
                    val activityThread = activityThreadField[loadedApk]
                    val getProcessName =
                        activityThread!!.javaClass.getDeclaredMethod("getProcessName")
                    processName = getProcessName.invoke(activityThread) as String
                } catch (var7: Exception) {
                    var7.printStackTrace()
                }

                return processName
            }

        private val applicationByReflect: Application
            get() {
                try {
                    val activityThread =
                        Class.forName("android.app.ActivityThread")
                    val thread = activityThread.getMethod("currentActivityThread")
                        .invoke(null as Any?)
                    val app = activityThread.getMethod("getApplication").invoke(thread)
                        ?: throw NullPointerException("u should init first")

                    return app as Application
                } catch (var3: NoSuchMethodException) {
                    var3.printStackTrace()
                } catch (var4: IllegalAccessException) {
                    var4.printStackTrace()
                } catch (var5: InvocationTargetException) {
                    var5.printStackTrace()
                } catch (var6: ClassNotFoundException) {
                    var6.printStackTrace()
                }

                throw NullPointerException("u should init first")
            }
    }
}

