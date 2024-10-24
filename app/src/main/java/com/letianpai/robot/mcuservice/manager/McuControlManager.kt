package com.letianpai.robot.mcuservice.manager

import android.content.Context
import android.text.TextUtils
import android.widget.Toast
import com.letianpai.robot.mcuservice.foot.MCUWalkConsts
import com.letianpai.robot.mcuservice.foot.Servo
import com.letianpai.robot.mcuservice.foot.consts.MCUConsts
import com.letianpai.robot.mcuservice.utils.ConvertUtils
import kotlin.math.abs

/**
 *
 */
class McuControlManager private constructor(private val mContext: Context) {
    private val entryValues: Array<String> = emptyArray()
    private val isOpen = false

    private var leftLeg: Servo? = null
    private var rightLeg: Servo? = null
    private var leftFoot: Servo? = null
    private var rightFoot: Servo? = null
    private var leftEar: Servo? = null
    private var rightEar: Servo? = null


    init {
        init(mContext)
    }

    private fun init(context: Context) {
        initSerialPortHelper()
        initServo()
    }

    private fun initServo() {
        rightLeg = Servo(MCUConsts.MOTOR_NUM_4, false)
        rightFoot = Servo(MCUConsts.MOTOR_NUM_2, false)
        leftLeg = Servo(MCUConsts.MOTOR_NUM_3, false)
        leftFoot = Servo(MCUConsts.MOTOR_NUM_1, false)
        leftEar = Servo(MCUConsts.MOTOR_NUM_6, false)
        rightEar = Servo(MCUConsts.MOTOR_NUM_5, false)
    }

    private fun initSerialPortHelper() {
    }

    fun sendCommand(command: String) {
        if (TextUtils.isEmpty(command)) {
            Toast.makeText(mContext, "Please enter a command that is not empty！", Toast.LENGTH_LONG).show()
            return
        }

        val hexCommand = ConvertUtils.convertToHexString(command)
    }

    fun run(totalSteps: Int, rate: Float) {
        for (i in 0 until totalSteps) {
            bigFootWalk("", i, totalSteps, rate)
        }
    }

    /**
     * @param pos        方向
     * @param curStep
     * @param totalSteps
     * @param rate       // 50- 100
     */
    fun bigFootWalk(pos: String, curStep: Int, totalSteps: Int, rate: Float) {
        // to be confirmed
        val angleLeftLeg = 10
        val angleRightLeg = 10
        var angleLeftFoot = 0 // TODO 需要确认
        var angleRightFoot = 0 // TODO 需要确认

        if (pos == MCUWalkConsts.DIRECTION_BACK) {
            angleLeftFoot = 30
            angleRightFoot = 30
        } else if (pos == MCUWalkConsts.DIRECTION_BACK_LEFT) {
            angleLeftFoot = -5
            angleRightFoot = -25
        } else if (pos == MCUWalkConsts.DIRECTION_BACK_RIGHT) {
            angleLeftFoot = -25
            angleRightFoot = -5
        } else if (pos == MCUWalkConsts.DIRECTION_RIGHT) {
            angleLeftFoot = 5
            angleRightFoot = 25
        } else if (pos == MCUWalkConsts.DIRECTION_LEFT) {
            angleLeftFoot = 25
            angleRightFoot = 5
        } else {
            angleLeftFoot = -25
            angleRightFoot = -25
        }
        //TODO 功能未外完整
//        Servo[] servoList1 = [leftLeg,rightLeg];
        val servoList1 = arrayOfNulls<Servo>(2)
        servoList1[0] = leftLeg
        servoList1[1] = rightLeg

        val angleList = FloatArray(2)
        angleList[0] = -(angleLeftLeg).toFloat()
        angleList[1] = -angleRightLeg.toFloat()

        //        multiRelateAngleAction([leftLeg,rightLeg],[-(angleLeftLeg),-angleRightLeg],rate);
//        multiRelateAngleAction(servoList1,[-angleLeftLeg,-angleRightLeg],rate);
        multiRelateAngleAction(servoList1, angleList, rate)
        val angleLeftFoot1: Int
        val angleRightFoot1: Int
        val angleLeftFootBack: Int
        val angleRightFootBack: Int

        if (curStep == 0) {
            angleLeftFoot1 = angleLeftFoot
            angleRightFoot1 = angleRightFoot
        } else {
            angleLeftFoot1 = 2 * angleLeftFoot
            angleRightFoot1 = 2 * angleRightFoot
        }

        val servoList2 = arrayOfNulls<Servo>(2)
        servoList2[0] = leftFoot
        servoList2[1] = rightFoot

        val angleList2 = FloatArray(2)
        angleList2[0] = angleLeftFoot1.toFloat()
        angleList2[1] = angleRightFoot1.toFloat()

        multiRelateAngleAction(servoList2, angleList2, rate)


        val servoList3 = arrayOfNulls<Servo>(2)
        servoList3[0] = leftLeg
        servoList3[1] = rightLeg

        val angleList3 = FloatArray(2)
        angleList3[0] = angleLeftLeg.toFloat()
        angleList3[1] = angleRightLeg.toFloat()
        //  # 左腿向内30度回正，右腿向外30度回正
        multiRelateAngleAction(servoList3, angleList3, rate)
        //   # 左腿向内30度，右腿向外30度
        multiRelateAngleAction(servoList3, angleList3, rate)

        if (curStep == totalSteps - 1) {
            angleLeftFootBack = angleLeftFoot
            angleRightFootBack = angleRightFoot
        } else {
            angleLeftFootBack = 2 * angleLeftFoot
            angleRightFootBack = 2 * angleRightFoot
        }

        val servoList4 = arrayOfNulls<Servo>(2)
        servoList4[0] = leftFoot
        servoList4[1] = rightFoot

        val angleList4 = FloatArray(2)
        angleList4[0] = -angleLeftFootBack.toFloat()
        angleList4[1] = -angleRightFootBack.toFloat()

        multiRelateAngleAction(servoList4, angleList4, rate)

        val servoList5 = arrayOfNulls<Servo>(2)
        servoList5[0] = leftLeg
        servoList5[1] = rightLeg

        val angleList5 = FloatArray(2)
        angleList5[0] = -angleLeftLeg.toFloat()
        angleList5[1] = -(angleRightLeg).toFloat()

        multiRelateAngleAction(servoList5, angleList5, rate)
    }

    //    /**
    //     *
    //     * @param pos 方向
    //     * @param curStep
    //     * @param totalSteps
    //     * @param rate // 50- 100
    //     */
    //    public void bigFootWalk(String pos,int curStep,int totalSteps,float rate){
    //        // to be confirmed
    //        int angleLeftLeg = 10;
    //        int angleRightLeg = 10;
    //        int angleLeftFoot = 0;  // TODO 需要确认
    //        int angleRightFoot = 0; // TODO 需要确认
    //
    //        if (pos.equals(MCUWalkConsts.DIRECTION_BACK)){
    //            angleLeftFoot = 30;
    //            angleRightFoot = 30;
    //        }else if(pos.equals(MCUWalkConsts.DIRECTION_BACK_LEFT)){
    //            angleLeftFoot = -5;
    //            angleRightFoot = -25;
    //        }else if(pos.equals(MCUWalkConsts.DIRECTION_BACK_RIGHT)){
    //            angleLeftFoot = -25;
    //            angleRightFoot = -5;
    //        }else if(pos.equals(MCUWalkConsts.DIRECTION_RIGHT)){
    //            angleLeftFoot = 5;
    //            angleRightFoot = 25;
    //        }else if(pos.equals(MCUWalkConsts.DIRECTION_LEFT)){
    //            angleLeftFoot = 25;
    //            angleRightFoot = 5;
    //        }else{
    //            angleLeftFoot = -25;
    //            angleRightFoot = -25;
    //        }
    //        //TODO 功能未外完整
    ////        Servo[] servoList1 = [leftLeg,rightLeg];
    //        Servo[] servoList1 = new Servo[2];
    //        servoList1[0]= leftLeg;
    //        servoList1[1]= rightLeg;
    //
    //        float[] angleList = new float[2];
    //        angleList[0] = -(angleLeftLeg);
    //        angleList[1] = -angleRightLeg;
    //
    ////        multiRelateAngleAction([leftLeg,rightLeg],[-(angleLeftLeg),-angleRightLeg],rate);
    ////        multiRelateAngleAction(servoList1,[-angleLeftLeg,-angleRightLeg],rate);
    //        multiRelateAngleAction(servoList1,angleList,rate);
    //        int angleLeftFoot1;
    //        int angleRightFoot1;
    //        int angleLeftFootBack;
    //        int angleRightFootBack;
    //
    //        if(curStep == 0){
    //            angleLeftFoot1 = angleLeftFoot;
    //            angleRightFoot1 = angleRightFoot;
    //        }else{
    //            angleLeftFoot1 = 2 * angleLeftFoot;
    //            angleRightFoot1 = 2 * angleRightFoot;
    //        }
    //
    //        Servo[] servoList2 = new Servo[2];
    //        servoList2[0]= leftFoot;
    //        servoList2[1]= rightFoot;
    //
    //        float[] angleList2 = new float[2];
    //        angleList2[0] = angleLeftFoot1;
    //        angleList2[1] = angleRightFoot1;
    //
    //        multiRelateAngleAction(servoList2,angleList2,rate);
    //
    //
    //        Servo[] servoList3 = new Servo[2];
    //        servoList3[0]= leftLeg;
    //        servoList3[1]= rightLeg;
    //
    //        float[] angleList3 = new float[2];
    //        angleList3[0] = angleLeftLeg;
    //        angleList3[1] = angleRightLeg;
    //        //  # 左腿向内30度回正，右腿向外30度回正
    //        multiRelateAngleAction(servoList3,angleList3,rate);
    //        //   # 左腿向内30度，右腿向外30度
    //        multiRelateAngleAction(servoList3,angleList3,rate);
    //
    //        if(curStep == totalSteps -1){
    //            angleLeftFootBack = angleLeftFoot;
    //            angleRightFootBack = angleRightFoot;
    //        }else{
    //            angleLeftFootBack = 2 * angleLeftFoot;
    //            angleRightFootBack = 2 * angleRightFoot;
    //        }
    //
    //        Servo[] servoList4 = new Servo[2];
    //        servoList4[0]= leftFoot;
    //        servoList4[1]= rightFoot;
    //
    //        float[] angleList4 = new float[2];
    //        angleList4[0] = -angleLeftFootBack;
    //        angleList4[1] = -angleRightFootBack;
    //
    //        multiRelateAngleAction(servoList4,angleList4,rate);
    //
    //        Servo[] servoList5 = new Servo[2];
    //        servoList5[0]= leftLeg;
    //        servoList5[1]= rightLeg;
    //
    //        float[] angleList5 = new float[2];
    //        angleList5[0] = -angleLeftLeg;
    //        angleList5[1] = -(angleRightLeg);
    //
    //        multiRelateAngleAction(servoList5,angleList5,rate);
    //
    //
    //
    //    }
    fun multiRelateAngleAction(servoList: Array<Servo?>, angleList: FloatArray, rate: Float) {
        if (servoList.size != angleList.size) {
            return
        }
        if (servoList.size == 0) {
            return
        }

        //
//        float [] startMarginList = null;
//        float [] endMarginList = null;
//        float [] rateList = null;
        var maxAngle = 0f
        var maxIndex = 0


        val startMarginList = FloatArray(servoList.size)
        val endMarginList = FloatArray(servoList.size)
        val rateList = FloatArray(servoList.size)

        val absAngleList = FloatArray(angleList.size)

        for (i in servoList.indices) {
            startMarginList[i] = servoList[i]?.margin!!
            endMarginList[i] =
                servoList[i]?.margin!! + angleList[i] * Servo.marginPerAngle
        }

        for (j in angleList.indices) {
            if (abs(angleList[j].toDouble()) > maxAngle) {
                maxAngle = abs(angleList[j].toDouble()).toFloat()
                maxIndex = j
            }
        }
        for (i in servoList.indices) {
            var adjustRate = (rate * (abs(
                angleList[i].toDouble()
            ) / abs((maxAngle).toDouble()))).toFloat()
            if (angleList[i] < 0) {
                adjustRate = -adjustRate
            }
            rateList[i] = adjustRate
        }
        val rangeList = getFloatRangeList(
            startMarginList[maxIndex],
            endMarginList[maxIndex], rateList[maxIndex]
        )

        //TODO
        for (i in rangeList.indices) {
            for (j in servoList.indices) {
                servoList[j]!!.moveMargin(startMarginList[j] + (i + 1) * rateList[j])
            }
        }
    }

    fun getFloatRangeList(start: Float, stop: Float, steps: Float): ArrayList<Float> {
        var start = start
        val resultList = ArrayList<Float>()
        if (steps < 0) {
            while (stop < start) {
                resultList.add(start)
                start = start + steps
            }
        } else {
            while (start < stop) {
                resultList.add(start)
                start = start + steps
            }
        }
        return resultList
    }

    //    //舵机校准
    //    public void allTurnAngle(int angle, float[] offsetList){
    //        if(offsetList.length == 4){
    ////            leftLegOffset = offsetList[0];
    ////            rightLegOffset = offsetList[1];
    ////            leftFootOffset = offsetList[2];
    ////            rightFootOffset = offsetList[3];
    //
    //            leftLeg.moveAbsAngle((int)(angle + offsetList[0]));
    //            rightLeg.moveAbsAngle((int)(angle + offsetList[1]));
    //            leftFoot.moveAbsAngle((int)(angle + offsetList[2]));
    //            rightFoot.moveAbsAngle((int)(angle + offsetList[3]));
    //        }
    //    }
    //舵机校准
    fun allTurnAngle(angle: Int, offsetList: ArrayList<Int>) {
        if (offsetList.size == 4) {
            leftLeg!!.moveAbsAngle((angle + offsetList[0]))
            rightLeg!!.moveAbsAngle((angle + offsetList[1]))
            leftFoot!!.moveAbsAngle((angle + offsetList[2]))
            rightFoot!!.moveAbsAngle((angle + offsetList[3]))
        }
    } //    调用：校准四个舵机到归零状态
    //            offsetList = (0, -16, -6, 7)
    //    allTurnAngle(90, offsetList)


    companion object {
        private var instance: McuControlManager? = null
        fun getInstance(context: Context): McuControlManager {
            synchronized(McuControlManager::class.java) {
                if (instance == null) {
                    instance = McuControlManager(context.applicationContext)
                }
                return instance!!
            }
        }
    }
}
