package com.letianpai.sensorservice

object SerialAllJNI {

    /**
     *
     */
    external fun openPort(): Int

    /**
     *
     */
    external fun closePort():Boolean

    /**
     *
     */
    external fun writeData(data: String)

    /**
     *
     */
    external fun registerSensorDataListener(listener: MCUSensorListener)


    init {
        System.loadLibrary("SerialAllLib")
    }
}

interface MCUSensorListener {
    fun resListener(res: String)
    fun antListener(ant: String)
}