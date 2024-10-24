package com.letianpai.robot.mcuservice.foot

import android.content.Context

class WalkManager private constructor(private var mContext: Context) {
    init {
        init(mContext)
    }

    private fun init(context: Context) {
        this.mContext = context
    }

    companion object {
        var MOTOR_NUM_1: String = "1"
        var MOTOR_NUM_2: String = "2"
        var MOTOR_NUM_3: String = "3"
        var MOTOR_NUM_4: String = "4"
        var MOTOR_NUM_5: String = "5"
        var MOTOR_NUM_6: String = "6"

        private var instance: WalkManager? = null
        fun getInstance(context: Context): WalkManager {
            synchronized(WalkManager::class.java) {
                if (instance == null) {
                    instance = WalkManager(context.getApplicationContext())
                }
                return instance!!
            }
        } //    public float getMarginByAngle(){
        //
        //    }
    }
}
