package com.letianpai

import android.util.Log
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader

class ShellUtils private constructor() {
    init {
        throw UnsupportedOperationException("u can't instantiate me...")
    }

    class CommandResult(var result: Int, var successMsg: String, var errorMsg: String) {
        override fun toString(): String {
            return """
                result: ${result}
                successMsg: ${successMsg}
                errorMsg: ${errorMsg}
                """.trimIndent()
        }
    }

    companion object {
        private val LINE_SEP: String? = System.lineSeparator()

        fun execCmdAsync(
            command: String,
            isRooted: Boolean,
            callback: Utils.Callback<CommandResult?>
        ): Utils.Task<CommandResult> {
            return execCmdAsync(arrayOf(command), isRooted, true, callback)
        }

        fun execCmdAsync(
            commands: List<String>?,
            isRooted: Boolean,
            callback: Utils.Callback<CommandResult?>
        ): Utils.Task<CommandResult> {
            return execCmdAsync(
                commands?.toTypedArray<String>(),
                isRooted,
                true,
                callback
            )
        }

        fun execCmdAsync(
            commands: Array<String>?,
            isRooted: Boolean,
            callback: Utils.Callback<CommandResult?>
        ): Utils.Task<CommandResult> {
            return execCmdAsync(commands, isRooted, true, callback)
        }

        fun execCmdAsync(
            command: String,
            isRooted: Boolean,
            isNeedResultMsg: Boolean,
            callback: Utils.Callback<CommandResult?>
        ): Utils.Task<CommandResult> {
            return execCmdAsync(arrayOf(command), isRooted, isNeedResultMsg, callback)
        }

        fun execCmdAsync(
            commands: List<String>?,
            isRooted: Boolean,
            isNeedResultMsg: Boolean,
            callback: Utils.Callback<CommandResult?>
        ): Utils.Task<CommandResult> {
            return execCmdAsync(
                commands?.toTypedArray<String>(),
                isRooted,
                isNeedResultMsg,
                callback
            )
        }

        fun execCmdAsync(
            commands: Array<String>?,
            isRooted: Boolean,
            isNeedResultMsg: Boolean,
            callback: Utils.Callback<CommandResult?>
        ): Utils.Task<CommandResult> {
            return Utils.Companion.doAsync<CommandResult>(object :
                Utils.Task<CommandResult>(callback) {
                public override fun doInBackground(): CommandResult {
                    return execCmd(commands, isRooted, isNeedResultMsg)
                }
            })
        }

        fun execCmd(command: String, isRooted: Boolean): CommandResult {
            return execCmd(arrayOf(command), isRooted, true)
        }

        fun execCmd(commands: List<String>?, isRooted: Boolean): CommandResult {
            return execCmd(
                commands?.toTypedArray<String>(),
                isRooted,
                true
            )
        }

        fun execCmd(commands: Array<String>?, isRooted: Boolean): CommandResult {
            return execCmd(commands, isRooted, true)
        }

        fun execCmd(command: String, isRooted: Boolean, isNeedResultMsg: Boolean): CommandResult {
            return execCmd(arrayOf(command), isRooted, isNeedResultMsg)
        }

        fun execCmd(
            commands: List<String>?,
            isRooted: Boolean,
            isNeedResultMsg: Boolean
        ): CommandResult {
            return execCmd(
                commands?.toTypedArray<String>(),
                isRooted,
                isNeedResultMsg
            )
        }

        fun execCmd(
            commands: Array<String>?,
            isRooted: Boolean,
            isNeedResultMsg: Boolean
        ): CommandResult {
            var result = -1
            if (commands != null && commands.isNotEmpty()) {
                var process: Process? = null
                var successResult: BufferedReader? = null
                var errorResult: BufferedReader? = null
                var successMsg: StringBuilder? = null
                var errorMsg: StringBuilder? = null
                var os: DataOutputStream? = null

                try {
                    val builder = ProcessBuilder("sh")
                    builder.redirectErrorStream(true) // 合并标准错误和标准输出
                    process = builder.start()

                    // process = Runtime.getRuntime().exec(isRooted ? SystemUtils.getSuAlias() : "sh");
                    os = DataOutputStream(process.outputStream)
                    val var10: Array<String> = commands
                    val var11 = commands.size

                    for (var12 in 0 until var11) {
                        val command = var10[var12]
                        if (command != null) {
                            os.write(command.toByteArray())
                            os.writeBytes(LINE_SEP)
                            os.flush()
                        }
                    }

                    os.writeBytes("exit$LINE_SEP")
                    os.flush()
                    result = process.waitFor()
                    if (isNeedResultMsg) {
                        successMsg = StringBuilder()
                        errorMsg = StringBuilder()
                        successResult =
                            BufferedReader(InputStreamReader(process.inputStream, "UTF-8"))
                        errorResult =
                            BufferedReader(InputStreamReader(process.errorStream, "UTF-8"))
                        var line: String
                        if ((successResult.readLine().also { line = it }) != null) {
                            successMsg.append(line)

                            while ((successResult.readLine().also { line = it }) != null) {
                                Log.d("<<<", "读取到的：$line")
                                successMsg.append(LINE_SEP).append(line)
                            }
                        }

                        if ((errorResult.readLine().also { line = it }) != null) {
                            errorMsg.append(line)

                            while ((errorResult.readLine().also { line = it }) != null) {
                                errorMsg.append(LINE_SEP).append(line)
                            }
                        }
                    }
                } catch (var30: Exception) {
                    var30.printStackTrace()
                } finally {
                    try {
                        os?.close()
                    } catch (var29: IOException) {
                        var29.printStackTrace()
                    }

                    try {
                        successResult?.close()
                    } catch (var28: IOException) {
                        var28.printStackTrace()
                    }

                    try {
                        errorResult?.close()
                    } catch (var27: IOException) {
                        var27.printStackTrace()
                    }

                    process?.destroy()
                }

                return CommandResult(
                    result, successMsg?.toString() ?: "",
                    errorMsg?.toString() ?: ""
                )
            } else {
                return CommandResult(result, "", "")
            }
        }
    }
}

