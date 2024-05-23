// IGeeUISensorDataListener.aidl
package com.letianpai.sensorservice;

// Declare any non-default types here with import statements

interface GeeUISensorDataListener {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
      fun onSensorDataChanged(sensorData: Int, sensorType: String);
}