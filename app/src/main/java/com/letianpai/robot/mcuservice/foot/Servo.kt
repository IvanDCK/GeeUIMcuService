package com.letianpai.robot.mcuservice.foot

import com.letianpai.robot.mcuservice.manager.ATCommandControler
import top.keepempty.sph.library.LogUtil

class Servo(servoNum: Int, reverse: Boolean) {
    private val minAngle = 0
    private val pwmScope = 2000
    private val pwmMinPulse = 0.5f // 0度，0.5ms
    private val pwmMaxPulse = 2.5f // 180度，2.5ms
    private val pwmPeriod = 20 //  一个周期20ms

    // default
    var margin: Float = 1500f // 90度
        private set
    private var reverse = false
    private var servoNum = -1 //（1-6）

    init {
        this.servoNum = servoNum
        this.reverse = reverse
    }

    /**
     * 根据角度计算margin
     * @param angle
     * @return
     */
    private fun getMarginByAngle(angle: Int): Float {
        var angle = angle
        if ((angle < minAngle) || (angle > maxAngle)) {
            return (-1).toFloat()
        }
        if (reverse) {
            angle = maxAngle - angle
        }
        return ((angle / maxAngle * (pwmMaxPulse - pwmMinPulse) + pwmMinPulse) * pwmScope) / pwmPeriod
    }


    /**
     * 根据绝对margin计算绝对角度
     */
    private fun getAngleByMargin(margin: Float): Float {
        if ((margin < marginAngle0) || (margin > marginAngle180)) {
            return (-1).toFloat()
        }
        var angle =
            (margin / pwmScope * pwmPeriod - pwmMinPulse) / (pwmMaxPulse - pwmMinPulse) * maxAngle
        if (angle < minAngle) {
            angle = minAngle.toFloat()
        } else if (angle > maxAngle) {
            angle = maxAngle.toFloat()
        }
        if (reverse) {
            angle = maxAngle - angle
        }
        return angle
    }

    /**
     * 根据角度计算margin
     */
    fun moveMargin(margin: Float) {
        var margin = margin
        if (margin < marginAngle0) {
            margin = marginAngle0.toFloat()
        } else if (margin > marginAngle180) {
            margin = marginAngle180.toFloat()
        } else {
            this.margin = margin
        }
        if (reverse) {
            this.margin = marginAngle180 - margin
        }
        sendATCommand(margin)
    }

    /**
     * 发送AT命令
     * @param margin
     */
    private fun sendATCommand(margin: Float) {
        //callAtCommand("AT+MOTORW," + str(self.servoNum) + ",1," + str(int(margin)) )
        LogUtil.e("moveAbsAngle_margin_2: $margin")
        LogUtil.e("moveAbsAngle_margin_3: $margin")
        val command = "AT+MOTORW," + servoNum + ",1," + margin.toInt() + "\\r\\n"
        //        SerialPortJNI.writePort(command.getBytes());
        ATCommandControler.instance?.addATCommand(command)
    }

    /**
     * 移动指绝对角度
     * @param angle
     */
    fun moveAbsAngle(angle: Int) {
        margin = (marginAngle0 + angle * marginPerAngle).toFloat()
        LogUtil.e("moveAbsAngle_margin_1: $margin")
        moveMargin(margin)
    }


    companion object {
        // 0度
        private const val marginAngle0 = 500

        // 90度
        private const val marginAngle90 = 1500

        // 180度
        private const val marginAngle180 = 2500
        private const val maxAngle = 180
        val marginPerAngle: Int
            get() = (marginAngle180 - marginAngle0) / maxAngle
    }
}
