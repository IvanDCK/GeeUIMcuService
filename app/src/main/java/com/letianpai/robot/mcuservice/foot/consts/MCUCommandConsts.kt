package com.letianpai.robot.mcuservice.foot.consts

/**
 * @author liujunbin
 */
object MCUCommandConsts {
    /**
     * ======================================== 命令类型 ========================================
     */
    /**
     * 表情
     */
    const val COMMAND_TYPE_FACE: String = "controlFace"

    /**
     * 动作控制
     */
    const val COMMAND_TYPE_MOTION: String = "controlMotion"

    /**
     * 声音控制
     */
    const val COMMAND_TYPE_SOUND: String = "controlSound"

    /**
     * 天线控制
     */
    const val COMMAND_TYPE_ANTENNA_MOTION: String = "controlAntennaMotion"

    /**
     * 天线光控制
     */
    const val COMMAND_TYPE_ANTENNA_LIGHT: String = "controlAntennaLight"

    /**
     * 天线光控制
     */
    const val COMMAND_TYPE_TRTC: String = "trtc"

    /**
     *
     */
    const val COMMAND_VALUE_ANTENNA_MOTION: String = "turn"

    /**
     * ======================================== 声音类型 ========================================
     */
    /**
     * 失落
     */
    const val COMMAND_VALUE_SOUND_LOSE: String = "lose"

    /**
     * 生气
     */
    const val COMMAND_VALUE_SOUND_ANGRY: String = "angry"

    /**
     * 搞怪
     */
    const val COMMAND_VALUE_SOUND_FUNNY: String = "funny"

    /**
     * 愤怒
     */
    const val COMMAND_VALUE_SOUND_ANGER: String = "anger"

    /**
     * 哭泣
     */
    const val COMMAND_VALUE_SOUND_CRY: String = "cry"

    /**
     * 撒娇
     */
    const val COMMAND_VALUE_SOUND_SPOILED: String = "spoiledChild"

    /**
     * 开心
     */
    const val COMMAND_VALUE_SOUND_HAPPY: String = "happy"

    /**
     * 苦笑
     */
    const val COMMAND_VALUE_SOUND_WRY_SMILE: String = "wrySmile"

    /**
     * 伤心
     */
    const val COMMAND_VALUE_SOUND_SAD: String = "sad"

    /**
     * ======================================== 表情类型 ========================================
     */
    /**
     * 苦笑
     */
    const val COMMAND_VALUE_FACE_WRY_SMILE: String = "wrySmile"

    /**
     * 生气
     */
    const val COMMAND_VALUE_FACE_ANGRY: String = "angry"

    /**
     * 伤心
     */
    const val COMMAND_VALUE_FACE_SAD: String = "sad"

    /**
     * 愤怒
     */
    const val COMMAND_VALUE_FACE_ANGER: String = "anger"

    /**
     * 无聊
     */
    const val COMMAND_VALUE_FACE_BORED: String = "bored"

    /**
     * 兴奋
     */
    const val COMMAND_VALUE_FACE_EXCITING: String = "exciting"

    /**
     * 哭泣
     */
    const val COMMAND_VALUE_FACE_CRY: String = "cry"

    /**
     * 失落
     */
    const val COMMAND_VALUE_FACE_LOSE: String = "lose"

    /**
     * 高兴
     */
    const val COMMAND_VALUE_FACE_HAPPY: String = "happy"

    /**
     * ======================================== 动作类型 ========================================
     */
    /**
     * 向前
     */
    const val COMMAND_VALUE_MOTION_FORWARD: String = "forward"

    /**
     * 向后
     */
    const val COMMAND_VALUE_MOTION_BACKEND: String = "backend"

    /**
     * 向左
     */
    const val COMMAND_VALUE_MOTION_LEFT: String = "left"

    /**
     * 向右
     */
    const val COMMAND_VALUE_MOTION_RIGHT: String = "right"

    /**
     * 向左转
     */
    const val COMMAND_VALUE_MOTION_LEFT_ROUND: String = "leftRound"

    /**
     * 向右转
     */
    const val COMMAND_VALUE_MOTION_RIGHT_ROUND: String = "rightRound"

    /**
     * 回正
     */
    const val COMMAND_VALUE_MOTION_SET_STRAIGHT: String = "setStraight"

    /**
     * 稍息
     */
    const val COMMAND_VALUE_MOTION_TAKE_EASY: String = "takeEasy"

    /**
     * 转圈
     */
    const val COMMAND_VALUE_MOTION_TURN_ROUND: String = "turnRound"

    /**
     * 撒娇
     */
    const val COMMAND_VALUE_MOTION_PETTISH: String = "pettish"

    /**
     * 生气
     */
    const val COMMAND_VALUE_MOTION_ANGRY: String = "angry"

    /**
     * 奔跑
     */
    const val COMMAND_VALUE_MOTION_RUN: String = "run"

    /**
     * 奔跑
     */
    const val COMMAND_VALUE_MOTION_CHEERS: String = "cheers"

    /**
     * 疲惫
     */
    const val COMMAND_VALUE_MOTION_TRIED: String = "tried"

    /**
     * 抖腿
     */
    const val COMMAND_VALUE_MOTION_SHAKE_LEG: String = "shakeLeg"

    /**
     * ======================================== 动作类型 ========================================
     */
    /**
     * 天线灯开
     */
    const val COMMAND_VALUE_ANTENNA_LIGHT_ON: String = "on"

    /**
     * 天线灯关
     */
    const val COMMAND_VALUE_ANTENNA_LIGHT_OFF: String = "off"

    /**
     * 天线闪烁
     */
    const val COMMAND_VALUE_ANTENNA_LIGHT_TWINKLE: String = "twinkle"
}
