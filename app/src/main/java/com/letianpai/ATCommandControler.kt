package com.letianpai

import android.content.Context
import android.text.TextUtils

import java.util.ArrayList

import com.letianpai.sensorservice.SerialAllJNI
import top.keepempty.sph.library.LogUtil

class ATCommandControler private constructor() {
    private val atCommandList = ArrayList<String>()
    private val mContext: Context? = null
    var isATCommandConsuming: Boolean = false
        private set


    //    private ATCommandControler(Context context) {
    //        init(context)
    //    }
    //
    //    public static ATCommandControler getInstance(Context context) {
    //        synchronized (ATCommandControler.class) {
    //            if (instance == null) {
    //                instance = new ATCommandControler(context.getApplicationContext())
    //            }
    //            return instance
    //        }
    //
    //    }
    init {
        addAtCommandListeners()
    }

    private fun addAtCommandListeners() {
        AtCommandCallback.instance.setAtCmdResultReturnListener { atCmdResult ->
            LogUtil.e("AtCommandCallback_onAtCmdResultReturn: $atCmdResult")
            consumeATCommands()
        }
    }

    //    private void init(Context context) {
    //        this.mContext = context
    //
    //    }
    fun addATCommand(command: String) {
        if (!TextUtils.isEmpty(command)) {
            atCommandList.add(command)
            if (!isATCommandConsuming) {
                consumeATCommands()
            }
        }
    }

    fun addATCommands(commands: ArrayList<String>) {
        atCommandList.addAll(commands)
    }

    fun consumeATCommands() {
        if (atCommandList.size > 0) {
            LogUtil.e("atCommandList.size(): " + atCommandList.size)
            isATCommandConsuming = true
            val command = atCommandList.removeAt(0)
            consumeATCommand(command)
        } else {
            LogUtil.e("atCommandList.size() is 0 ")
            isATCommandConsuming = false
        }
    }

    fun consumeATCommand(command: String) {
        writeCommand(command)
    }

    private fun writeCommand(command: String) {
        SerialAllJNI.writeData(command)
        // if (!TextUtils.isEmpty(command)) {
        //     SerialPortJNI.writePort(command.getBytes())
        // }
    }

    companion object {
        @JvmStatic
        var instance: ATCommandControler? = null
            get() {
                synchronized(ATCommandControler::class.java) {
                    if (field == null) {
                        field = ATCommandControler()
                    }
                    return field
                }
            }
            private set
    }
}
