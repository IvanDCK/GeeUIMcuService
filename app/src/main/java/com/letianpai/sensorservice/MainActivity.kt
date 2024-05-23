package com.letianpai.sensorservice

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.letianpai.robot.mcuservice.R
import com.letianpai.robot.mcuservice.service.LTPMcuService


class MainActivity : AppCompatActivity() {

    private var iSensorService: ISensorService? = null

    private val serviceConnection: ServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d("MainActivity", "传感器服务绑定成功")
            iSensorService = ISensorService.Stub.asInterface(service)
//            try {
//                iSensorService!!.registerGeeUISensorDataListener(
//                    MainActivity::class.java.name,
//                    GeeUISensorsService.GEEUI_SENSOR_CLIFF,
//                    object : IGeeUISensorDataListener.Stub() {
//                        override fun onSensorDataChanged(sensorData: Int, sensorType: String?) {
//                            Log.d("cliff", "sensorType-$sensorType -- sensorData-$sensorData")
//                        }
//
//                        override fun onSensorWriteRes(res: String?) {
//                            Log.d("cliff", "res-$res")
//                        }
//                    })
//
//                iSensorService!!.registerGeeUISensorDataListener(
//                    MainActivity::class.java.name,
//                    GeeUISensorsService.GEEUI_SENSOR_SUSPEND,
//                    object : IGeeUISensorDataListener.Stub() {
//                        override fun onSensorDataChanged(sensorData: Int, sensorType: String?) {
//                            Log.d("suspend", "sensorType-$sensorType -- sensorData-$sensorData")
//                        }
//
//                        override fun onSensorWriteRes(res: String?) {
//                            Log.d("suspend", "res-$res")
//                        }
//                    })
//
//                iSensorService!!.registerGeeUISensorDataListener(
//                    MainActivity::class.java.name,
//                    GeeUISensorsService.GEEUI_SENSOR_TOUCH,
//                    object : IGeeUISensorDataListener.Stub() {
//                        override fun onSensorDataChanged(sensorData: Int, sensorType: String?) {
//                            Log.d("touch", "sensorType-$sensorType -- sensorData-$sensorData")
//                        }
//
//                        override fun onSensorWriteRes(res: String?) {
//                            Log.d("touch", "res-$res")
//                        }
//                    })
//
//                iSensorService!!.registerGeeUISensorDataListener(
//                    MainActivity::class.java.name,
//                    GeeUISensorsService.GEEUI_SENSOR_LIGHT,
//                    object : IGeeUISensorDataListener.Stub() {
//                        override fun onSensorDataChanged(sensorData: Int, sensorType: String?) {
//                            Log.d("light", "sensorType-$sensorType -- sensorData-$sensorData")
//                        }
//
//                        override fun onSensorWriteRes(res: String?) {
//                            Log.d("light", "res-$res")
//                        }
//                    })
//
//                iSensorService!!.registerGeeUISensorDataListener(
//                    MainActivity::class.java.name,
//                    GeeUISensorsService.GEEUI_SENSOR_WAGGLE,
//                    object : IGeeUISensorDataListener.Stub() {
//                        override fun onSensorDataChanged(sensorData: Int, sensorType: String?) {
//                            Log.d("waggle", "sensorType-$sensorType -- sensorData-$sensorData")
//                        }
//
//                        override fun onSensorWriteRes(res: String?) {
//                            Log.d("waggle", "res-$res")
//                        }
//                    })
//
//                iSensorService!!.registerGeeUISensorDataListener(
//                    MainActivity::class.java.name,
//                    GeeUISensorsService.GEEUI_SENSOR_DOWN,
//                    object : IGeeUISensorDataListener.Stub() {
//                        override fun onSensorDataChanged(sensorData: Int, sensorType: String?) {
//                            Log.d("down", "sensorType-$sensorType -- sensorData-$sensorData")
//                        }
//
//                        override fun onSensorWriteRes(res: String?) {
//                            Log.d("down", "res-$res")
//                        }
//                    })
//                iSensorService!!.registerGeeUISensorDataListener(
//                    MainActivity::class.java.name,
//                    GeeUISensorsService.GEEUI_SENSOR_WRITE_RES,
//                    object : IGeeUISensorDataListener.Stub() {
//                        override fun onSensorDataChanged(sensorData: Int, sensorType: String?) {
//                            Log.d("write_res", "sensorType-$sensorType -- sensorData-$sensorData")
//                        }
//
//                        override fun onSensorWriteRes(res: String?) {
//                            Log.d("write_res", "res-$res")
//                        }
//                    })
//            } catch (e: RemoteException) {
//                e.printStackTrace()
//            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.d("MainActivity", "传感器服务绑定失败")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        GeeUISensorsManager.getInstance().registerGeeUISensorDataListener(
//            this,
//            GeeUISensorsManager.GEEUI_SENSOR_LIGHT,
//            object : GeeUISensorDataListener {
//                override fun onSensorDataChanged(sensorData: Int, sensorType: String) {
//                    Log.i("<<<", "sensorData: $sensorData, sensorType: $sensorType")
//                }
//
//                override fun onSensorWriteRes(res: String) {
//                    TODO("Not yet implemented")
//                }
//            })
        val mcuService = Intent(this@MainActivity, LTPMcuService::class.java)
        startService(mcuService)
        findViewById<View>(R.id.startService).setOnClickListener {
            Toast.makeText(this@MainActivity, "start service", Toast.LENGTH_SHORT).show()
            val mcuService = Intent(this@MainActivity, LTPMcuService::class.java)
            startService(mcuService)
//            val sensorIntentService = Intent(this@MainActivity, GeeUISensorsService::class.java)
//            bindService(sensorIntentService, serviceConnection, BIND_AUTO_CREATE) //绑定服务

        }
    }
}