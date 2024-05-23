package com.letianpai;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.letianpai.sensorservice.SerialAllJNI;
import com.renhejia.robot.commandlib.consts.RobotRemoteConsts;
import com.renhejia.robot.commandlib.log.LogUtils;

import com.google.gson.Gson;
import com.renhejia.robot.commandlib.consts.ATCmdConsts;
import com.renhejia.robot.commandlib.consts.MCUCommandConsts;
import com.renhejia.robot.commandlib.parser.antennalight.AntennaLight;
import com.renhejia.robot.commandlib.parser.antennamotion.AntennaMotion;
import com.renhejia.robot.commandlib.parser.motion.Motion;
import com.renhejia.robot.commandlib.parser.power.PowerMotion;
import com.renhejia.robot.letianpaiservice.LtpCommand;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 */
public class McuCommandControlManager {

    private String TAG = "McuCommandControlManager";
    private String TAG1 = "letianpai_test_control";
    private Gson mGson;

    private static McuCommandControlManager instance;
    private Context mContext;


    private McuCommandControlManager(Context context) {
        this.mContext = context;
        init(context);
    }

    public static McuCommandControlManager getInstance(Context context) {
        synchronized (McuCommandControlManager.class) {
            if (instance == null) {
                instance = new McuCommandControlManager(context.getApplicationContext());
            }
            return instance;
        }
    }

    private void init(Context context) {
        mGson = new Gson();
    }

    public void commandDistribute(String command, String data) {
        if (command.equals(RobotRemoteConsts.COMMAND_TYPE_MOTION)) {
            LogUtils.logd(TAG, "mcu 执行动作：" + data);
            responseMotion(data);

        } else if (command.equals(RobotRemoteConsts.COMMAND_TYPE_ANTENNA_LIGHT)) {
            LogUtils.logd(TAG, "mcu 执行天线灯光：" + data);
            responseAntennaLight(data);

        } else if (command.equals(RobotRemoteConsts.COMMAND_TYPE_ANTENNA_MOTION)) {
            LogUtils.logd(TAG, "mcu 执行天线：" + data);
            responseAntennaMotion(data);
        } else if (command.equals(MCUCommandConsts.COMMAND_TYPE_POWER_CONTROL)) {
            LogUtils.logd("letianpai_test_control", "mcu COMMAND_TYPE_POWER_CONTROL：" + data);
            Log.e(TAG, "mcu command：" + command);
            responsePowerControl(data);
        } else if (command.equals(MCUCommandConsts.COMMAND_TYPE_RESET_MCU)) {
            LogUtils.logd("letianpai_test_control", "RESET_MCU：" + data);
            resetMcu();

        } else if (command.equals(MCUCommandConsts.COMMAND_TYPE_START_GYROSCOPE)) {
            LogUtils.logd("letianpai_test_control", "start_gyroscope：" + data);
            startGyroscope();

        } else if (command.equals(MCUCommandConsts.COMMAND_TYPE_STOP_GYROSCOPE)) {
            LogUtils.logd("letianpai_test_control", "stop_gyroscope：" + data);
            stopGyroscope();
        } else if (command.equals(MCUCommandConsts.COMMAND_TYPE_GYROSCOPE)) {
            controlGyroscope(data);
        }
    }

    private void controlGyroscope(String data) {
        Log.i(TAG, "McuCommandControlManager controlGyroscope: 控制陀螺仪:" + data);
//"{\n" +
//        "    \"cmd_value\":\"AT+FiAGW,2,10\",\n" +
//        "    \"update_time\":1693323185\n" +
//        "}"
        try {
            JSONObject jo = new JSONObject(data);
            String mcuData = jo.optString("cmd_value");
            if (mcuData != null) {
                consumeATCommand(mcuData + "\\r\\n");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void stopGyroscope() {
        String command = String.format("AT+FiAGW,1\\r\\n");
        LogUtils.logi(TAG1, "关闭陀螺仪: " + command);
        consumeATCommand(command);
    }

    private void startGyroscope() {
        String command = String.format("AT+FiAGW,0\\r\\n");
        LogUtils.logi(TAG1, "启动陀螺仪 " + command);
        consumeATCommand(command);
    }

    private void resetMcu() {
        String command = String.format("AT+Reset\\r\\n");
        LogUtils.logi(TAG1, "MCU 关闭功能: " + command);
        consumeATCommand(command);

    }

    private void responsePowerControl(String data) {
        PowerMotion powerMotion = mGson.fromJson(data, PowerMotion.class);
        if (powerMotion == null || powerMotion.getFunction() == 0) {
            return;
        }
        functionControl(powerMotion.getFunction(), powerMotion.getStatus());

    }

    private void responseAntennaMotion(String data) {
        AntennaMotion antennaMotion = mGson.fromJson(data, AntennaMotion.class);
        if (antennaMotion == null) {
            return;
        }
        LogUtils.logd("McuCommandControlManager", "responseAntennaMotion: gson data:" + antennaMotion.toString());
        turnAntennaMotion(antennaMotion.getCmd(), antennaMotion.getStep(), antennaMotion.getSpeed(), antennaMotion.getAngle());
    }


    private void responseAntennaLight(String data) {
        AntennaLight light = mGson.fromJson(data, AntennaLight.class);
        if (light == null) {
            return;
        }
        String lightCommand = light.getAntenna_light();
        int color = light.getAntenna_light_color();

        if (lightCommand.equals(MCUCommandConsts.COMMAND_TYPE_ANTENNA_LIGHT_VALUE_ON)) {
            //TODO
            light(color);
        } else if (lightCommand.equals(MCUCommandConsts.COMMAND_TYPE_ANTENNA_LIGHT_VALUE_OFF)) {
            lightOff();
        }

    }

    private void responseMotion(String data) {
        LogUtils.logd(TAG, "responseMotion: " + data);
        Motion motion = mGson.fromJson(data, Motion.class);
        String motionType;
        LogUtils.logd("McuCommandControlManager", "responseMotion: gson data:" + motion);

        if (motion != null) {
            motionType = motion.getMotion();
            if (TextUtils.isEmpty(motionType)) {
                return;
            }
            LogUtils.logd(TAG, "responseMotion: " + motionType);

            if (motionType == null || motionType.equals("null") || motion.equals("")) {
                int stepNum = 1;
                if (motion.getStepNum() > 0) {
                    stepNum = motion.getStepNum();
                }
                int speed = 3;
                if (motion.getSpeed() != 0) {
                    speed = motion.getSpeed();
                }
                LogUtils.logd(TAG, "responseMotion: if==" + motion);
                McuResponseUtil.consumeATCommand(motion.getNumber(), stepNum, speed);
            } else {
                int stepNum = motion.getStepNum();
                if (stepNum == 0) {
                    stepNum = 1;
                }
                LogUtils.logd(TAG, "responseMotion: else==" + motion);
                switch (motionType) {
                    case MCUCommandConsts.COMMAND_VALUE_MOTION_FORWARD:
                    case ATCmdConsts.AT_STR_MOVEW_FORWARD:
                        McuResponseUtil.walkForward(stepNum);
                        break;
                    case MCUCommandConsts.COMMAND_VALUE_MOTION_BACKEND:

                    case ATCmdConsts.AT_STR_MOVEW_BACK:
                        McuResponseUtil.walkBackend(stepNum);
                        break;
                    case MCUCommandConsts.COMMAND_VALUE_MOTION_LEFT:

                    case ATCmdConsts.AT_STR_MOVEW_CRAB_STEP_LEFT:
                        McuResponseUtil.crabStepLeft(stepNum);

                        break;
                    case MCUCommandConsts.COMMAND_VALUE_MOTION_RIGHT:

                    case ATCmdConsts.AT_STR_MOVEW_CRAB_STEP_RIGHT:
                        //                walkRight(number);
                        McuResponseUtil.crabStepRight(stepNum);

                        break;
                    case MCUCommandConsts.COMMAND_VALUE_MOTION_LEFT_ROUND:
                    case ATCmdConsts.AT_STR_MOVEW_LOCAL_ROUND_LEFT:
                        LogUtils.logd("McuCommandControlManager", "responseMotion:COMMAND_VALUE_MOTION_LEFT " + stepNum);
                        McuResponseUtil.localRoundLeft(stepNum);

                        break;
                    case MCUCommandConsts.COMMAND_VALUE_MOTION_RIGHT_ROUND:
                    case ATCmdConsts.AT_STR_MOVEW_TURN_RIGHT:
                    case ATCmdConsts.AT_STR_MOVEW_LOCAL_ROUND_RIGHT:
                        McuResponseUtil.localRoundRight(stepNum);

                        break;
                    case MCUCommandConsts.COMMAND_VALUE_MOTION_SET_STRAIGHT:
                    case ATCmdConsts.AT_STR_MOVEW_STAND:
                        McuResponseUtil.walkStand();
                        //TODO 增加回正逻辑
                        break;

                    case ATCmdConsts.AT_STR_MOVEW_TURN_LEFT:
                        McuResponseUtil.turnLeft(stepNum);
                        break;

                    case ATCmdConsts.AT_STR_MOVEW_SHAKE_LEFT_LEG:
                    case ATCmdConsts.AT_STR_MOVEW_SHAKE_LEFT_LEG1:
                        McuResponseUtil.shakeLeftLeg(stepNum);

                        break;
                    case ATCmdConsts.AT_STR_MOVEW_SHAKE_RIGHT_LEG:
                    case ATCmdConsts.AT_STR_MOVEW_SHAKE_RIGHT_LEG1:
                        McuResponseUtil.shakeRightLeg(stepNum);

                        break;
                    case ATCmdConsts.AT_STR_MOVEW_SHAKE_LEFT_FOOT:
                    case ATCmdConsts.AT_STR_MOVEW_SHAKE_LEFT_FOOT1:
                        McuResponseUtil.shakeLeftFoot(stepNum);

                        break;
                    case ATCmdConsts.AT_STR_MOVEW_SHAKE_RIGHT_FOOT:
                    case ATCmdConsts.AT_STR_MOVEW_SHAKE_RIGHT_FOOT1:
                        McuResponseUtil.shakeRightFoot(stepNum);

                        break;
                    case ATCmdConsts.AT_STR_MOVEW_SHAKE_CROSS_LEFT_FOOT:
                    case ATCmdConsts.AT_STR_MOVEW_SHAKE_CROSS_LEFT_FOOT1:
                        McuResponseUtil.shakeCrossLeftFoot(stepNum);

                        break;
                    case ATCmdConsts.AT_STR_MOVEW_SHAKE_CROSS_RIGHT_FOOT:
                    case ATCmdConsts.AT_STR_MOVEW_SHAKE_CROSS_RIGHT_FOOT1:
                        McuResponseUtil.shakeCrossRightFoot(stepNum);

                        break;
                    case ATCmdConsts.AT_STR_MOVEW_SHAKE_LEFT_LEANING:
                    case ATCmdConsts.AT_STR_MOVEW_SHAKE_LEFT_LEANING1:
                        McuResponseUtil.shakeLeftLeaning(stepNum);

                        break;
                    case ATCmdConsts.AT_STR_MOVEW_SHAKE_RIGHT_LEANING:
                    case ATCmdConsts.AT_STR_MOVEW_SHAKE_RIGHT_LEANING1:
                        McuResponseUtil.shakeRightLeaning(stepNum);

                        break;
                    case ATCmdConsts.AT_STR_MOVEW_STAMP_LEFT_FOOT:
                    case ATCmdConsts.AT_STR_MOVEW_STAMP_LEFT_FOOT1:
                        McuResponseUtil.stampLeftFoot(stepNum);

                        break;
                    case ATCmdConsts.AT_STR_MOVEW_STAMP_RIGHT_FOOT:
                    case ATCmdConsts.AT_STR_MOVEW_STAMP_RIGHT_FOOT1:
                        McuResponseUtil.stampRightFoot(stepNum);

                        break;
                    case ATCmdConsts.AT_STR_MOVEW_SHAKE_SWAYING_UP_AND_DOWN:
                    case ATCmdConsts.AT_STR_MOVEW_SHAKE_SWAYING_UP_AND_DOWN1:
                        McuResponseUtil.swayingUpdAndDown(stepNum);

                        break;
                    case ATCmdConsts.AT_STR_MOVEW_SHAKE_SWINGS_FROM_SIDE_TO_SIDE:
                    case ATCmdConsts.AT_STR_MOVEW_SHAKE_SWINGS_FROM_SIDE_TO_SIDE1:
                        McuResponseUtil.swingsFromSideToSide(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_SHAKE_HEAD:
                    case ATCmdConsts.AT_STR_MOVEW_SHAKE_HEAD1:
                        McuResponseUtil.shakeHead(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_STAND_AT_EASE:
                    case ATCmdConsts.AT_STR_MOVEW_STAND_AT_EASE1:
                    case ATCmdConsts.AT_STR_MOVEW_SHAKE_LEG:
                    case ATCmdConsts.AT_STR_MOVEW_FEET_TREMOR:
                        McuResponseUtil.standAtEase(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_MICROTREMOR:
                        McuResponseUtil.microTremor(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_29:
                        McuResponseUtil.atMovEW29(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_30:
                        McuResponseUtil.atMovEW30(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_31:
                        McuResponseUtil.atMovEW31(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_32:
                        McuResponseUtil.atMovEW32(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_33:
                        McuResponseUtil.atMovEW33(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_34:
                        McuResponseUtil.atMovEW34(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_35:
                        McuResponseUtil.atMovEW35(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_36:
                        McuResponseUtil.atMovEW36(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_37:
                        McuResponseUtil.atMovEW37(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_38:
                        McuResponseUtil.atMovEW38(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_39:
                        McuResponseUtil.atMovEW39(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_40:
                        McuResponseUtil.atMovEW40(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_41:
                        McuResponseUtil.atMovEW41(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_42:
                        McuResponseUtil.atMovEW42(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_43:
                        McuResponseUtil.atMovEW43(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_44:
                        McuResponseUtil.atMovEW44(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_45:
                        McuResponseUtil.atMovEW45(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_46:
                        McuResponseUtil.atMovEW46(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_47:
                        McuResponseUtil.atMovEW47(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_48:
                        McuResponseUtil.atMovEW48(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_49:
                        McuResponseUtil.atMovEW49(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_50:
                        McuResponseUtil.atMovEW50(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_51:
                        McuResponseUtil.atMovEW51(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_52:
                        McuResponseUtil.atMovEW52(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_53:
                        McuResponseUtil.atMovEW53(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_54:
                        McuResponseUtil.atMovEW54(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_55:
                        McuResponseUtil.atMovEW55(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_56:
                        McuResponseUtil.atMovEW56(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_57:
                        McuResponseUtil.atMovEW57(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_58:
                        McuResponseUtil.atMovEW58(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_59:
                        McuResponseUtil.atMovEW59(stepNum);
                        break;
                    case ATCmdConsts.AT_STR_MOVEW_60:
                        McuResponseUtil.atMovEW60(stepNum);
                        break;
                    default:
                        McuResponseUtil.consumeATCommand(motion.getNumber(), stepNum, 3);
                }
            }
        }
        //walkBackend(2);

    }


    public void consumeATCommand(String command) {
        LogUtils.logi(TAG, "consumeATCommand --" + command);
        LogUtils.logi(TAG1, "mcu_responsePowerControl_5" + command);

        if (!TextUtils.isEmpty(command)) {
            writeCommand(command);
        }
    }

    private void writeCommand(String command) {
        SerialAllJNI.INSTANCE.writeData(command);
    }

    private void walkForward(int stepNum) {
        walks(1, stepNum);
    }

    private void walkBackend(int stepNum) {
        walks(2, stepNum);
    }

    private void walkLeft(int stepNum) {
        walks(3, stepNum);
    }

    private void walkRight(int stepNum) {
        walks(4, stepNum);
    }

    private void walks(int moveCommand, int stepNum) {
        String command = String.format("AT+MOVEW,%d,%d,2\\r\\n", moveCommand, stepNum);
        LogUtils.logi("letianpai_walks", "command: " + command);
        consumeATCommand(command);
    }

    private void turnEarLeftRound() {
        consumeATCommand("AT+EARW,1,2,2\\r\\n");
    }

    private void turnEarRightRound() {
        consumeATCommand("AT+EARW,2,2,2\\r\\n");
    }

    public void light(int lightColor) {

        String command = String.format("AT+LEDOn,%d\\r\\n", lightColor);
        LogUtils.logi("letianpai_walks", "command: " + command);
        consumeATCommand(command);
    }

    public void lightOff() {
        String command = String.format("AT+LEDOff\\r\\n");
        LogUtils.logi("letianpai_walks", "command: " + command);
        consumeATCommand(command);
    }

    public void turnAntennaMotion(int cmd, int step, int speed, int angle) {
        if (step == 0) {
            step = 1;
        }
        if (speed == 0) {
            speed = 300;
        }
        if (angle == 0) {
            angle = 90;
        }
        String command = String.format("AT+EARW,%d,%d,%d,%d\\r\\n", cmd, step, speed, angle);
        LogUtils.logi("letianpai_ear", "turnAntennaMotion_command: " + command);
        consumeATCommand(command);
    }

    /**
     *   1--环境光任务
     *   2--触摸任务（暂不支持）
     *   3--腿和脚舵机电源
     *   4--耳朵灯（只支持关）
     *   5--悬崖和悬空检测任务
     *   6--陀螺仪数据采集与欧拉角算法任务
     * status：
     *   0--关闭
     *   1--开启
     * 举例：
     */

    /**
     * @param func   功能：
     * @param status
     */
    public void functionControl(int func, int status) {
        LogUtils.logd("McuCommandControlManager", "functionControl: func: " + func + " status:  " + status);
        String command = String.format("AT+FunCtr,%d,%d\\r\\n", func, status);
        LogUtils.logi(TAG1, "MCU 关闭功能: " + command);
        consumeATCommand(command);
    }

}
