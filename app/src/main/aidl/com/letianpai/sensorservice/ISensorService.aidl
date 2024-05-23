// ILetianpaiService.aidl
package com.letianpai.sensorservice;
import com.letianpai.sensorservice.IGeeUISensorDataListener;
import com.letianpai.sensorservice.IGeeUISensoWriteResListener;
// Declare any non-default types here with import statements

interface ISensorService {
     void registerGeeUIWriteResListener(IGeeUISensoWriteResListener listener);
     void unRegisterGeeUIWriteResListener(IGeeUISensoWriteResListener listener);
     void writeAtCommand(String command);

      void registerGeeUISensorIRDataListener(IGeeUISensorDataListener listener);
      void unRegisterGeeUISensorIRDataListener(IGeeUISensorDataListener listener);
}