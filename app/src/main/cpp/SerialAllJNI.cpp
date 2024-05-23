#include <jni.h>
#include <assert.h>
#include <cstdlib>
#include <string.h>
#include <unistd.h>
#include <sys/system_properties.h>

#include "include/SerialPortLog.h"
#include "include/SerialPort.h"
#include <pthread.h>

#define MAX_PROP_VALUE_LENGTH 10
const char* property_name = "geeui.log.mcuservice";  // 属性名称，这里以 ro.build.version.sdk 为例
void geeuiLog(const char* str1,const char* str2){
    LOGD(" %s: %s", str1, str2);
//    char property_value[MAX_PROP_VALUE_LENGTH];
//    if (__system_property_get(property_name, property_value) >= 0) {
//        if (strcmp(property_value, "0") == 0){
//            LOGD(" %s: %s", str1, str2);
//        }
//    }
}
//
// Created by apple on 2023/4/25.
//

jobject j_callback_obj;
jmethodID j_callback_res_listener;
jmethodID j_callback_ant_listener;

// 将 Java 对象转为全局引用，以便在 C++ 层中持有该对象
void set_callback_obj(JNIEnv *env, jobject obj) {
    if (j_callback_obj != nullptr) {
        env->DeleteGlobalRef(j_callback_obj);
    }
    j_callback_obj = env->NewGlobalRef(obj);
}

// 注册回调函数
void register_listener(JNIEnv *env, jobject obj, jobject listener) {
    // 将 Java 对象转为全局引用，以便在回调函数中持有该对象
    set_callback_obj(env, listener);

    // 获取回调函数的方法 ID
    jclass cls_listener = env->GetObjectClass(listener);
    j_callback_res_listener = env->GetMethodID(cls_listener, "resListener",
                                               "(Ljava/lang/String;)V");
    j_callback_ant_listener = env->GetMethodID(cls_listener, "antListener",
                                               "(Ljava/lang/String;)V");
    env->DeleteLocalRef(cls_listener);
}

// 回调 resListener 函数
void call_res_listener(JNIEnv *env, const char *res) {
    if (j_callback_obj != nullptr && j_callback_res_listener != nullptr) {
        jstring j_res = env->NewStringUTF(res);
        env->CallVoidMethod(j_callback_obj, j_callback_res_listener, j_res);
        env->DeleteLocalRef(j_res);
    }
}

// 回调 antListener 函数
void call_ant_listener(JNIEnv *env, const char *ant) {
    if (j_callback_obj != nullptr && j_callback_ant_listener != nullptr) {
        jstring j_ant = env->NewStringUTF(ant);
        env->CallVoidMethod(j_callback_obj, j_callback_ant_listener, j_ant);
        env->DeleteLocalRef(j_ant);
    }
}

static JNINativeMethod gMethods[] = {
        {"registerSensorDataListener", "(Lcom/letianpai/sensorservice/MCUSensorListener;)V",
         (void *) register_listener},
};

JavaVM *jvm;
JNIEnv *env = nullptr;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    jvm = vm;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    jclass clazz = env->FindClass("com/letianpai/sensorservice/SerialAllJNI");
    if (clazz == nullptr) {
        return JNI_ERR;
    }

    env->RegisterNatives(clazz, gMethods, sizeof(gMethods) / sizeof(JNINativeMethod));
    env->DeleteLocalRef(clazz);

    return JNI_VERSION_1_6;
}


int res_read_func(char *data) {
    if (env != nullptr) {
        call_res_listener(env, data);
    }

    geeuiLog("c++ get res response:", data);
    return 0;
}

int int_read_func(char *data) {
    if (env != nullptr) {
        call_ant_listener(env, data);
    }

    geeuiLog("c++ get int response:", data);
    return 0;
}


void *readDataJni(void *read_func) {
    jint res = jvm->AttachCurrentThread(&env, nullptr);
    if (res != JNI_OK) {
        return nullptr;
    }
    //调用readdata
    readData(read_func);
    jvm->DetachCurrentThread();
    pthread_exit(nullptr);
}

JNIEnv *env2 = nullptr;

void *writeDataJni(char *data) {
    jint res = jvm->AttachCurrentThread(&env2, nullptr);
    if (res != JNI_OK) {
        return nullptr;
    }
//    char *arr[] = {
//            "AT+DateVer\\r\\n",
//            "AT+VerR\\r\\n",
//            "AT+Gsys\\r\\n",
//            "AT+AG,2\\r\\n",
//            "AT+CLIFFR\\r\\n",
//            "AT+CLIFFR\\r\\n",
//            "AT+LEDOn,1\\r\\n",
//            "AT+LEDOn,2\\r\\n",
//            "AT+LEDOn,3\\r\\n",
//            "AT+LEDOn,3\\r\\n",
//            "AT+MOVEW,1,10,2\\r\\n",
//            "AT+MOVEW,3,10,2\\r\\n"
//    };

//    while (1){
//        for (int i =0; i<11;i++) {
//            //调用readdata
//            LOGD("写入数据 -- %s",arr[i]);
//            writeData(arr[i]);
//            usleep(500000);
//        }
//    }
    writeData(data);

    jvm->DetachCurrentThread();
    pthread_exit(nullptr);
}

pthread_t read_thread, write_thread;
read_st *read_func = nullptr;
extern "C"
JNIEXPORT jint JNICALL
Java_com_letianpai_sensorservice_SerialAllJNI_openPort(JNIEnv *env, jobject thiz) {
    env2 = env;
    stop_flag = false;
    struct SerialPortConfig config = {
            .baudrate = 115200,
            .databits = 8,
            .stopbits = 1,
            .parity   = 'n',
    };

    int re = openSerialPort(config);
    LOGE(" openSerialPort::%d", re);
    if (re) {
        setMode(0);
        read_func = (read_st *) malloc(sizeof(read_st));

        read_func->res_read = res_read_func;
        read_func->int_read = int_read_func;

        // 创建线程并执行
        pthread_create(&read_thread, NULL, readDataJni, (void *) read_func);
        pthread_detach(read_thread);
//    pthread_create(&write_thread, NULL, writeDataJni, (void*)data);
//        pthread_detach(read_thread);
//        pthread_kill(read_thread, SIGCHLD);
    }
    return re;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_letianpai_sensorservice_SerialAllJNI_writeData(JNIEnv *env, jobject thiz, jstring data) {
    const char *dataStr = env->GetStringUTFChars(data, NULL);
    geeuiLog("c++ sensorservice - write data is -",dataStr);
    writeData(dataStr);
    env->ReleaseStringUTFChars(data, dataStr);
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_letianpai_sensorservice_SerialAllJNI_closePort(JNIEnv *env, jobject thiz) {
    //kill线程，关闭串口的时候回失败
//    pthread_kill(read_thread, SIGCHLD);
    stop_flag = true;
//    pthread_join(read_thread, nullptr);
    jboolean result = closePort();
    free(read_func);
    read_func = nullptr;
    if (result){
        geeuiLog("c++ close device:","true");
    }else{
        geeuiLog("c++ close device:","false");
    }
    return result;
}


