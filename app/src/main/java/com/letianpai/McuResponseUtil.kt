package com.letianpai

import com.renhejia.robot.commandlib.log.LogUtils
import com.letianpai.sensorservice.SerialAllJNI
import com.renhejia.robot.commandlib.consts.ATCmdConsts
import android.text.TextUtils;

object McuResponseUtil {
    /**
     * 立正
     */
    fun walkStand() {
        consumeATCommand(ATCmdConsts.AT_MOVEW_STAND, 1, 1)
    }

    /**
     * 向前走
     * @param stepNum
     * @param speed
     */
    /**
     * 向前走
     * @param stepNum
     */
    @JvmOverloads
    fun walkForward(stepNum: Int, speed: Int = 3) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_FORWARD, stepNum, speed)
    }

    /**
     * 向后走
     * @param stepNum
     * @param speed
     */
    /**
     * 向后走
     * @param stepNum
     */
    @JvmOverloads
    fun walkBackend(stepNum: Int, speed: Int = 3) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_BACK, stepNum, speed)
    }

    /**
     * 向左转
     * @param stepNum
     * @param speed
     */
    /**
     * 向左转
     * @param stepNum
     */
    @JvmOverloads
    fun turnLeft(stepNum: Int, speed: Int = 3) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_TURN_LEFT, stepNum, speed)
    }

    /**
     * 向右转
     * @param stepNum
     * @param speed
     */
    /**
     * 向右转
     * @param stepNum
     */
    @JvmOverloads
    fun turnRight(stepNum: Int, speed: Int = 3) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_TURN_RIGHT, stepNum, speed)
    }

    /**
     * 螃蟹步向左
     * @param stepNum
     */
    fun crabStepLeft(stepNum: Int) {
        LogUtils.logi("letianpai", "COMMAND_VALUE_MOTION_LEFT:=====crabStepLeft====1=2 ")
        crabStepLeft(stepNum, 3)
    }

    /**
     * 螃蟹步向左
     * @param stepNum
     * @param speed
     */
    fun crabStepLeft(stepNum: Int, speed: Int) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_CRAB_STEP_LEFT, stepNum, speed)
    }

    /**
     * 螃蟹步向右
     * @param stepNum
     * @param speed
     */
    /**
     * 螃蟹步向右
     * @param stepNum
     */
    @JvmOverloads
    fun crabStepRight(stepNum: Int, speed: Int = 3) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_CRAB_STEP_RIGHT, stepNum, speed)
    }

    /**
     * 抖左腿
     * @param stepNum
     * @param speed
     */
    /**
     * 抖左腿
     * @param stepNum
     */
    @JvmOverloads
    fun shakeLeftLeg(stepNum: Int, speed: Int = 2) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_SHAKE_LEFT_LEG, stepNum, speed)
    }

    /**
     * 抖右腿
     * @param stepNum
     * @param speed
     */
    /**
     * 抖右腿
     * @param stepNum
     */
    @JvmOverloads
    fun shakeRightLeg(stepNum: Int, speed: Int = 2) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_SHAKE_RIGHT_LEG, stepNum, speed)
    }

    /**
     * 抖左脚
     * @param stepNum
     * @param speed
     */
    /**
     * 抖左脚
     * @param stepNum
     */
    @JvmOverloads
    fun shakeLeftFoot(stepNum: Int, speed: Int = 3) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_SHAKE_LEFT_FOOT, stepNum, speed)
    }

    /**
     * 抖右腿
     * @param stepNum
     * @param speed
     */
    /**
     * 抖右腿
     * @param stepNum
     */
    @JvmOverloads
    fun shakeRightFoot(stepNum: Int, speed: Int = 3) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_SHAKE_RIGHT_FOOT, stepNum, speed)
    }

    /**
     * 左跷脚
     * @param stepNum
     * @param speed
     */
    /**
     * 左跷脚
     * @param stepNum
     */
    @JvmOverloads
    fun shakeCrossLeftFoot(stepNum: Int, speed: Int = 3) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_SHAKE_CROSS_LEFT_FOOT, stepNum, speed)
    }

    /**
     * 右跷脚
     * @param stepNum
     * @param speed
     */
    /**
     * 右跷脚
     * @param stepNum
     */
    @JvmOverloads
    fun shakeCrossRightFoot(stepNum: Int, speed: Int = 3) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_SHAKE_CROSS_RIGHT_FOOT, stepNum, speed)
    }

    /**
     * 右跷脚
     * @param stepNum
     * @param speed
     */
    /**
     *
     * @param stepNum
     */
    @JvmOverloads
    fun shakeLeftLeaning(stepNum: Int, speed: Int = 6) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_SHAKE_LEFT_LEANING, stepNum, speed)
    }

    /**
     * 右倾身
     * @param stepNum
     * @param speed
     */
    /**
     * 右倾身
     * @param stepNum
     */
    @JvmOverloads
    fun shakeRightLeaning(stepNum: Int, speed: Int = 6) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_SHAKE_RIGHT_LEANING, stepNum, speed)
    }

    /**
     * 左跺脚
     * @param stepNum
     * @param speed
     */
    /**
     * 左跺脚
     * @param stepNum
     */
    @JvmOverloads
    fun stampLeftFoot(stepNum: Int, speed: Int = 3) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_STAMP_LEFT_FOOT, stepNum, speed)
    }

    /**
     * 左跺脚
     * @param stepNum
     * @param speed
     */
    /**
     * 左跺脚
     * @param stepNum
     */
    @JvmOverloads
    fun stampRightFoot(stepNum: Int, speed: Int = 3) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_STAMP_RIGHT_FOOT, stepNum, speed)
    }

    /**
     * 左跺脚
     * @param stepNum
     * @param speed
     */
    /**
     * 左跺脚
     * @param stepNum
     */
    @JvmOverloads
    fun swayingUpdAndDown(stepNum: Int, speed: Int = 1) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_SHAKE_SWAYING_UP_AND_DOWN, stepNum, speed)
    }

    /**
     * 左跺脚
     * @param stepNum
     * @param speed
     */
    /**
     * 左跺脚
     * @param stepNum
     */
    @JvmOverloads
    fun swingsFromSideToSide(stepNum: Int, speed: Int = 1) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_SHAKE_SWINGS_FROM_SIDE_TO_SIDE, stepNum, speed)
    }

    /**
     * 摇头
     * @param stepNum
     * @param speed
     */
    /**
     * 摇头
     * @param stepNum
     */
    @JvmOverloads
    fun shakeHead(stepNum: Int, speed: Int = 1) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_SHAKE_HEAD, stepNum, speed)
    }

    /**
     * 稍息
     * @param stepNum
     * @param speed
     */
    /**
     * 稍息
     * @param stepNum
     */
    @JvmOverloads
    fun standAtEase(stepNum: Int, speed: Int = 1) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_STAND_AT_EASE, stepNum, speed)
    }

    /**
     * 原地左转
     * @param stepNum
     * @param speed
     */
    /**
     * 原地左转
     * @param stepNum
     */
    @JvmOverloads
    fun localRoundLeft(stepNum: Int, speed: Int = 3) {
        LogUtils.logi(
            "letianpai",
            "COMMAND_VALUE_MOTION_LEFT:=====crabStepLeft====1=2=3=4======5 $stepNum  $speed"
        )
        consumeATCommand(ATCmdConsts.AT_MOVEW_LOCAL_ROUND_LEFT, stepNum, speed)
    }

    /**
     * 原地右转
     * @param stepNum
     * @param speed
     */
    /**
     * 原地右转
     * @param stepNum
     */
    @JvmOverloads
    fun localRoundRight(stepNum: Int, speed: Int = 3) {
        LogUtils.logi(
            "letianpai",
            "COMMAND_VALUE_MOTION_LEFT:=====crabStepLeft====1=2=3=4=========6 "
        )
        consumeATCommand(ATCmdConsts.AT_MOVEW_LOCAL_ROUND_RIGHT, stepNum, speed)
    }

    /**
     * 双抖脚
     * @param stepNum
     * @param speed
     */
    /**
     * 双抖脚
     * @param stepNum
     */
    @JvmOverloads
    fun feetTremor(stepNum: Int, speed: Int = 1) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_FEET_TREMOR, stepNum, speed)
    }

    /**
     * 微颤
     * @param stepNum
     * @param speed
     */
    /**
     * 微颤
     * @param stepNum
     */
    @JvmOverloads
    fun microTremor(stepNum: Int, speed: Int = 3) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_MICROTREMOR, stepNum, speed)
    }

    /**
     *
     * @param stepNum
     * @param speed
     */
    /**
     *
     * @param stepNum
     */
    @JvmOverloads
    fun atMovEW29(stepNum: Int, speed: Int = 4) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_29, stepNum, speed)
    }

    /**
     *
     * @param stepNum
     * @param speed
     */
    /**
     *
     * @param stepNum
     */
    @JvmOverloads
    fun atMovEW30(stepNum: Int, speed: Int = 4) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_30, stepNum, speed)
    }

    /**
     *
     * @param stepNum
     */
    fun atMovEW31(stepNum: Int) {
        LogUtils.logi("letianpai_test", "AT_STR_MOVEW_31: ===================2  ")
        atMovEW31(stepNum, 4)
    }

    /**
     *
     * @param stepNum
     * @param speed
     */
    fun atMovEW31(stepNum: Int, speed: Int) {
        LogUtils.logi("letianpai_test", "AT_STR_MOVEW_31: ===================3  ")
        consumeATCommand(ATCmdConsts.AT_MOVEW_31, stepNum, speed)
    }

    /**
     *
     * @param stepNum
     * @param speed
     */
    /**
     *
     * @param stepNum
     */
    @JvmOverloads
    fun atMovEW32(stepNum: Int, speed: Int = 4) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_32, stepNum, speed)
    }

    /**
     *
     * @param stepNum
     * @param speed
     */
    /**
     *
     * @param stepNum
     */
    @JvmOverloads
    fun atMovEW33(stepNum: Int, speed: Int = 4) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_33, stepNum, speed)
    }

    /**
     *
     * @param stepNum
     * @param speed
     */
    /**
     *
     * @param stepNum
     */
    @JvmOverloads
    fun atMovEW34(stepNum: Int, speed: Int = 4) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_34, stepNum, speed)
    }

    /**
     *
     * @param stepNum
     * @param speed
     */
    /**
     *
     * @param stepNum
     */
    @JvmOverloads
    fun atMovEW35(stepNum: Int, speed: Int = 4) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_35, stepNum, speed)
    }

    /**
     *
     * @param stepNum
     * @param speed
     */
    /**
     *
     * @param stepNum
     */
    @JvmOverloads
    fun atMovEW36(stepNum: Int, speed: Int = 4) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_36, stepNum, speed)
    }

    /**
     *
     * @param stepNum
     * @param speed
     */
    /**
     *
     * @param stepNum
     */
    @JvmOverloads
    fun atMovEW37(stepNum: Int, speed: Int = 4) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_37, stepNum, speed)
    }

    /**
     *
     * @param stepNum
     * @param speed
     */
    /**
     *
     * @param stepNum
     */
    @JvmOverloads
    fun atMovEW38(stepNum: Int, speed: Int = 4) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_38, stepNum, speed)
    }

    /**
     *
     * @param stepNum
     * @param speed
     */
    /**
     *
     * @param stepNum
     */
    @JvmOverloads
    fun atMovEW39(stepNum: Int, speed: Int = 4) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_39, stepNum, speed)
    }

    /**
     *
     * @param stepNum
     * @param speed
     */
    /**
     *
     * @param stepNum
     */
    @JvmOverloads
    fun atMovEW40(stepNum: Int, speed: Int = 4) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_40, stepNum, speed)
    }

    /**
     *
     * @param stepNum
     * @param speed
     */
    /**
     *
     * @param stepNum
     */
    @JvmOverloads
    fun atMovEW41(stepNum: Int, speed: Int = 4) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_41, stepNum, speed)
    }

    /**
     *
     * @param stepNum
     * @param speed
     */
    /**
     *
     * @param stepNum
     */
    @JvmOverloads
    fun atMovEW42(stepNum: Int, speed: Int = 4) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_42, stepNum, speed)
    }

    /**
     *
     * @param stepNum
     * @param speed
     */
    /**
     *
     * @param stepNum
     */
    @JvmOverloads
    fun atMovEW43(stepNum: Int, speed: Int = 4) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_43, stepNum, speed)
    }

    /**
     *
     * @param stepNum
     * @param speed
     */
    /**
     *
     * @param stepNum
     */
    @JvmOverloads
    fun atMovEW44(stepNum: Int, speed: Int = 4) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_44, stepNum, speed)
    }

    /**
     *
     * @param stepNum
     * @param speed
     */
    /**
     *
     * @param stepNum
     */
    @JvmOverloads
    fun atMovEW45(stepNum: Int, speed: Int = 4) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_45, stepNum, speed)
    }

    /**
     *
     * @param stepNum
     * @param speed
     */
    /**
     *
     * @param stepNum
     */
    @JvmOverloads
    fun atMovEW46(stepNum: Int, speed: Int = 4) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_46, stepNum, speed)
    }

    /**
     *
     * @param stepNum
     * @param speed
     */
    /**
     *
     * @param stepNum
     */
    @JvmOverloads
    fun atMovEW47(stepNum: Int, speed: Int = 4) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_47, stepNum, speed)
    }

    /**
     *
     * @param stepNum
     * @param speed
     */
    /**
     *
     * @param stepNum
     */
    @JvmOverloads
    fun atMovEW48(stepNum: Int, speed: Int = 4) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_48, stepNum, speed)
    }

    /**
     *
     * @param stepNum
     * @param speed
     */
    /**
     *
     * @param stepNum
     */
    @JvmOverloads
    fun atMovEW49(stepNum: Int, speed: Int = 4) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_49, stepNum, speed)
    }

    /**
     *
     * @param stepNum
     * @param speed
     */
    /**
     *
     * @param stepNum
     */
    @JvmOverloads
    fun atMovEW50(stepNum: Int, speed: Int = 4) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_50, stepNum, speed)
    }

    /**
     *
     * @param stepNum
     * @param speed
     */
    /**
     *
     * @param stepNum
     */
    @JvmOverloads
    fun atMovEW51(stepNum: Int, speed: Int = 4) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_51, stepNum, speed)
    }

    /**
     *
     * @param stepNum
     * @param speed
     */
    /**
     *
     * @param stepNum
     */
    @JvmOverloads
    fun atMovEW52(stepNum: Int, speed: Int = 4) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_52, stepNum, speed)
    }

    /**
     *
     * @param stepNum
     * @param speed
     */
    /**
     *
     * @param stepNum
     */
    @JvmOverloads
    fun atMovEW53(stepNum: Int, speed: Int = 4) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_53, stepNum, speed)
    }

    /**
     *
     * @param stepNum
     * @param speed
     */
    /**
     *
     * @param stepNum
     */
    @JvmOverloads
    fun atMovEW54(stepNum: Int, speed: Int = 4) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_54, stepNum, speed)
    }

    /**
     *
     * @param stepNum
     * @param speed
     */
    /**
     *
     * @param stepNum
     */
    @JvmOverloads
    fun atMovEW55(stepNum: Int, speed: Int = 4) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_55, stepNum, speed)
    }

    /**
     *
     * @param stepNum
     * @param speed
     */
    /**
     *
     * @param stepNum
     */
    @JvmOverloads
    fun atMovEW56(stepNum: Int, speed: Int = 4) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_56, stepNum, speed)
    }

    /**
     *
     * @param stepNum
     * @param speed
     */
    /**
     *
     * @param stepNum
     */
    @JvmOverloads
    fun atMovEW57(stepNum: Int, speed: Int = 4) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_57, stepNum, speed)
    }

    /**
     *
     * @param stepNum
     * @param speed
     */
    /**
     *
     * @param stepNum
     */
    @JvmOverloads
    fun atMovEW58(stepNum: Int, speed: Int = 4) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_58, stepNum, speed)
    }

    /**
     *
     * @param stepNum
     * @param speed
     */
    /**
     *
     * @param stepNum
     */
    @JvmOverloads
    fun atMovEW59(stepNum: Int, speed: Int = 4) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_59, stepNum, speed)
    }

    /**
     *
     * @param stepNum
     * @param speed
     */
    /**
     *
     * @param stepNum
     */
    @JvmOverloads
    fun atMovEW60(stepNum: Int, speed: Int = 4) {
        consumeATCommand(ATCmdConsts.AT_MOVEW_60, stepNum, speed)
    }

    fun consumeATCommand(commandType: Int, stepNum: Int, speed: Int) {
        val command = String.format("AT+MOVEW,%d,%d,%d\\r\\n", commandType, stepNum, speed)
        LogUtils.logd("McuResponseUtil", "consumeATCommand:舵机执行命令 $command")
        if (!TextUtils.isEmpty(command)) {
            writeCommand(command)
            //            ShellUtils.execCmd("at_tools " + atCom + "\\\\r\\\\n", false);
//           shellUtils(command);
        }
    }

    private fun writeCommand(command: String) {
        //如果是陀螺仪，延迟一下
        if (command.contains("AT+FiAGW")) {
            try {
                Thread.sleep(50)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        SerialAllJNI.writeData(command)
        // if (!TextUtils.isEmpty(command)) {
        //     SerialPortJNI.writePort(command.getBytes());
        // }
    }

    private fun shellUtils(command: String) {
        val result: ShellUtils.CommandResult = ShellUtils.execCmd("at_tools $command", false)
        android.util.Log.i("TAG===", "McuResponseUtil shellUtils: ====$result")
    } //    public static void consumeATCommand(String command) {
    //        LogUtils.logi("letianpai", "consumeATCommand  ======= 1");
    //        if (!TextUtils.isEmpty(command)) {
    //            LogUtils.logi("letianpai", "consumeATCommand  ======= 2");
    //            SerialPortJNI.writePort(command.getBytes());
    //        }
    //    }
}
