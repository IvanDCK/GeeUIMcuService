package com.letianpai.robot.mcuservice.callback

/**
 * At命令回调
 */
class AtCommandCallback private constructor() {
    private var mAtCmdResultReturnListener: AtCmdResultReturnListener? = null

    private object AtCommandCallbackHolder {
        val instance: AtCommandCallback = AtCommandCallback()
    }

    fun interface AtCmdResultReturnListener {
        fun onAtCmdResultReturn(atCmdResult: String)
    }

    fun setAtCmdResultReturnListener(listener: AtCmdResultReturnListener?) {
        this.mAtCmdResultReturnListener = listener
    }

    fun setAtCmdResultReturn(atCmdResult: String) {
        if (mAtCmdResultReturnListener != null) {
            mAtCmdResultReturnListener!!.onAtCmdResultReturn(atCmdResult)
        }
    }

    companion object {
        val instance: AtCommandCallback
            get() = AtCommandCallbackHolder.instance
    }
}
