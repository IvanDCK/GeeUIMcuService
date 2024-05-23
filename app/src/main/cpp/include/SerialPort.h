//
// Created by frey on 2018/6/4.
//

#ifndef SERIALPORTHELPER_SERIALPORT_H
#define SERIALPORTHELPER_SERIALPORT_H

#include <sys/ioctl.h>

#include "SerialPortLog.h"


typedef unsigned char BYTE;

/** define states. */
#define FALSE  0
#define TRUE   1
extern bool stop_flag;
/** Serial port config param. */
struct SerialPortConfig {
    int baudrate;   // read speed
    int databits;   // one of 7,8
    int stopbits;   // one of 1,2
    char parity;    // one of N,E,O,S
};

typedef int (*res_read_callback)(char *);
typedef int (*int_read_callback)(char *);

typedef struct {
    res_read_callback res_read;
    int_read_callback int_read;
} read_st;


int openSerialPort(struct SerialPortConfig config);
int setMode(int mode);
void *readData(void *read_func);
void *writeData(const char *data);
bool closePort();

/** Serial port device class. */
#endif //SERIALPORTHELPER_SERIALPORT_H
