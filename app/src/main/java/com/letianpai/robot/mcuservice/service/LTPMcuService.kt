package com.letianpai.robot.mcuservice.service

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.RemoteCallbackList
import android.os.RemoteException
import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.letianpai.McuCommandControlManager
import com.letianpai.Utils
import com.letianpai.robot.letianpaiservice.LtpMcuCommandCallback
import com.renhejia.robot.commandlib.consts.MCUCommandConsts
import com.renhejia.robot.commandlib.consts.SensorConsts
import com.renhejia.robot.commandlib.log.LogUtils
import com.letianpai.sensorservice.GeeUISensoWriteResListener
import com.letianpai.sensorservice.GeeUISensorDataListener
import com.letianpai.sensorservice.GeeUISensorsService.SensorServiceListener
import com.letianpai.sensorservice.IGeeUISensoWriteResListener
import com.letianpai.sensorservice.IGeeUISensorDataListener
import com.letianpai.sensorservice.ISensorService
import com.letianpai.sensorservice.SerialAllJNI
import java.io.File
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class LTPMcuService : Service() {
    private var isEnterFactory: Boolean = false
    private var isCloseMcuSerial: Boolean = false
    private var isCloseRobotPolicy: Boolean = false

    //MCU当前执行命令的时间
    @Volatile
    private var preExeComTime: Long = 0

    //本地来的服务
    private var localSensorService: SensorServiceListener? = null
    private var iLetianpaiService: ILetianpaiService? = null
    private val ltpSensorResCallback: RemoteCallbackList<IGeeUISensoWriteResListener> =
        RemoteCallbackList<IGeeUISensoWriteResListener>()

    //红外数据
    private val ltpSensorIRCallback: RemoteCallbackList<IGeeUISensorDataListener> =
        RemoteCallbackList<IGeeUISensorDataListener>()
    private val mIRLock: Lock = ReentrantLock()
    private val mResLock: Lock = ReentrantLock()
    private var suspendValue: Int = 0
    private var downValue: Int = 0

    private var isOpenSerial: Boolean = false

    // private long lightReportTime = 0;
    private var serverHandler: Handler? = null
    private var handlerThread: HandlerThread? = null

    private var iSensorService: ISensorService.Stub? = object : ISensorService.Stub() {
        @Throws(RemoteException::class)
        override fun registerGeeUIWriteResListener(listener: IGeeUISensoWriteResListener?) {
            ltpSensorResCallback.register(listener)
        }

        @Throws(RemoteException::class)
        override fun unRegisterGeeUIWriteResListener(listener: IGeeUISensoWriteResListener?) {
            ltpSensorResCallback.unregister(listener)
        }

        @Throws(RemoteException::class)
        override fun writeAtCommand(command: String) {
            writeAtCom(command)
        }

        override fun registerGeeUISensorIRDataListener(listener: IGeeUISensorDataListener?) {
            ltpSensorIRCallback.register(listener)
        }

        override fun unRegisterGeeUISensorIRDataListener(listener: IGeeUISensorDataListener?) {
            ltpSensorIRCallback.unregister(listener)
        }
    }

    //处理红外上报的数据
    private fun responseIRData(sensorData: Int, sensorType: String) {
        try {
            mIRLock.lock()
            val N: Int = ltpSensorIRCallback.beginBroadcast()
            Log.d(
                "<<<<",
                "responseIRData: sensorData--" + sensorData + "----sensorType-" + sensorType + "--N--" + N
            )
            for (i in 0 until N) {
                ltpSensorIRCallback.getBroadcastItem(i).onSensorDataChanged(sensorData, sensorType)
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        } finally {
            ltpSensorIRCallback.finishBroadcast()
            mIRLock.unlock()
        }
    }

    private fun responseResCommand(res: String) {
        Log.d("<<<<", "responseLongConnectCommand: command--\$res-----")
        if (TextUtils.isEmpty(res)) {
            return
        }
        mResLock.lock()
        try {
            val N: Int = ltpSensorResCallback.beginBroadcast()
            Log.d("<<<<", "responseLongConnectCommand: for--" + res + "----N-" + N + "")
            for (i in 0 until N) {
                ltpSensorResCallback.getBroadcastItem(i).onSensorWriteRes(res)
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        } finally {
            ltpSensorResCallback.finishBroadcast()
            mResLock.unlock()
        }
    }

    private fun writeAtCom(command: String) {
        SerialAllJNI.writeData(command)
    }

    private fun closeSerialPort() {
        val result: Boolean = SerialAllJNI.closePort()
        if (result) {
            LogUtils.logi(TAG, "close serialPort success ")
        } else {
            LogUtils.logi(TAG, "close serialPort failed ")
        }
    }

    /**
     * 打开串口
     */
    private fun openSerialPort() {
        val isOpen: Int = SerialAllJNI.openPort()
        if (isOpen == 1) {
            isOpenSerial = true
            LogUtils.logi(TAG, "sensor service openSerialPort success ")
        } else {
            isOpenSerial = false
            LogUtils.loge(TAG, " ---- sensor service openSerialPort Faild----- ")
        }
    }

    override fun onCreate() {
        super.onCreate()
        openSerialPort()
        connectLetianpaiService()
        connectSensorService()

        handlerThread = HandlerThread("ServerHandlerThread")
        handlerThread!!.start()
        serverHandler = Handler(handlerThread!!.getLooper())

        // new Thread(new Runnable() {
        //     @Override
        //     public void run() {
        //         deleteFolder(new File("sdcard/aispeech/"));
        //     }
        // }).start();
    }

    override fun onBind(intent: Intent): IBinder? {
        return iSensorService
    }

    override fun onDestroy() {
        super.onDestroy()
        if (iLetianpaiService != null) {
            try {
                iLetianpaiService.unregisterMcuCmdCallback(ltpMcuCommandCallback)
                unbindService(letianpaiServiceConnection)
                iLetianpaiService = null
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
        if (iSensorService != null) {
            unbindService(sensorServiceConnection)
            iSensorService = null
        }
    }

    private val letianpaiServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            LogUtils.logi(TAG, "乐天派 MCU 完成AIDLService服务")
            iLetianpaiService = ILetianpaiService.Stub.asInterface(service)
            try {
                iLetianpaiService.registerMcuCmdCallback(ltpMcuCommandCallback)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            LogUtils.logi(TAG, "乐天派 MCU 无法绑定aidlserver的AIDLService服务")
        }
    }

    private val sensorServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            localSensorService = service as SensorServiceListener?
            LogUtils.logi(TAG, "本地绑定sensor服务成功")
            observeCliffEvent()
            observeSuspendEvent()
            observeTouchEvent()
            observeLightEvent()
            observeDownEvent()
            observeWriteResEvent()
            observeIREvent()
            observeTofEvent()
            observeWaggleEvent()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            LogUtils.logd(TAG, "---sensor service bind faild in mcuservice")
            iSensorService = null
        }

        private fun observeDownEvent() {
            localSensorService?.registerGeeUISensorDataListener(
                LTPMcuService::class.java.getName(),
                SensorConsts.GEEUI_SENSOR_TYPE_DOWN,
                object : GeeUISensorDataListener {
                    override fun onSensorDataChanged(sensorData: Int, sensorType: String) {
                        LogUtils.logd(TAG, "---down-" + sensorType + "--down--" + sensorData)
                        downValue = sensorData
                        try {
                            if (downValue != 0) {
                                iLetianpaiService.setSensorResponse(
                                    "controlStartFallDown",
                                    "fall_down"
                                )
                            } else {
                                iLetianpaiService.setSensorResponse(
                                    "controlStopFallDown",
                                    "fall_down"
                                )
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                })
        }

        private fun observeWriteResEvent() {
            try {
                localSensorService?.registerGeeUIWriteResListener(
                    LTPMcuService::class.java.getName(),
                    object : GeeUISensoWriteResListener {
                        override fun onSensorWriteRes(res: String) {
                            responseResCommand(res)
                        }
                    })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun observeLightEvent() {
            try {
                localSensorService?.registerGeeUISensorDataListener(
                    LTPMcuService::class.java.getName(),
                    SensorConsts.GEEUI_SENSOR_TYPE_LIGHT,
                    object : GeeUISensorDataListener {
                        override fun onSensorDataChanged(sensorData: Int, sensorType: String) {
                            if (!isCloseRobotPolicy) {
                                // lightReportTime = System.currentTimeMillis();
                                // LogUtils.logi(TAG, "---lightType-" + sensorType + "--lightData--" + sensorData + "--lightReportTime--"+lightReportTime);
                            }
                        }
                    })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun observeTouchEvent() {
            localSensorService?.registerGeeUISensorDataListener(
                LTPMcuService::class.java.getName(),
                SensorConsts.GEEUI_SENSOR_TYPE_TOUCH,
                object : GeeUISensorDataListener {
                    override fun onSensorDataChanged(sensorData: Int, sensorType: String) {
                        LogUtils.logi(
                            TAG,
                            "---touchType-" + sensorType + "--touchData--" + sensorData
                        )
                        try {
                            if (sensorData == 1) {
                                iLetianpaiService.setSensorResponse("controlTap", "controlTap")
                            } else if (sensorData == 2) {
                                iLetianpaiService.setSensorResponse(
                                    "controlDoubleTap",
                                    "controlDoubleTap"
                                )
                            } else if (sensorData == 3) {
                                iLetianpaiService.setSensorResponse(
                                    "controlLongPressTap",
                                    "controlLongPressTap"
                                )
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                })
        }

        private fun observeSuspendEvent() {
            localSensorService?.registerGeeUISensorDataListener(
                LTPMcuService::class.java.getName(),
                SensorConsts.GEEUI_SENSOR_TYPE_SUSPEND,
                object : GeeUISensorDataListener {
                    override fun onSensorDataChanged(sensorData: Int, sensorType: String) {
                        suspendValue = sensorData
                        LogUtils.logi(
                            TAG,
                            "---suspendType-" + sensorType + "--suspendData--" + suspendValue
                        )
                        try {
                            if (!isCloseRobotPolicy) {
                                if (suspendValue == 1) {
                                    //延迟再发指令
                                    Thread.sleep(400)
                                    //如果没有倒下，就发悬空
                                    if (downValue == 0) {
                                        iLetianpaiService.setSensorResponse(
                                            "controlStartPrecipice",
                                            "dangling"
                                        )
                                    }
                                } else if (suspendValue == 0) {
                                    iLetianpaiService.setSensorResponse(
                                        "controlStopPrecipice",
                                        "dangling"
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                })
        }

        private fun observeCliffEvent() {
            localSensorService?.registerGeeUISensorDataListener(
                LTPMcuService::class.java.getName(),
                SensorConsts.GEEUI_SENSOR_TYPE_CLIFF,
                object : GeeUISensorDataListener {
                    override fun onSensorDataChanged(sensorData: Int, sensorType: String) {
                        LogUtils.logi(
                            TAG,
                            "---cliffType-" + sensorType + "--cliffData--" + sensorData
                        )
                        //没有悬空，没有倒下，触发悬崖的时候在发送
                        if (suspendValue != 1 && downValue == 0 && sensorData != 0 && !isCloseRobotPolicy) {
                            //延迟1秒再发指令
                            try {
                                //延迟再发指令
                                Thread.sleep(400)
                                when (sensorData) {
                                    CLIFF_LEFT_FRONT, CLIFF_RIGHT_FRONT, CLIFF_FRONT_RIGHT_LEFT -> {
                                        //往后走
                                        iLetianpaiService.setSensorResponse("fallBackend", "往后走")
                                        LogUtils.logd(TAG, "cliffType 需要往后走")
                                    }

                                    CLIFF_LEFT_BACK, CLIFF_RIGHT_BACK, CLIFF_BACK_RIGHT_LEFT -> {
                                        //往前走
                                        iLetianpaiService.setSensorResponse("fallForward", "往前走")
                                        LogUtils.logd(TAG, "cliffType 需要往前走")
                                    }

                                    CLIFF_LEFT_FRONT_BACK -> {
                                        //往右走
                                        iLetianpaiService.setSensorResponse("fallRight", "往右走")
                                        LogUtils.logd(TAG, "cliffType 需要往右走")
                                    }

                                    CLIFF_RIGHT_FRONT_BACK -> {
                                        //往左走
                                        iLetianpaiService.setSensorResponse("fallLeft", "往左走")
                                        LogUtils.logd(TAG, " cliffType 需要往左走")
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                })
        }

        private fun observeIREvent() {
            localSensorService?.registerGeeUISensorDataListener(
                LTPMcuService::class.java.getName(),
                SensorConsts.GEEUI_SENSOR_TYPE_IR,
                object : GeeUISensorDataListener {
                    override fun onSensorDataChanged(sensorData: Int, sensorType: String) {
                        responseIRData(sensorData, sensorType)
                    }
                })
        }

        private fun observeTofEvent() {
            localSensorService?.registerGeeUISensorDataListener(
                LTPMcuService::class.java.getName(),
                SensorConsts.GEEUI_SENSOR_TYPE_TOF,
                object : GeeUISensorDataListener {
                    override fun onSensorDataChanged(sensorData: Int, sensorType: String) {
                        try {
                            val thermal: Float = Utils.cpuThermal
                            val minValue: Int
                            val maxValue: Int
                            //随着温度的升高，红外误差会加大，所以加了下面的判断
                            if (thermal < 85.0) {
                                minValue = 30
                                maxValue = 70
                            } else {
                                minValue = 60
                                maxValue = 100
                            }
                            if (sensorData > minValue && sensorData < maxValue) {
                                iLetianpaiService.setSensorResponse("tof", "避障")
                            }
                            LogUtils.logd(
                                TAG,
                                "---sensorType-" + sensorType + "--sensorData：" + sensorData + "--thermal::" + thermal
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                })
        }

        private fun observeWaggleEvent() {
            localSensorService?.registerGeeUISensorDataListener(
                LTPMcuService::class.java.getName(),
                SensorConsts.GEEUI_SENSOR_TYPE_WAGGLE,
                object : GeeUISensorDataListener {
                    override fun onSensorDataChanged(sensorData: Int, sensorType: String) {
                        try {
                            iLetianpaiService.setSensorResponse("waggle", "摇晃")
                            LogUtils.logd(
                                TAG,
                                "---waggle-" + sensorType + "--waggle--" + sensorData
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                })
        }
    }


    //链接letianpai 服务端
    private fun connectLetianpaiService() {
        val intent = Intent()
        intent.setPackage("com.renhejia.robot.letianpaiservice")
        intent.setAction("android.intent.action.LETIANPAI")
        bindService(intent, letianpaiServiceConnection, BIND_AUTO_CREATE)
    }

    //绑定本地sensor 服务
    private fun connectSensorService() {
        val intent: Intent = Intent()
        intent.setPackage("com.letianpai.robot.mcuservice")
        intent.setAction("android.intent.action.geeui.SENSOR")
        bindService(intent, sensorServiceConnection, BIND_AUTO_CREATE)
    }

    private val ltpMcuCommandCallback: LtpMcuCommandCallback.Stub = object : Stub() {
        @Throws(RemoteException::class)
        override fun onMcuCommandCommand(command: String, data: String) {
            if (TextUtils.isEmpty(command)) {
                return
            }
            if (command == MCUCommandConsts.COMMAND_TYPE_OPEN_MCU) {
                isCloseMcuSerial = false
                LogUtils.logd(TAG, "onCommandReceived: 打开串口")
                openSerialPort()
            } else if (command == MCUCommandConsts.COMMAND_TYPE_CLOSE_MCU) {
                isCloseMcuSerial = true
                LogUtils.logd(TAG, "onCommandReceived: 关闭串口")
                closeSerialPort()
            }
            if (command == MCUCommandConsts.COMMAND_TYPE_ENTER_FACTORY) {
                isEnterFactory = true
                LogUtils.logd(TAG, "进入工厂模式")
                closeSerialPort()
            } else if (command == MCUCommandConsts.COMMAND_TYPE_EXIT_FACTORY) {
                isEnterFactory = false
                LogUtils.logd(TAG, "退出工厂模式")
                openSerialPort()
            }
            //切换到机器人模式
            if (command == "powerControl") {
                val gson: Gson = Gson()
                val powerMotion: PowerMotion = gson.fromJson<T>(data, PowerMotion::class.java)
                if (powerMotion.getFunction() === 5) {
                    isCloseRobotPolicy = powerMotion.getStatus() !== 1
                    serverHandler!!.post(object : Runnable {
                        override fun run() {
                            try {
                                Thread.sleep(40)
                                //不是机器人模式
                                if (!isCloseRobotPolicy) {
                                    //打开避障
                                    writeAtCom("AT+TofSet,1,800\\r\\n")
                                } else {
                                    //关闭避障
                                    writeAtCom("AT+TofSet,0,800\\r\\n")
                                }
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }
                        }
                    })

                    // new Handler(getMainLooper()).postDelayed(new Runnable() {
                    //     @Override
                    //     public void run() {
                    //         if (!isCloseRobotPolicy) {
                    //             serverHandler.post(new Runnable() {
                    //                 @Override
                    //                 public void run() {
                    //                 }
                    //             });
                    //         }
                    //     }
                    // }, 5);
                }
            }
            //工厂模式也会打开串口
            if (!isEnterFactory && !isCloseMcuSerial) {
                serverHandler!!.post(Runnable {
                    try {
                        Thread.sleep(40)
                        McuCommandControlManager.getInstance(this@LTPMcuService)
                            .commandDistribute(command, data)
                        LogUtils.logd(TAG, "mcu 延迟执行--command1::" + command + "------" + data)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                })
            }
        }
    }

    companion object {
        private const val TAG: String = "LTPMcuService"

        //左前
        private const val CLIFF_LEFT_FRONT: Int = 1

        //左后
        private const val CLIFF_LEFT_BACK: Int = 2

        //右后
        private const val CLIFF_RIGHT_BACK: Int = 3

        //右前
        private const val CLIFF_RIGHT_FRONT: Int = 4

        //前面左右
        private const val CLIFF_FRONT_RIGHT_LEFT: Int = 8

        //后面左右
        private const val CLIFF_BACK_RIGHT_LEFT: Int = 6

        //左边前后
        private const val CLIFF_LEFT_FRONT_BACK: Int = 5

        //右边前后
        private const val CLIFF_RIGHT_FRONT_BACK: Int = 7
        fun deleteFolder(folder: File) {
            try {
                if (folder.isDirectory()) {
                    val files: Array<File>? = folder.listFiles()
                    if (files != null) {
                        for (file: File in files) {
                            deleteFolder(file)
                        }
                    }
                }
                folder.delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
