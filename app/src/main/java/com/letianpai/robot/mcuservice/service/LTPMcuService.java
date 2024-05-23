package com.letianpai.robot.mcuservice.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.*;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.letianpai.McuCommandControlManager;
import com.letianpai.Utils;
import com.letianpai.robot.letianpaiservice.LtpMcuCommandCallback;
import com.letianpai.sensorservice.*;
import com.renhejia.robot.commandlib.consts.MCUCommandConsts;
import com.renhejia.robot.commandlib.consts.SensorConsts;
import com.renhejia.robot.commandlib.log.LogUtils;
import com.renhejia.robot.commandlib.parser.power.PowerMotion;
import com.renhejia.robot.letianpaiservice.ILetianpaiService;

import java.io.File;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LTPMcuService extends Service {
    private static final String TAG = "LTPMcuService";
    //左前
    private static final int CLIFF_LEFT_FRONT = 1;
    //左后
    private static final int CLIFF_LEFT_BACK = 2;
    //右后
    private static final int CLIFF_RIGHT_BACK = 3;
    //右前
    private static final int CLIFF_RIGHT_FRONT = 4;

    //前面左右
    private static final int CLIFF_FRONT_RIGHT_LEFT = 8;
    //后面左右
    private static final int CLIFF_BACK_RIGHT_LEFT = 6;

    //左边前后
    private static final int CLIFF_LEFT_FRONT_BACK = 5;
    //右边前后
    private static final int CLIFF_RIGHT_FRONT_BACK = 7;
    private boolean isEnterFactory = false;
    private boolean isCloseMcuSerial = false;
    private boolean isCloseRobotPolicy = false;

    //MCU当前执行命令的时间
    private volatile long preExeComTime = 0;
    //本地来的服务
    private GeeUISensorsService.SensorServiceListener localSensorService;
    private ILetianpaiService iLetianpaiService;
    private final RemoteCallbackList<IGeeUISensoWriteResListener> ltpSensorResCallback = new RemoteCallbackList<>();
    //红外数据
    private final RemoteCallbackList<IGeeUISensorDataListener> ltpSensorIRCallback = new RemoteCallbackList<>();
    private final Lock mIRLock = new ReentrantLock();
    private final Lock mResLock = new ReentrantLock();
    private int suspendValue = 0;
    private int downValue = 0;

    private boolean isOpenSerial = false;

    // private long lightReportTime = 0;

    private Handler serverHandler;
    private HandlerThread handlerThread;

    private ISensorService.Stub iSensorService = new ISensorService.Stub() {

        @Override
        public void registerGeeUIWriteResListener(IGeeUISensoWriteResListener listener) throws RemoteException {
            ltpSensorResCallback.register(listener);
        }

        @Override
        public void unRegisterGeeUIWriteResListener(IGeeUISensoWriteResListener listener) throws RemoteException {
            ltpSensorResCallback.unregister(listener);
        }

        @Override
        public void writeAtCommand(String command) throws RemoteException {
            writeAtCom(command);
        }

        @Override
        public void registerGeeUISensorIRDataListener(IGeeUISensorDataListener listener) {
            ltpSensorIRCallback.register(listener);
        }

        @Override
        public void unRegisterGeeUISensorIRDataListener(IGeeUISensorDataListener listener) {
            ltpSensorIRCallback.unregister(listener);
        }
    };

    //处理红外上报的数据
    private void responseIRData(int sensorData, @NonNull String sensorType) {
        try {
            mIRLock.lock();
            int N = ltpSensorIRCallback.beginBroadcast();
            Log.d("<<<<", "responseIRData: sensorData--" + sensorData + "----sensorType-" + sensorType + "--N--" + N);
            for (int i = 0; i < N; i++) {
                ltpSensorIRCallback.getBroadcastItem(i).onSensorDataChanged(sensorData, sensorType);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            ltpSensorIRCallback.finishBroadcast();
            mIRLock.unlock();
        }
    }

    private void responseResCommand(String res) {
        Log.d("<<<<", "responseLongConnectCommand: command--$res-----");
        if (TextUtils.isEmpty(res)) {
            return;
        }
        mResLock.lock();
        try {
            int N = ltpSensorResCallback.beginBroadcast();
            Log.d("<<<<", "responseLongConnectCommand: for--" + res + "----N-" + N + "");
            for (int i = 0; i < N; i++) {
                ltpSensorResCallback.getBroadcastItem(i).onSensorWriteRes(res);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            ltpSensorResCallback.finishBroadcast();
            mResLock.unlock();
        }
    }

    private void writeAtCom(String command) {
        SerialAllJNI.INSTANCE.writeData(command);
    }

    private void closeSerialPort() {
        boolean result = SerialAllJNI.INSTANCE.closePort();
        if (result) {
            LogUtils.logi(TAG, "close serialPort success ");
        } else {
            LogUtils.logi(TAG, "close serialPort failed ");
        }
    }

    /**
     * 打开串口
     */
    private void openSerialPort() {
        int isOpen = SerialAllJNI.INSTANCE.openPort();
        if (isOpen == 1) {
            isOpenSerial = true;
            LogUtils.logi(TAG, "sensor service openSerialPort success ");
        } else {
            isOpenSerial = false;
            LogUtils.loge(TAG, " ---- sensor service openSerialPort Faild----- ");
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        openSerialPort();
        connectLetianpaiService();
        connectSensorService();

        handlerThread = new HandlerThread("ServerHandlerThread");
        handlerThread.start();
        serverHandler = new Handler(handlerThread.getLooper());

        // new Thread(new Runnable() {
        //     @Override
        //     public void run() {
        //         deleteFolder(new File("sdcard/aispeech/"));
        //     }
        // }).start();
    }

    public static void deleteFolder(File folder) {
        try {
            if (folder.isDirectory()) {
                File[] files = folder.listFiles();
                if (files != null) {
                    for (File file : files) {
                        deleteFolder(file);
                    }
                }
            }
            folder.delete();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iSensorService;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (iLetianpaiService != null) {
            try {
                iLetianpaiService.unregisterMcuCmdCallback(ltpMcuCommandCallback);
                unbindService(letianpaiServiceConnection);
                iLetianpaiService = null;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if (iSensorService != null) {
            unbindService(sensorServiceConnection);
            iSensorService = null;
        }
    }

    private final ServiceConnection letianpaiServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtils.logi(TAG, "乐天派 MCU 完成AIDLService服务");
            iLetianpaiService = ILetianpaiService.Stub.asInterface(service);
            try {
                iLetianpaiService.registerMcuCmdCallback(ltpMcuCommandCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.logi(TAG, "乐天派 MCU 无法绑定aidlserver的AIDLService服务");
        }
    };

    private final ServiceConnection sensorServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            localSensorService = (GeeUISensorsService.SensorServiceListener) service;
            LogUtils.logi(TAG, "本地绑定sensor服务成功");
            observeCliffEvent();
            observeSuspendEvent();
            observeTouchEvent();
            observeLightEvent();
            observeDownEvent();
            observeWriteResEvent();
            observeIREvent();
            observeTofEvent();
            observeWaggleEvent();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.logd(TAG, "---sensor service bind faild in mcuservice");
            iSensorService = null;
        }

        private void observeDownEvent() {
            localSensorService.registerGeeUISensorDataListener(LTPMcuService.class.getName(), SensorConsts.GEEUI_SENSOR_TYPE_DOWN, new GeeUISensorDataListener() {
                @Override
                public void onSensorDataChanged(int sensorData, @NonNull String sensorType) {
                    LogUtils.logd(TAG, "---down-" + sensorType + "--down--" + sensorData);
                    downValue = sensorData;
                    try {
                        if (downValue != 0) {
                            iLetianpaiService.setSensorResponse("controlStartFallDown", "fall_down");
                        } else {
                            iLetianpaiService.setSensorResponse("controlStopFallDown", "fall_down");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        private void observeWriteResEvent() {
            try {
                localSensorService.registerGeeUIWriteResListener(LTPMcuService.class.getName(), new GeeUISensoWriteResListener() {
                    @Override
                    public void onSensorWriteRes(@NonNull String res) {
                        responseResCommand(res);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void observeLightEvent() {
            try {
                localSensorService.registerGeeUISensorDataListener(LTPMcuService.class.getName(), SensorConsts.GEEUI_SENSOR_TYPE_LIGHT, new GeeUISensorDataListener() {
                    @Override
                    public void onSensorDataChanged(int sensorData, @NonNull String sensorType) {
                        if (!isCloseRobotPolicy) {
                            // lightReportTime = System.currentTimeMillis();
                            // LogUtils.logi(TAG, "---lightType-" + sensorType + "--lightData--" + sensorData + "--lightReportTime--"+lightReportTime);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void observeTouchEvent() {
            localSensorService.registerGeeUISensorDataListener(LTPMcuService.class.getName(), SensorConsts.GEEUI_SENSOR_TYPE_TOUCH, new GeeUISensorDataListener() {
                @Override
                public void onSensorDataChanged(int sensorData, @NonNull String sensorType) {
                    LogUtils.logi(TAG, "---touchType-" + sensorType + "--touchData--" + sensorData);
                    try {
                        if (sensorData == 1) {
                            iLetianpaiService.setSensorResponse("controlTap", "controlTap");
                        } else if (sensorData == 2) {
                            iLetianpaiService.setSensorResponse("controlDoubleTap", "controlDoubleTap");
                        } else if (sensorData == 3) {
                            iLetianpaiService.setSensorResponse("controlLongPressTap", "controlLongPressTap");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        private void observeSuspendEvent() {
            localSensorService.registerGeeUISensorDataListener(LTPMcuService.class.getName(), SensorConsts.GEEUI_SENSOR_TYPE_SUSPEND, new GeeUISensorDataListener() {
                @Override
                public void onSensorDataChanged(int sensorData, @NonNull String sensorType) {
                    suspendValue = sensorData;
                    LogUtils.logi(TAG, "---suspendType-" + sensorType + "--suspendData--" + suspendValue);
                    try {
                        if (!isCloseRobotPolicy) {
                            if (suspendValue == 1) {
                                //延迟再发指令
                                Thread.sleep(400);
                                //如果没有倒下，就发悬空
                                if (downValue == 0) {
                                    iLetianpaiService.setSensorResponse("controlStartPrecipice", "dangling");
                                }
                            } else if (suspendValue == 0) {
                                iLetianpaiService.setSensorResponse("controlStopPrecipice", "dangling");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        private void observeCliffEvent() {
            localSensorService.registerGeeUISensorDataListener(LTPMcuService.class.getName(), SensorConsts.GEEUI_SENSOR_TYPE_CLIFF, new GeeUISensorDataListener() {
                @Override
                public void onSensorDataChanged(int sensorData, @NonNull String sensorType) {
                    LogUtils.logi(TAG, "---cliffType-" + sensorType + "--cliffData--" + sensorData);
                    //没有悬空，没有倒下，触发悬崖的时候在发送
                    if (suspendValue != 1 && downValue == 0 && sensorData != 0 && !isCloseRobotPolicy) {
                        //延迟1秒再发指令
                        try {
                            //延迟再发指令
                            Thread.sleep(400);
                            switch (sensorData) {
                                case CLIFF_LEFT_FRONT:
                                case CLIFF_RIGHT_FRONT:
                                case CLIFF_FRONT_RIGHT_LEFT: {
                                    //往后走
                                    iLetianpaiService.setSensorResponse("fallBackend", "往后走");
                                    LogUtils.logd(TAG, "cliffType 需要往后走");
                                    break;
                                }
                                case CLIFF_LEFT_BACK:
                                case CLIFF_RIGHT_BACK:
                                case CLIFF_BACK_RIGHT_LEFT: {
                                    //往前走
                                    iLetianpaiService.setSensorResponse("fallForward", "往前走");
                                    LogUtils.logd(TAG, "cliffType 需要往前走");
                                    break;
                                }
                                case CLIFF_LEFT_FRONT_BACK: {
                                    //往右走
                                    iLetianpaiService.setSensorResponse("fallRight", "往右走");
                                    LogUtils.logd(TAG, "cliffType 需要往右走");
                                    break;
                                }
                                case CLIFF_RIGHT_FRONT_BACK: {
                                    //往左走
                                    iLetianpaiService.setSensorResponse("fallLeft", "往左走");
                                    LogUtils.logd(TAG, " cliffType 需要往左走");
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        private void observeIREvent() {
            localSensorService.registerGeeUISensorDataListener(LTPMcuService.class.getName(), SensorConsts.GEEUI_SENSOR_TYPE_IR, new GeeUISensorDataListener() {
                @Override
                public void onSensorDataChanged(int sensorData, @NonNull String sensorType) {
                    responseIRData(sensorData, sensorType);
                }
            });
        }

        private void observeTofEvent() {
            localSensorService.registerGeeUISensorDataListener(LTPMcuService.class.getName(), SensorConsts.GEEUI_SENSOR_TYPE_TOF, new GeeUISensorDataListener() {
                @Override
                public void onSensorDataChanged(int sensorData, @NonNull String sensorType) {
                    try {
                        float thermal = Utils.getCpuThermal();
                        int minValue;
                        int maxValue;
                        //随着温度的升高，红外误差会加大，所以加了下面的判断
                        if (thermal < 85.0) {
                            minValue = 30;
                            maxValue = 70;
                        } else {
                            minValue = 60;
                            maxValue = 100;
                        }
                        if (sensorData > minValue && sensorData < maxValue) {
                            iLetianpaiService.setSensorResponse("tof", "避障");
                        }
                        LogUtils.logd(TAG, "---sensorType-" + sensorType + "--sensorData：" + sensorData + "--thermal::" + thermal);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        private void observeWaggleEvent() {
            localSensorService.registerGeeUISensorDataListener(LTPMcuService.class.getName(), SensorConsts.GEEUI_SENSOR_TYPE_WAGGLE, new GeeUISensorDataListener() {
                @Override
                public void onSensorDataChanged(int sensorData, @NonNull String sensorType) {
                    try {
                        iLetianpaiService.setSensorResponse("waggle", "摇晃");
                        LogUtils.logd(TAG, "---waggle-" + sensorType + "--waggle--" + sensorData);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };


    //链接letianpai 服务端
    private void connectLetianpaiService() {
        Intent intent = new Intent();
        intent.setPackage("com.renhejia.robot.letianpaiservice");
        intent.setAction("android.intent.action.LETIANPAI");
        bindService(intent, letianpaiServiceConnection, Context.BIND_AUTO_CREATE);
    }

    //绑定本地sensor 服务
    private void connectSensorService() {
        Intent intent = new Intent();
        intent.setPackage("com.letianpai.robot.mcuservice");
        intent.setAction("android.intent.action.geeui.SENSOR");
        bindService(intent, sensorServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private final LtpMcuCommandCallback.Stub ltpMcuCommandCallback = new LtpMcuCommandCallback.Stub() {
        @Override
        public void onMcuCommandCommand(String command, String data) throws RemoteException {
            if (TextUtils.isEmpty(command)) {
                return;
            }
            if (command.equals(MCUCommandConsts.COMMAND_TYPE_OPEN_MCU)) {
                isCloseMcuSerial = false;
                LogUtils.logd(TAG, "onCommandReceived: 打开串口");
                openSerialPort();
            } else if (command.equals(MCUCommandConsts.COMMAND_TYPE_CLOSE_MCU)) {
                isCloseMcuSerial = true;
                LogUtils.logd(TAG, "onCommandReceived: 关闭串口");
                closeSerialPort();
            }
            if (command.equals(MCUCommandConsts.COMMAND_TYPE_ENTER_FACTORY)) {
                isEnterFactory = true;
                LogUtils.logd(TAG, "进入工厂模式");
                closeSerialPort();
            } else if (command.equals(MCUCommandConsts.COMMAND_TYPE_EXIT_FACTORY)) {
                isEnterFactory = false;
                LogUtils.logd(TAG, "退出工厂模式");
                openSerialPort();
            }
            //切换到机器人模式
            if (command.equals("powerControl")) {
                Gson gson = new Gson();
                PowerMotion powerMotion = gson.fromJson(data, PowerMotion.class);
                if (powerMotion.getFunction() == 5) {
                    isCloseRobotPolicy = powerMotion.getStatus() != 1;
                    serverHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                Thread.sleep(40);
                                //不是机器人模式
                                if (!isCloseRobotPolicy){
                                    //打开避障
                                    writeAtCom("AT+TofSet,1,800\\r\\n");
                                }else{
                                    //关闭避障
                                    writeAtCom("AT+TofSet,0,800\\r\\n");
                                }
                            }catch (InterruptedException e){
                                e.printStackTrace();
                            }
                        }
                    });

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
                serverHandler.post(() -> {
                    try {
                        Thread.sleep(40);
                        McuCommandControlManager.getInstance(LTPMcuService.this).commandDistribute(command, data);
                        LogUtils.logd(TAG, "mcu 延迟执行--command1::" + command + "------" + data);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    };
}
