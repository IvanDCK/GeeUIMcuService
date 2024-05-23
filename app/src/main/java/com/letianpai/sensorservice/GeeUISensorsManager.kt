package com.letianpai.sensorservice

import android.util.Log
import kotlinx.coroutines.*
import java.util.concurrent.ArrayBlockingQueue
import kotlin.system.measureTimeMillis
/*
class GeeUISensorsManager private constructor(){

    val queue = ArrayBlockingQueue<String>(1000)
    val list = listOf(
        "AT+INT,light,50\\r\\n",
        "AT+INT,person,1\\r\\n",
        "AT+INT,touch,longK\\r\\n",//0
        "AT+INT,touch,singleK\\r\\n",//1
        "AT+INT,touch,doubleK\\r\\n",//2
        "AT+INT,cliff,1\\r\\n",
        "AT+INT,cliff,0\\r\\n",
        "AT+INT,suspend,1\\r\\n",
        "AT+INT,suspend,0\\r\\n",
        "AT+INT,waggle,1\\r\\n",
        "AT+INT,down,0\\r\\n",
        "AT+INT,down,1\\r\\n",
        "AT+INT,down,2\\r\\n",
        "AT+INT,down,3\\r\\n",
        "AT+INT,down,5\\r\\n",
    )
    private val job = Job()  // 创建一个 Job 对象
    private val scope = CoroutineScope(Dispatchers.Default + job)  // 创建协程作用域
    private val lightListenerMap = HashMap<Class<*>, GeeUISensorDataListener>()
    private val touchListenerMap = HashMap<Class<*>, GeeUISensorDataListener>()
    private val cliffListenerMap = HashMap<Class<*>, GeeUISensorDataListener>()
    private val suspendListenerMap = HashMap<Class<*>, GeeUISensorDataListener>()
    private val waggleListenerMap = HashMap<Class<*>, GeeUISensorDataListener>()
    private val downListenerMap = HashMap<Class<*>, GeeUISensorDataListener>()

    companion object{
        const val GEEUI_SENSOR_LIGHT = "light"
        const val GEEUI_SENSOR_TOUCH = "touch"
        const val GEEUI_SENSOR_CLIFF = "cliff"
        const val GEEUI_SENSOR_SUSPEND = "suspend"
        const val GEEUI_SENSOR_WAGGLE = "waggle"
        const val GEEUI_SENSOR_DOWN = "down"
        private var instance: GeeUISensorsManager? = null
        @Synchronized
        fun getInstance(): GeeUISensorsManager {
            if (instance == null) {
                instance = GeeUISensorsManager()
            }
            return instance!!
        }
    }

    fun registerGeeUISensorDataListener(register: Any, sensorType: String,listener: GeeUISensorDataListener){
        val subscriberClass: Class<*> = register::class.java
        when(sensorType){
            GEEUI_SENSOR_LIGHT ->{
                synchronized(this){
                    lightListenerMap.put(subscriberClass, listener)
                }
            }

            GEEUI_SENSOR_TOUCH ->{
                synchronized(this){
                    touchListenerMap.put(subscriberClass, listener)
                }
            }
            GEEUI_SENSOR_CLIFF ->{
                synchronized(this){
                    cliffListenerMap.put(subscriberClass, listener)
                }
            }
            GEEUI_SENSOR_SUSPEND ->{
                synchronized(this){
                    suspendListenerMap.put(subscriberClass, listener)
                }
            }
            GEEUI_SENSOR_WAGGLE ->{
                synchronized(this){
                    waggleListenerMap.put(subscriberClass, listener)
                }
            }
            GEEUI_SENSOR_DOWN ->{
                synchronized(this){
                    downListenerMap.put(subscriberClass, listener)
                }
            }
        }
    }

    fun unRegisterGeeUISensorDataListener(register: Any, sensorType: String){
        val subscriberClass: Class<*> = register::class.java
        when(sensorType){
            GEEUI_SENSOR_LIGHT ->{
                synchronized(this){
                    lightListenerMap.remove(subscriberClass)
                }
            }

            GEEUI_SENSOR_TOUCH ->{
                synchronized(this){
                    touchListenerMap.remove(subscriberClass)
                }
            }
            GEEUI_SENSOR_CLIFF ->{
                synchronized(this){
                    cliffListenerMap.remove(subscriberClass)
                }
            }
            GEEUI_SENSOR_SUSPEND ->{
                synchronized(this){
                    suspendListenerMap.remove(subscriberClass)
                }
            }
            GEEUI_SENSOR_WAGGLE ->{
                synchronized(this){
                    waggleListenerMap.remove(subscriberClass)
                }
            }
            GEEUI_SENSOR_DOWN ->{
                synchronized(this){
                    downListenerMap.remove(subscriberClass)
                }
            }
        }
    }


    suspend fun produce() {
        Log.d("GeeUISensorsService", "oncreate")
        scope.launch {
             withContext(Dispatchers.IO){
                 var i = 0
                 while (true) {
                     delay(200)
                     queue.put(list.random())
                     println("Produced $i")
                     println("Produced total size-- ${queue.size}")
                 }
             }
         }
    }

    suspend fun consumer() {
        Log.d("GeeUISensorsService", "onStartCommand")
        scope.launch {
            val numCoroutines = 6 // 设置要并发执行的协程数量
            withContext(Dispatchers.IO){
                while (true){
                    val time = measureTimeMillis {
                        val jobs = List(numCoroutines) {
                            launch(Dispatchers.Default) { // 在默认的调度器上启动协程
                                doSomeWork()
                            }
                        }
                        jobs.forEach { it.join() }
                    }
                    println("Total time: $time ms")
                }
            }
        }
    }


    suspend fun doSomeWork() {
        delay(1000)
        synchronized(this){
            var d = queue.take()
            d = d.replace("\\r\\n", "")
            val data = d.split(",")
            // TODO: 这里解析消息，进行分发
            Log.i("<<<","---------data.size ${data.size}")
            Log.i("<<<","--------- data[0] ${data[0] == "AT+INT"}")
            if (data.size == 3 && data[0] == "AT+INT"){
                Log.i("<<<","---------lightListenerMap.size ${lightListenerMap.size}")
                //传感器上报数据
                val type = data[1]
                when(type){
                    GEEUI_SENSOR_LIGHT ->{
                        for ((_, listener) in lightListenerMap) {
                            Log.i("<<<","listener --$listener")
                            listener.onSensorDataChanged(data[2].toInt(), type)
                        }
                    }

                    GEEUI_SENSOR_TOUCH ->{
                        val touchValue = data[2]
                        var tempV = 0
                        if(touchValue === "doubleK"){
                            tempV = 2
                        }else if (touchValue === "singleK"){
                            tempV = 1
                        }else{
                            tempV = 0
                        }
                        for ((_, listener) in touchListenerMap) {
                            listener.onSensorDataChanged(tempV, type)
                        }
                    }
                    GEEUI_SENSOR_CLIFF ->{
                        for ((_, listener) in cliffListenerMap) {
                            listener.onSensorDataChanged(data[2].toInt(), type)
                        }
                    }
                    GEEUI_SENSOR_SUSPEND ->{
                        for ((_, listener) in suspendListenerMap) {
                            listener.onSensorDataChanged(data[2].toInt(), type)
                        }
                    }
                    GEEUI_SENSOR_WAGGLE ->{
                        for ((_, listener) in waggleListenerMap) {
                            listener.onSensorDataChanged(data[2].toInt(), type)
                        }
                    }
                    GEEUI_SENSOR_DOWN ->{
                        for ((_, listener) in downListenerMap) {
                            listener.onSensorDataChanged(data[2].toInt(), type)
                        }
                    }
                }
            }
            println("Consumed $data")
            println("Consumed size -- ${queue.size}")
            println("Work done by ${Thread.currentThread().name}")
        }
    }

    fun destroy(){
        job.cancel()
    }
}

 */