// IGeeUISensorDataListener.aidl
package com.letianpai.sensorservice;

// Declare any non-default types here with import statements

interface IGeeUISensorDataListener {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
      void onSensorDataChanged(int sensorData, String sensorType);
}