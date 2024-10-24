package com.letianpai.robot.mcuservice.parser.motion

class Motion(var motion: String, var number: Int) {
    override fun toString(): String {
        return "{" +
                "motion='" + motion + '\'' +
                ", number=" + number +
                '}'
    }
}
