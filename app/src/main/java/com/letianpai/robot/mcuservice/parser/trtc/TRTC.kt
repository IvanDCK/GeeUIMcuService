package com.letianpai.robot.mcuservice.parser.trtc

class TRTC {
    var room_id: Int = 0
    var user_id: String? = null
    var user_sig: String? = null
    var expire_ts: Long = 0

    override fun toString(): String {
        return "TRTC{" +
                "room_id=" + room_id +
                ", user_id='" + user_id + '\'' +
                ", user_sig='" + user_sig + '\'' +
                ", expire_ts=" + expire_ts +
                '}'
    }
}
