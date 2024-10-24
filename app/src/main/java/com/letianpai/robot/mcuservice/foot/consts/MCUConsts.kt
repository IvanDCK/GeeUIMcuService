package com.letianpai.robot.mcuservice.foot.consts

object MCUConsts {
    var AT_COMMAND_SENSOR: String = "AT+AG"
    var AT_COMMAND_READ: String = "AT+MOTORR"
    var AT_COMMAND_MOTORW: String = "AT+MOTORW"
    var AT_COMMAND_CLIFFR: String = "AT+CLIFFR" //读取悬崖传感器状态
    var AT_COMMAND_CLIFFD: String = "AT+CLIFFD" //读取悬崖传感器状态

    //    public static String AT_COMMAND_FMCGADDR ="AT+FMCGADDR"; //读取悬崖传感器状态
    //    public static String AT_COMMAND_FMCR ="AT+FMCR"; //读取悬崖传感器状态
    //    public static String AT_COMMAND_FMCW ="AT+FMCW"; //读取悬崖传感器状态
    //    public static String AT_COMMAND_FMCDUMP ="AT+FMCDUMP"; //读取悬崖传感器状态
    var AT_COMMAND_VERR: String = "AT+VerR" //读取悬崖传感器状态
    var AT_COMMAND_SNR: String = "AT+SNR" //读取悬崖传感器状态
    var AT_COMMAND_AGID: String = "AT+AGID" //读取悬崖传感器状态
    var AT_COMMAND_LEDON: String = "AT+LEDOn" //Led 开
    var AT_COMMAND_LEDOFF: String = "AT+LEDOff" //Led 关

    var AT_COMMAND_END: String = "\r\n"
    var AT_COMMAND_CONNECT: String = ","

    /**
     * 加速度
     */
    var SENSOR_TYPE_ACC: String = "0"

    /**
     * 陀螺仪
     */
    var SENSOR_TYPE_GYRO: String = "1"

    /**
     * 加速度 + 陀螺仪
     */
    var SENSOR_TYPE_AAC_GYRO: String = "2"

    /**
     * ============= 舵机 start =============
     */
    //    public static String MOTOR_NUM_1 = "1";
    //    public static String MOTOR_NUM_2 = "2";
    //    public static String MOTOR_NUM_3 = "3";
    //    public static String MOTOR_NUM_4 = "4";
    //    public static String MOTOR_NUM_5 = "5";
    //    public static String MOTOR_NUM_6 = "6";
    const val MOTOR_NUM_1: Int = 1
    const val MOTOR_NUM_2: Int = 2
    const val MOTOR_NUM_3: Int = 3
    const val MOTOR_NUM_4: Int = 4
    const val MOTOR_NUM_5: Int = 5
    const val MOTOR_NUM_6: Int = 6

    var VALUE_TYPE_ANGLE: String = "0"
    var VALUE_TYPE_PULSE: String = "1"


    // ================================================ start ===================================================
    //    public final static String  COMMAND_TYPE_FACE = "controlFace";
    //    public final static String  COMMAND_TYPE_MOTION = "controlMotion";
    //    public final static String  COMMAND_TYPE_SOUND = "controlSound";
    //    public final static String  COMMAND_TYPE_ANTENNA_MOTION = "controlAntennaMotion";
    //    public final static String  COMMAND_TYPE_ANTENNA_LIGHT = "controlAntennaLight";
    // ================================================ end ===================================================
    /**
     * ============= 舵机 end =============
     */
    var RETURN_VALUE_SUCCESS: String = "AT+RES,ACK\\r\\n"

    //    public static String RETURN_VALUE_FAILED = "AT+RES";
    /**
     * 获取传感器 AT命令
     * @param command AT命令
     * @param sensorType 传感器类型
     * @return
     */
    private fun getAGCommand(command: String, sensorType: String): String {
        return command + AT_COMMAND_CONNECT + sensorType + AT_COMMAND_END
    }

    /**
     *
     * @param motorNum 舵机编号
     * @return
     */
    private fun getMOTORRCommand(motorNum: String): String {
        return AT_COMMAND_READ + AT_COMMAND_CONNECT + motorNum + AT_COMMAND_END
    }

    /**
     *
     * @param motorNum 舵机编号
     * @param valueType
     * @param value
     * @return
     */
    private fun getMOTORWCommand(motorNum: String, valueType: String, value: String): String {
        return (AT_COMMAND_MOTORW + AT_COMMAND_CONNECT
                + motorNum + AT_COMMAND_CONNECT
                + valueType + AT_COMMAND_CONNECT
                + value + AT_COMMAND_END)
    }

    val aCCCommand: String
        /**
         * 获取传感器 AT命令
         * @return
         */
        get() = getAGCommand(
            AT_COMMAND_SENSOR,
            SENSOR_TYPE_ACC
        )

    val gYROCommand: String
        /**
         * 获取加速度 AT命令
         * @return
         */
        get() {
            return getAGCommand(
                AT_COMMAND_SENSOR,
                SENSOR_TYPE_GYRO
            )
        }

    private val aCCGYROCommand: String
        /**
         * 获取传感器加速度 AT命令
         * @return
         */
        get() {
            return getAGCommand(
                AT_COMMAND_SENSOR,
                SENSOR_TYPE_AAC_GYRO
            )
        }
}
