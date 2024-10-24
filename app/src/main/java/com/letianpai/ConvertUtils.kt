package com.letianpai

import com.renhejia.robot.commandlib.log.LogUtils
import android.util.Log

object ConvertUtils {
    fun intToHex(n: Int): String {
        //StringBuffer s = new StringBuffer();
        var n = n
        var sb = java.lang.StringBuilder(8)
        val b = charArrayOf(
            '0',
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9',
            'A',
            'B',
            'C',
            'D',
            'E',
            'F'
        )
        while (n != 0) {
            sb = sb.append(b[n % 16])
            n = n / 16
        }
        val a = sb.reverse().toString()
        //        LogUtils.logi("","16进制： "+ a);
        return a
    }

    fun convertToHexString(content: String): String {
        LogUtil.e("convertToHexString: $content")
        var result = ""
        for (i in 0 until content.length) {
//           LogUtils.logi("","content.charAt(i)_"+i +"_"+ content.charAt(i));
//           LogUtils.logi("","content.charAt(i)_"+i +"_"+  intToHex(content.charAt(i)));
            result = result + intToHex(content[i].code)
        }

        return result
    }

    /**
     * 十六进制转十进制
     * @param hex
     * @return
     */
    fun hexToDec(hex: String): Int {
        return hex.toInt(16)
    }


    fun decodeHexString(hexString: String): String {
        require(hexString.length % 2 != 1) { "Invalid hexadecimal String supplied." }
        val results = arrayOfNulls<String>(hexString.length / 2)
        val builder = java.lang.StringBuilder()
        var i = 0
        while (i < hexString.length) {
            builder.append((hexToDec(hexString.substring(i, i + 2))).toChar())
            i += 2
        }

        return builder.toString()
    }
}
