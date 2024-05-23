package com.letianpai.sensorservice

import android.content.ComponentName
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleService
import com.letianpai.MCULogUtils
import com.renhejia.robot.commandlib.consts.SensorConsts
import com.renhejia.robot.commandlib.log.LogUtils
import com.renhejia.robot.commandlib.utils.SystemUtil
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

class GeeUISensorsService : LifecycleService() {

    interface SensorServiceListener {
        fun registerGeeUISensorDataListener(
            className: String?,
            sensorType: String?,
            listener:GeeUISensorDataListener?
        )
        fun unRegisterGeeUISensorDataListener(className: String?, sensorType: String?)
        fun registerGeeUIWriteResListener(
            className: String?,
            listener: GeeUISensoWriteResListener?
        )
        fun unRegisterGeeUIWriteResListener(className: String?)
    }

    private abstract class LocalBinder : Binder(), SensorServiceListener


    private val job = Job()  // 创建一个 Job 对象
    private val scope = CoroutineScope(Dispatchers.IO + job)  // 创建协程作用域

    private val lightListenerMap = HashMap<Class<*>, GeeUISensorDataListener>()
    private val touchListenerMap = HashMap<Class<*>, GeeUISensorDataListener>()
    private val cliffListenerMap = HashMap<Class<*>, GeeUISensorDataListener>()
    private val suspendListenerMap = HashMap<Class<*>, GeeUISensorDataListener>()
    private val waggleListenerMap = HashMap<Class<*>, GeeUISensorDataListener>()
    private val downListenerMap = HashMap<Class<*>, GeeUISensorDataListener>()
    private val iRListenerMap = HashMap<Class<*>, GeeUISensorDataListener>()//红外灯，上桩用
    private val tofListenerMap = HashMap<Class<*>, GeeUISensorDataListener>()//红外, 测距用
    private val writeResListenerMap = HashMap<Class<*>, GeeUISensoWriteResListener>()


    //本地的
    private val mBinder = object : LocalBinder(){
        override fun registerGeeUISensorDataListener(
            className: String?,
            sensorType: String?,
            listener: GeeUISensorDataListener?
        ) {
            if ((className != null && className != "") && (sensorType != null && sensorType != "") && (listener != null)) {
                registerSensorDataListener(className, sensorType, listener)
            }
        }

        override fun unRegisterGeeUISensorDataListener(className: String?, sensorType: String?) {
            if ((className != null && className != "") && (sensorType != null && sensorType != "")) {
                unRegisterSensorDataListener(className, sensorType)
            }
        }

        override fun registerGeeUIWriteResListener(
            className: String?,
            listener: GeeUISensoWriteResListener?
        ) {
            if ((className != null && className != "") && (listener != null)) {
                registerSensorWriteResListener(className, listener)
            }
        }

        override fun unRegisterGeeUIWriteResListener(className: String?) {
            if (className != null && className != "") {
                unRegisterSensorWriteResListener(className)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return mBinder;
    }

    override fun onCreate() {
        super.onCreate()
        openPortAndRegisterListener()
    }

    private fun openPortAndRegisterListener() {
        scope.launch {
            //串口MCUservice已经打开，这里直接用即可
//                SerialAllJNI.openPort()
            SerialAllJNI.registerSensorDataListener(object : MCUSensorListener {
                override fun resListener(res: String) {
                    doSomeWorkForRes(res)
                    MCULogUtils.logi("<<<","resListener: $res" )
                }

                override fun antListener(ant: String) {
                    doSomeWorkForInt(ant)
                    MCULogUtils.logi("<<<","antListener: $ant" )
                }
            })
            //判断MCU是否正常
            SerialAllJNI.writeData("AT+Gsys\\r\\n")
            //要加延迟，不然版本号的返回收不到
            delay(1000)
            //读取固件版本号
            SerialAllJNI.writeData("AT+VerR\\r\\n")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private fun consumerData() {
        Log.d("GeeUISensorsService", "consumerData")
        scope.launch {
            val numCoroutines = 9 // 设置要并发执行的协程数量
            withContext(Dispatchers.IO) {
                while (true) {
                    Log.d("GeeUISensorsService", "true")

                    val time = measureTimeMillis {
                        val jobs = List(numCoroutines) {
                            launch(Dispatchers.Default) { // 在默认的调度器上启动协程
                                Log.d("GeeUISensorsService", "jobs-- launch")
//                                doSomeWorkForInt()
                            }
                        }
                        jobs.forEach { it.join() }

                        val jobs2 = List(numCoroutines) {
                            launch(Dispatchers.Default) { // 在默认的调度器上启动协程
                                Log.d("GeeUISensorsService", "jobs2-- launch")
//                                doSomeWorkForRes()
                            }
                        }

                        jobs2.forEach { it.join() }
                    }
                    println("Total time: $time ms")
                }
            }
        }
    }

    private fun registerSensorWriteResListener(
        className: String,
        listener: GeeUISensoWriteResListener
    ) {
        val subscriberClass = Class.forName(className)
        synchronized(this) {
            writeResListenerMap.put(subscriberClass, listener)
        }
    }

    private fun unRegisterSensorWriteResListener(className: String) {
        val subscriberClass = Class.forName(className)
        synchronized(this) {
            writeResListenerMap.remove(subscriberClass)
        }
    }

    private fun registerSensorDataListener(
        className: String, sensorType: String, listener: GeeUISensorDataListener
    ) {
        MCULogUtils.logi("GeeUISensorsService","registerSensorDataListener -- className-$className --sensorType-$sensorType")

        val subscriberClass = Class.forName(className)
        when (sensorType) {
            SensorConsts.GEEUI_SENSOR_TYPE_LIGHT -> {
                synchronized(this) {
                    lightListenerMap.put(subscriberClass, listener)
                }
            }

            SensorConsts.GEEUI_SENSOR_TYPE_TOUCH -> {
                synchronized(this) {
                    touchListenerMap.put(subscriberClass, listener)
                }
            }
            SensorConsts.GEEUI_SENSOR_TYPE_CLIFF -> {
                synchronized(this) {
                    cliffListenerMap.put(subscriberClass, listener)
                }
            }
            SensorConsts.GEEUI_SENSOR_TYPE_SUSPEND -> {
                synchronized(this) {
                    suspendListenerMap.put(subscriberClass, listener)
                }
            }
            SensorConsts.GEEUI_SENSOR_TYPE_WAGGLE -> {
                synchronized(this) {
                    waggleListenerMap.put(subscriberClass, listener)
                }
            }
            SensorConsts.GEEUI_SENSOR_TYPE_DOWN -> {
                synchronized(this) {
                    downListenerMap.put(subscriberClass, listener)
                }
            }
            //红外上报的数据
            SensorConsts.GEEUI_SENSOR_TYPE_IR -> {
                synchronized(this){
                    iRListenerMap.put(subscriberClass, listener)
                }
            }
            //红外测距
            SensorConsts.GEEUI_SENSOR_TYPE_TOF -> {
                synchronized(this){
                    tofListenerMap.put(subscriberClass, listener)
                }
            }
        }
    }

    fun unRegisterSensorDataListener(className: String, sensorType: String) {
        val subscriberClass = Class.forName(className)
        when (sensorType) {
            SensorConsts.GEEUI_SENSOR_TYPE_LIGHT -> {
                synchronized(this) {
                    lightListenerMap.remove(subscriberClass)
                }
            }

            SensorConsts.GEEUI_SENSOR_TYPE_TOUCH -> {
                synchronized(this) {
                    touchListenerMap.remove(subscriberClass)
                }
            }
            SensorConsts.GEEUI_SENSOR_TYPE_CLIFF -> {
                synchronized(this) {
                    cliffListenerMap.remove(subscriberClass)
                }
            }
            SensorConsts.GEEUI_SENSOR_TYPE_SUSPEND -> {
                synchronized(this) {
                    suspendListenerMap.remove(subscriberClass)
                }
            }
            SensorConsts.GEEUI_SENSOR_TYPE_WAGGLE -> {
                synchronized(this) {
                    waggleListenerMap.remove(subscriberClass)
                }
            }
            SensorConsts.GEEUI_SENSOR_TYPE_DOWN -> {
                synchronized(this) {
                    downListenerMap.remove(subscriberClass)
                }
            }
            //红外上报的数据
            SensorConsts.GEEUI_SENSOR_TYPE_IR -> {
                synchronized(this){
                    iRListenerMap.remove(subscriberClass)
                }
            }
            //红外测距
            SensorConsts.GEEUI_SENSOR_TYPE_TOF -> {
                synchronized(this){
                    tofListenerMap.remove(subscriberClass)
                }
            }

            SensorConsts.GEEUI_SENSOR_TYPE_WRITE_RES -> {
                synchronized(this) {
                    writeResListenerMap.remove(subscriberClass)
                }
            }
        }
    }

    private fun doSomeWorkForRes(res: String) {
        if (res.contains("AT+RES,LRAM") || res.contains("AT+RES,GeeUI")) {
            val result = res.replace("\r\n", "")
            var verStr = result.split(",")[1]
            verStr = if (verStr.startsWith("LRAM.")) {
                verStr.replace("LRAM.", "")
            } else {
                verStr.replace("GeeUI.", "")
            }
            //写入文件
            SystemUtil.set(SystemUtil.MCU_VERSION, verStr)
        }

        if (res.contains("AT+RES,bootloader")){
            //MCU工作不正常，需要重新刷机
            val intent = Intent()
            intent.putExtra("mcuBootloader", true)
            intent.component =
                ComponentName("com.letianpai.otaservice", "com.letianpai.otaservice.L81OtaActivity")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }

        for ((_, listener) in writeResListenerMap) {
//            MCULogUtils.logi("<<<","listener --$listener")
            listener.onSensorWriteRes(res)
        }
    }

    private fun doSomeWorkForInt(intCom: String) {
        val com = intCom.replace("\\r\\n", "")
        val data = com.split(",")
        if (data.size == 3 && data[0] == "AT+INT") {
            //传感器上报数据
            val type = data[1]
            when (type) {
                SensorConsts.GEEUI_SENSOR_TYPE_LIGHT -> {
                    for ((_, listener) in lightListenerMap) {
                        listener.onSensorDataChanged(data[2].trim().toInt(), type)
                    }
                }

                SensorConsts.GEEUI_SENSOR_TYPE_TOUCH -> {
                    for ((_, listener) in touchListenerMap) {
                        listener.onSensorDataChanged(data[2].trim().toInt(), type)
                    }
                }
                SensorConsts.GEEUI_SENSOR_TYPE_CLIFF -> {
                    for ((_, listener) in cliffListenerMap) {
                        listener.onSensorDataChanged(data[2].trim().toInt(), type)
                    }
                }
                SensorConsts.GEEUI_SENSOR_TYPE_SUSPEND -> {
                    for ((_, listener) in suspendListenerMap) {
                        listener.onSensorDataChanged(data[2].trim().toInt(), type)
                    }
                }
                SensorConsts.GEEUI_SENSOR_TYPE_WAGGLE -> {
                    for ((_, listener) in waggleListenerMap) {
                        listener.onSensorDataChanged(data[2].trim().toInt(), type)
                    }
                }
                SensorConsts.GEEUI_SENSOR_TYPE_DOWN -> {
                    for ((_, listener) in downListenerMap) {
                        listener.onSensorDataChanged(data[2].trim().toInt(), type)
                    }
                }
                //红外上报的数据
                SensorConsts.GEEUI_SENSOR_TYPE_IR -> {
                    for ((_, listener) in iRListenerMap) {
                        listener.onSensorDataChanged(data[2].trim().toInt(), type)
                    }
                }
                //红外，测距
                SensorConsts.GEEUI_SENSOR_TYPE_TOF -> {
                    for ((_, listener) in tofListenerMap) {
                        listener.onSensorDataChanged(data[2].trim().toInt(), type)
                    }
                }
            }
        }
//        MCULogUtils.logi("<<<","Work done by ${Thread.currentThread().name}")
    }
}