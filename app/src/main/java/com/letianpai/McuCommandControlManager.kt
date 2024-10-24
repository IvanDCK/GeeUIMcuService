package com.letianpai

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.letianpai.robot.mcuservice.parser.motion.Motion
import com.renhejia.robot.commandlib.consts.RobotRemoteConsts
import com.letianpai.sensorservice.SerialAllJNI
import com.renhejia.robot.commandlib.consts.RobotRemoteConsts
import com.renhejia.robot.commandlib.log.LogUtils
import com.google.gson.Gson

import com.letianpai.McuResponseUtil.*
import com.renhejia.robot.commandlib.consts.ATCmdConsts
import com.renhejia.robot.commandlib.consts.MCUCommandConsts
import com.renhejia.robot.commandlib.parser.antennalight.AntennaLight
import com.renhejia.robot.commandlib.parser.antennamotion.AntennaMotion
import com.renhejia.robot.commandlib.parser.motion.Motion
import com.renhejia.robot.commandlib.parser.power.PowerMotion
import com.renhejia.robot.letianpaiservice.LtpCommand

import org.json.JSONException
import org.json.JSONObject


/**
 *
 */
class McuCommandControlManager private constructor(private val mContext: Context) {
    private val TAG = "McuCommandControlManager"
    private val TAG1 = "letianpai_test_control"
    private var mGson: Gson? = null


    init {
        init(mContext)
    }

    private fun init(context: Context) {
        mGson = Gson()
    }

    fun commandDistribute(command: String, data: String) {
        when (command) {
            RobotRemoteConsts.COMMAND_TYPE_MOTION -> {
                LogUtils.logd(TAG, "mcu 执行动作：$data")
                responseMotion(data)
            }
            RobotRemoteConsts.COMMAND_TYPE_ANTENNA_LIGHT -> {
                LogUtils.logd(TAG, "mcu 执行天线灯光：$data")
                responseAntennaLight(data)
            }
            RobotRemoteConsts.COMMAND_TYPE_ANTENNA_MOTION -> {
                LogUtils.logd(TAG, "mcu 执行天线：$data")
                responseAntennaMotion(data)
            }
            MCUCommandConsts.COMMAND_TYPE_POWER_CONTROL -> {
                LogUtils.logd("letianpai_test_control", "mcu COMMAND_TYPE_POWER_CONTROL：$data")
                Log.e(TAG, "mcu command：$command")
                responsePowerControl(data)
            }
            MCUCommandConsts.COMMAND_TYPE_RESET_MCU -> {
                LogUtils.logd("letianpai_test_control", "RESET_MCU：$data")
                resetMcu()
            }
            MCUCommandConsts.COMMAND_TYPE_START_GYROSCOPE -> {
                LogUtils.logd("letianpai_test_control", "start_gyroscope：$data")
                startGyroscope()
            }
            MCUCommandConsts.COMMAND_TYPE_STOP_GYROSCOPE -> {
                LogUtils.logd("letianpai_test_control", "stop_gyroscope：$data")
                stopGyroscope()
            }
            MCUCommandConsts.COMMAND_TYPE_GYROSCOPE -> {
                controlGyroscope(data)
            }
        }
    }

    private fun controlGyroscope(data: String) {
        Log.i(TAG, "McuCommandControlManager controlGyroscope: 控制陀螺仪:$data")
        //"{\n" +
//        "    \"cmd_value\":\"AT+FiAGW,2,10\",\n" +
//        "    \"update_time\":1693323185\n" +
//        "}"
        try {
            val jo: JSONObject = JSONObject(data)
            val mcuData: String = jo.optString("cmd_value")
            if (mcuData != null) {
                consumeATCommand("$mcuData\\r\\n")
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun stopGyroscope() {
        val command = String.format("AT+FiAGW,1\\r\\n")
        LogUtils.logi(TAG1, "关闭陀螺仪: $command")
        consumeATCommand(command)
    }

    private fun startGyroscope() {
        val command = String.format("AT+FiAGW,0\\r\\n")
        LogUtils.logi(TAG1, "启动陀螺仪 $command")
        consumeATCommand(command)
    }

    private fun resetMcu() {
        val command = String.format("AT+Reset\\r\\n")
        LogUtils.logi(TAG1, "MCU 关闭功能: $command")
        consumeATCommand(command)
    }

    private fun responsePowerControl(data: String) {
        val powerMotion: PowerMotion = mGson.fromJson<T>(data, PowerMotion::class.java)
        if (powerMotion == null || powerMotion.getFunction() === 0) {
            return
        }
        functionControl(powerMotion.getFunction(), powerMotion.getStatus())
    }

    private fun responseAntennaMotion(data: String) {
        val antennaMotion: AntennaMotion =
            mGson.fromJson<T>(data, AntennaMotion::class.java) ?: return
        LogUtils.logd(
            "McuCommandControlManager",
            "responseAntennaMotion: gson data:" + antennaMotion.toString()
        )
        turnAntennaMotion(
            antennaMotion.getCmd(),
            antennaMotion.getStep(),
            antennaMotion.getSpeed(),
            antennaMotion.getAngle()
        )
    }


    private fun responseAntennaLight(data: String) {
        val light: AntennaLight = mGson.fromJson<T>(data, AntennaLight::class.java) ?: return
        val lightCommand: String = light.getAntenna_light()
        val color: Int = light.getAntenna_light_color()

        if (lightCommand == MCUCommandConsts.COMMAND_TYPE_ANTENNA_LIGHT_VALUE_ON) {
            //TODO
            light(color)
        } else if (lightCommand == MCUCommandConsts.COMMAND_TYPE_ANTENNA_LIGHT_VALUE_OFF) {
            lightOff()
        }
    }

    private fun responseMotion(data: String) {
        LogUtils.logd(TAG, "responseMotion: $data")
        val motion: Motion = mGson.fromJson<T>(data, Motion::class.java)
        val motionType: String
        LogUtils.logd("McuCommandControlManager", "responseMotion: gson data:$motion")

        if (motion != null) {
            motionType = motion.motion
            if (TextUtils.isEmpty(motionType)) {
                return
            }
            LogUtils.logd(TAG, "responseMotion: $motionType")

            if (motionType == null || motionType == "null" || motion.equals("")) {
                var stepNum = 1
                if (motion.getStepNum() > 0) {
                    stepNum = motion.getStepNum()
                }
                var speed = 3
                if (motion.getSpeed() !== 0) {
                    speed = motion.getSpeed()
                }
                LogUtils.logd(TAG, "responseMotion: if==$motion")
                McuResponseUtil.consumeATCommand(motion.number, stepNum, speed)
            } else {
                var stepNum: Int = motion.getStepNum()
                if (stepNum == 0) {
                    stepNum = 1
                }
                LogUtils.logd(TAG, "responseMotion: else==$motion")
                when (motionType) {
                    MCUCommandConsts.COMMAND_VALUE_MOTION_FORWARD, ATCmdConsts.AT_STR_MOVEW_FORWARD -> walkForward(
                        stepNum
                    )

                    MCUCommandConsts.COMMAND_VALUE_MOTION_BACKEND, ATCmdConsts.AT_STR_MOVEW_BACK -> walkBackend(
                        stepNum
                    )

                    MCUCommandConsts.COMMAND_VALUE_MOTION_LEFT, ATCmdConsts.AT_STR_MOVEW_CRAB_STEP_LEFT -> McuResponseUtil.crabStepLeft(
                        stepNum
                    )

                    MCUCommandConsts.COMMAND_VALUE_MOTION_RIGHT, ATCmdConsts.AT_STR_MOVEW_CRAB_STEP_RIGHT ->                         //                walkRight(number)
                        McuResponseUtil.crabStepRight(stepNum)

                    MCUCommandConsts.COMMAND_VALUE_MOTION_LEFT_ROUND, ATCmdConsts.AT_STR_MOVEW_LOCAL_ROUND_LEFT -> {
                        LogUtils.logd(
                            "McuCommandControlManager",
                            "responseMotion:COMMAND_VALUE_MOTION_LEFT $stepNum"
                        )
                        localRoundLeft(stepNum)
                    }

                    MCUCommandConsts.COMMAND_VALUE_MOTION_RIGHT_ROUND, ATCmdConsts.AT_STR_MOVEW_TURN_RIGHT, ATCmdConsts.AT_STR_MOVEW_LOCAL_ROUND_RIGHT -> McuResponseUtil.localRoundRight(
                        stepNum
                    )

                    MCUCommandConsts.COMMAND_VALUE_MOTION_SET_STRAIGHT, ATCmdConsts.AT_STR_MOVEW_STAND -> McuResponseUtil.walkStand()
                    ATCmdConsts.AT_STR_MOVEW_TURN_LEFT -> turnLeft(stepNum)
                    ATCmdConsts.AT_STR_MOVEW_SHAKE_LEFT_LEG, ATCmdConsts.AT_STR_MOVEW_SHAKE_LEFT_LEG1 -> McuResponseUtil.shakeLeftLeg(
                        stepNum
                    )

                    ATCmdConsts.AT_STR_MOVEW_SHAKE_RIGHT_LEG, ATCmdConsts.AT_STR_MOVEW_SHAKE_RIGHT_LEG1 -> McuResponseUtil.shakeRightLeg(
                        stepNum
                    )

                    ATCmdConsts.AT_STR_MOVEW_SHAKE_LEFT_FOOT, ATCmdConsts.AT_STR_MOVEW_SHAKE_LEFT_FOOT1 -> McuResponseUtil.shakeLeftFoot(
                        stepNum
                    )

                    ATCmdConsts.AT_STR_MOVEW_SHAKE_RIGHT_FOOT, ATCmdConsts.AT_STR_MOVEW_SHAKE_RIGHT_FOOT1 -> McuResponseUtil.shakeRightFoot(
                        stepNum
                    )

                    ATCmdConsts.AT_STR_MOVEW_SHAKE_CROSS_LEFT_FOOT, ATCmdConsts.AT_STR_MOVEW_SHAKE_CROSS_LEFT_FOOT1 -> McuResponseUtil.shakeCrossLeftFoot(
                        stepNum
                    )

                    ATCmdConsts.AT_STR_MOVEW_SHAKE_CROSS_RIGHT_FOOT, ATCmdConsts.AT_STR_MOVEW_SHAKE_CROSS_RIGHT_FOOT1 -> McuResponseUtil.shakeCrossRightFoot(
                        stepNum
                    )

                    ATCmdConsts.AT_STR_MOVEW_SHAKE_LEFT_LEANING, ATCmdConsts.AT_STR_MOVEW_SHAKE_LEFT_LEANING1 -> McuResponseUtil.shakeLeftLeaning(
                        stepNum
                    )

                    ATCmdConsts.AT_STR_MOVEW_SHAKE_RIGHT_LEANING, ATCmdConsts.AT_STR_MOVEW_SHAKE_RIGHT_LEANING1 -> McuResponseUtil.shakeRightLeaning(
                        stepNum
                    )

                    ATCmdConsts.AT_STR_MOVEW_STAMP_LEFT_FOOT, ATCmdConsts.AT_STR_MOVEW_STAMP_LEFT_FOOT1 -> McuResponseUtil.stampLeftFoot(
                        stepNum
                    )

                    ATCmdConsts.AT_STR_MOVEW_STAMP_RIGHT_FOOT, ATCmdConsts.AT_STR_MOVEW_STAMP_RIGHT_FOOT1 -> McuResponseUtil.stampRightFoot(
                        stepNum
                    )

                    ATCmdConsts.AT_STR_MOVEW_SHAKE_SWAYING_UP_AND_DOWN, ATCmdConsts.AT_STR_MOVEW_SHAKE_SWAYING_UP_AND_DOWN1 -> McuResponseUtil.swayingUpdAndDown(
                        stepNum
                    )

                    ATCmdConsts.AT_STR_MOVEW_SHAKE_SWINGS_FROM_SIDE_TO_SIDE, ATCmdConsts.AT_STR_MOVEW_SHAKE_SWINGS_FROM_SIDE_TO_SIDE1 -> McuResponseUtil.swingsFromSideToSide(
                        stepNum
                    )

                    ATCmdConsts.AT_STR_MOVEW_SHAKE_HEAD, ATCmdConsts.AT_STR_MOVEW_SHAKE_HEAD1 -> McuResponseUtil.shakeHead(
                        stepNum
                    )

                    ATCmdConsts.AT_STR_MOVEW_STAND_AT_EASE, ATCmdConsts.AT_STR_MOVEW_STAND_AT_EASE1, ATCmdConsts.AT_STR_MOVEW_SHAKE_LEG, ATCmdConsts.AT_STR_MOVEW_FEET_TREMOR -> McuResponseUtil.standAtEase(
                        stepNum
                    )

                    ATCmdConsts.AT_STR_MOVEW_MICROTREMOR -> McuResponseUtil.microTremor(stepNum)
                    ATCmdConsts.AT_STR_MOVEW_29 -> McuResponseUtil.atMovEW29(stepNum)
                    ATCmdConsts.AT_STR_MOVEW_30 -> McuResponseUtil.atMovEW30(stepNum)
                    ATCmdConsts.AT_STR_MOVEW_31 -> McuResponseUtil.atMovEW31(stepNum)
                    ATCmdConsts.AT_STR_MOVEW_32 -> McuResponseUtil.atMovEW32(stepNum)
                    ATCmdConsts.AT_STR_MOVEW_33 -> McuResponseUtil.atMovEW33(stepNum)
                    ATCmdConsts.AT_STR_MOVEW_34 -> McuResponseUtil.atMovEW34(stepNum)
                    ATCmdConsts.AT_STR_MOVEW_35 -> McuResponseUtil.atMovEW35(stepNum)
                    ATCmdConsts.AT_STR_MOVEW_36 -> McuResponseUtil.atMovEW36(stepNum)
                    ATCmdConsts.AT_STR_MOVEW_37 -> McuResponseUtil.atMovEW37(stepNum)
                    ATCmdConsts.AT_STR_MOVEW_38 -> McuResponseUtil.atMovEW38(stepNum)
                    ATCmdConsts.AT_STR_MOVEW_39 -> McuResponseUtil.atMovEW39(stepNum)
                    ATCmdConsts.AT_STR_MOVEW_40 -> McuResponseUtil.atMovEW40(stepNum)
                    ATCmdConsts.AT_STR_MOVEW_41 -> McuResponseUtil.atMovEW41(stepNum)
                    ATCmdConsts.AT_STR_MOVEW_42 -> McuResponseUtil.atMovEW42(stepNum)
                    ATCmdConsts.AT_STR_MOVEW_43 -> McuResponseUtil.atMovEW43(stepNum)
                    ATCmdConsts.AT_STR_MOVEW_44 -> McuResponseUtil.atMovEW44(stepNum)
                    ATCmdConsts.AT_STR_MOVEW_45 -> McuResponseUtil.atMovEW45(stepNum)
                    ATCmdConsts.AT_STR_MOVEW_46 -> McuResponseUtil.atMovEW46(stepNum)
                    ATCmdConsts.AT_STR_MOVEW_47 -> McuResponseUtil.atMovEW47(stepNum)
                    ATCmdConsts.AT_STR_MOVEW_48 -> McuResponseUtil.atMovEW48(stepNum)
                    ATCmdConsts.AT_STR_MOVEW_49 -> McuResponseUtil.atMovEW49(stepNum)
                    ATCmdConsts.AT_STR_MOVEW_50 -> McuResponseUtil.atMovEW50(stepNum)
                    ATCmdConsts.AT_STR_MOVEW_51 -> McuResponseUtil.atMovEW51(stepNum)
                    ATCmdConsts.AT_STR_MOVEW_52 -> McuResponseUtil.atMovEW52(stepNum)
                    ATCmdConsts.AT_STR_MOVEW_53 -> McuResponseUtil.atMovEW53(stepNum)
                    ATCmdConsts.AT_STR_MOVEW_54 -> McuResponseUtil.atMovEW54(stepNum)
                    ATCmdConsts.AT_STR_MOVEW_55 -> McuResponseUtil.atMovEW55(stepNum)
                    ATCmdConsts.AT_STR_MOVEW_56 -> McuResponseUtil.atMovEW56(stepNum)
                    ATCmdConsts.AT_STR_MOVEW_57 -> McuResponseUtil.atMovEW57(stepNum)
                    ATCmdConsts.AT_STR_MOVEW_58 -> McuResponseUtil.atMovEW58(stepNum)
                    ATCmdConsts.AT_STR_MOVEW_59 -> McuResponseUtil.atMovEW59(stepNum)
                    ATCmdConsts.AT_STR_MOVEW_60 -> McuResponseUtil.atMovEW60(stepNum)
                    else -> McuResponseUtil.consumeATCommand(motion.number, stepNum, 3)
                }
            }
        }

        //walkBackend(2)
    }


    fun consumeATCommand(command: String) {
        LogUtils.logi(TAG, "consumeATCommand --$command")
        LogUtils.logi(TAG1, "mcu_responsePowerControl_5$command")

        if (!TextUtils.isEmpty(command)) {
            writeCommand(command)
        }
    }

    private fun writeCommand(command: String) {
        SerialAllJNI.writeData(command)
    }

    private fun walkForward(stepNum: Int) {
        walks(1, stepNum)
    }

    private fun walkBackend(stepNum: Int) {
        walks(2, stepNum)
    }

    private fun walkLeft(stepNum: Int) {
        walks(3, stepNum)
    }

    private fun walkRight(stepNum: Int) {
        walks(4, stepNum)
    }

    private fun walks(moveCommand: Int, stepNum: Int) {
        val command = String.format("AT+MOVEW,%d,%d,2\\r\\n", moveCommand, stepNum)
        LogUtils.logi("letianpai_walks", "command: $command")
        consumeATCommand(command)
    }

    private fun turnEarLeftRound() {
        consumeATCommand("AT+EARW,1,2,2\\r\\n")
    }

    private fun turnEarRightRound() {
        consumeATCommand("AT+EARW,2,2,2\\r\\n")
    }

    fun light(lightColor: Int) {
        val command = String.format("AT+LEDOn,%d\\r\\n", lightColor)
        LogUtils.logi("letianpai_walks", "command: $command")
        consumeATCommand(command)
    }

    fun lightOff() {
        val command = String.format("AT+LEDOff\\r\\n")
        LogUtils.logi("letianpai_walks", "command: $command")
        consumeATCommand(command)
    }

    fun turnAntennaMotion(cmd: Int, step: Int, speed: Int, angle: Int) {
        var step = step
        var speed = speed
        var angle = angle
        if (step == 0) {
            step = 1
        }
        if (speed == 0) {
            speed = 300
        }
        if (angle == 0) {
            angle = 90
        }
        val command = String.format("AT+EARW,%d,%d,%d,%d\\r\\n", cmd, step, speed, angle)
        LogUtils.logi("letianpai_ear", "turnAntennaMotion_command: $command")
        consumeATCommand(command)
    }

    /**
     * 1--环境光任务
     * 2--触摸任务（暂不支持）
     * 3--腿和脚舵机电源
     * 4--耳朵灯（只支持关）
     * 5--悬崖和悬空检测任务
     * 6--陀螺仪数据采集与欧拉角算法任务
     * status：
     * 0--关闭
     * 1--开启
     * 举例：
     */
    /**
     * @param func   功能：
     * @param status
     */
    fun functionControl(func: Int, status: Int) {
        LogUtils.logd("McuCommandControlManager", "functionControl: func: $func status:  $status")
        val command = String.format("AT+FunCtr,%d,%d\\r\\n", func, status)
        LogUtils.logi(TAG1, "MCU 关闭功能: $command")
        consumeATCommand(command)
    }

    companion object {
        private var instance: McuCommandControlManager? = null
        @JvmStatic
        fun getInstance(context: Context): McuCommandControlManager {
            synchronized(McuCommandControlManager::class.java) {
                if (instance == null) {
                    instance = McuCommandControlManager(context.applicationContext)
                }
                return instance!!
            }
        }
    }
}
