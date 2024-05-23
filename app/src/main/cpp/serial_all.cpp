//
// Created by frey on 2018/6/4.
//

#include <fcntl.h>
#include <termios.h>
#include <unistd.h>
#include <cstdlib>
#include <cstdio>
#include <cstring>
#include <asm-generic/ioctl.h>
#include <linux/serial.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/ioctl.h>
#include <threads.h>
#include <jni.h>
#include "include/SerialPort.h"

#define DEBUG

#ifdef DEBUG
#define l81_log(...)  printf(__VA_ARGS__)
#else
#define l81_log(...)
#endif

#define l81_err(...)  printf(__VA_ARGS__)

#define path "/dev/ttyS5"
#define FALSE 0
#define TRUE 1

#define restr  "AT+RES"
#define intstr "AT+INT"
#define endstr "\r\n"

bool stop_flag = false;


char *check_res(char data[]) {
    return strstr(data, restr);
}

char *check_int(char data[]) {
    return strstr(data, intstr);
}

char *check_end(char data[]) {
    return strstr(data, endstr);
}



bool isClose = true;
static int fd = 0;
static int write_lock = 0;
//int res_read_func(char *data) {
//    printf("get res response:\n %s\n", data);
//    printf("------------------------------\n");
//    return 0;
//}
//
//int int_read_func(char *data) {
//    printf("get int response:\n %s\n", data);
//    printf("------------------------------\n");
//    return 0;
//}

int getBaudrate(int baudrate) {
    switch (baudrate) {
        case 0:
            return B0;
        case 50:
            return B50;
        case 75:
            return B75;
        case 110:
            return B110;
        case 134:
            return B134;
        case 150:
            return B150;
        case 200:
            return B200;
        case 300:
            return B300;
        case 600:
            return B600;
        case 1200:
            return B1200;
        case 1800:
            return B1800;
        case 2400:
            return B2400;
        case 4800:
            return B4800;
        case 9600:
            return B9600;
        case 19200:
            return B19200;
        case 38400:
            return B38400;
        case 57600:
            return B57600;
        case 115200:
            return B115200;
        case 230400:
            return B230400;
        case 460800:
            return B460800;
        case 500000:
            return B500000;
        case 576000:
            return B576000;
        case 921600:
            return B921600;
        case 1000000:
            return B1000000;
        case 1152000:
            return B1152000;
        case 1500000:
            return B1500000;
        case 2000000:
            return B2000000;
        case 2500000:
            return B2500000;
        case 3000000:
            return B3000000;
        case 3500000:
            return B3500000;
        case 4000000:
            return B4000000;
        default:
            return -1;
    }
}

int setSpeed(int fd, int speed) {
    speed_t b_speed;
    struct termios cfg;
    b_speed = getBaudrate(speed);
    if (tcgetattr(fd, &cfg)) {
        l81_err("tcgetattr invocation method failed!");
        close(fd);
        return FALSE;
    }

    cfmakeraw(&cfg);
    cfsetispeed(&cfg, b_speed);
    cfsetospeed(&cfg, b_speed);

    if (tcsetattr(fd, TCSANOW, &cfg)) {
        l81_err("tcsetattr invocation method failed!");
        close(fd);
        return FALSE;
    }
    return TRUE;
}

int setParity(int fd, int databits, int stopbits, char parity) {
    struct termios options;
    if (tcgetattr(fd, &options) != 0) {
        l81_err("The method tcgetattr exception!");
        return FALSE;
    }
    options.c_cflag &= ~CSIZE;
    switch (databits)                                           /* Set data bits */
    {
        case 7:
            options.c_cflag |= CS7;
            break;
        case 8:
            options.c_cflag |= CS8;
            break;
        default:
            l81_err("Unsupported data size!");
            return FALSE;
    }
    switch (parity) {
        case 'n':
        case 'N':
            options.c_cflag &= ~PARENB;                         /* Clear parity enable */
            options.c_iflag &= ~INPCK;                          /* Enable parity checking */
            break;
        case 'o':
        case 'O':
            options.c_cflag |= (PARODD | PARENB);               /* Set odd checking */
            options.c_iflag |= INPCK;                           /* Disnable parity checking */
            break;
        case 'e':
        case 'E':
            options.c_cflag |= PARENB;                          /* Enable parity */
            options.c_cflag &= ~PARODD;                         /* Transformation even checking */
            options.c_iflag |= INPCK;                           /* Disnable parity checking */
            break;
        case 'S':
        case 's':  /*as no parity*/
            options.c_cflag &= ~PARENB;
            options.c_cflag &= ~CSTOPB;
            break;
        default:
            l81_err("Unsupported parity!");
            return FALSE;
    }
    /* 设置停止位*/
    switch (stopbits) {
        case 1:
            options.c_cflag &= ~CSTOPB;
            break;
        case 2:
            options.c_cflag |= CSTOPB;
            break;
        default:
            l81_err("Unsupported stop bits!");
            return FALSE;
    }
    /* Set input parity option */
    if (parity != 'n')
        options.c_iflag |= INPCK;
    tcflush(fd, TCIFLUSH);
    options.c_cc[VTIME] = 150;                                  /* Set timeout to 15 seconds */
    options.c_cc[VMIN] = 0;                                     /* Update the options and do it NOW */
    if (tcsetattr(fd, TCSANOW, &options) != 0) {
        l81_err("The method tcsetattr exception!");
        return FALSE;
    }
    return TRUE;
}

int openSerialPort(struct SerialPortConfig config) {

    struct serial_struct serial;


    fd = open(path, O_RDWR | O_NOCTTY | O_NDELAY);
    if (fd < 0) {
        l81_err("Error to read %s port file!", path);
        return FALSE;
    }
    l81_log("Open device!\n");

    ioctl(fd, TIOCGSERIAL, &serial);

    serial.xmit_fifo_size = 1024; //1K

    ioctl(fd, TIOCSSERIAL, &serial);

    if (!setSpeed(fd, config.baudrate)) {
        l81_err("Set Speed Error!");
        return FALSE;
    }
    if (!setParity(fd, config.databits, config.stopbits, config.parity)) {
        l81_err("Set Parity Error!");
        return FALSE;
    }

    tcflush(fd, TCIFLUSH);

    isClose = false;
    l81_log("Open Success!\n");
    return TRUE;
}

void *readData(void *read_func) {
    struct timeval timeout;
    timeout.tv_sec = 0;       // 设置超时时间的秒数为 0
    timeout.tv_usec = 0;  // 设置超时时间的微秒数为 10000，即 10 毫秒
    int ret = 0, retval = 0;
    char tmp;
    char buf[1024] = {'\0'}; //used for store read data
    int count = 0;

    read_st *func = (read_st *) read_func;
    fd_set rfds;

    l81_log("begin read\n");
    LOGD("begin read\n");
    if (isClose) return 0;

    FD_ZERO(&rfds);
    FD_SET(fd, &rfds);
    // TODO Async operation. Thread blocking.

    if (FD_ISSET(fd, &rfds)) {
        FD_ZERO(&rfds);
        FD_SET(fd, &rfds);

        while (!stop_flag) {
            usleep(1000);
            //if someone write tty, we should stop reading until writting finished
            if (write_lock) {
                continue;
            }

            memset(buf, '\0', 1024);
            for (count = 0; (count < 1024) && !stop_flag; count++) {
                retval = select(fd + 1, &rfds, NULL, NULL, NULL);
                if (retval == -1) {
                    l81_err("Select error!");
                } else if (retval) {
                    ret = read(fd, &tmp, 1);
                    buf[count] = tmp;
                }else{
                    //超时
                    l81_err("read timeout!\n");
                }
                if (check_end(buf))
                    break;
            }
//            LOGD("c++ sensor buf= %s", buf);
            if (check_res(buf)) {
                func->res_read(buf);
                memset(buf, '\0', 1024);
                LOGD("c++ sensor buf111= %s", buf);
                continue;
            }
            if (check_int(buf)) {
                func->int_read(buf);
                memset(buf, '\0', 1024);
                continue;
            }
            memset(buf, '\0', 1024);
            tcflush(fd, TCIFLUSH);
        }
    }
    return NULL;
}

void *writeData(const char *data) {
    int result = 0;

    write_lock = 1;
    result = static_cast<int>(write(fd, data, strlen(data)));
    if (result < 0)
        l81_err("error\n");

    write_lock = 0;

    return NULL;
}

int setMode(int mode) {
    struct termios options;
    if (tcgetattr(fd, &options) != 0) {
        l81_err("The method tcgetattr exception!");
        return FALSE;
    }
    if (mode != 0) {
        if (mode == 1) {
            options.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);   //input
            options.c_oflag &= ~OPOST;                            //out put
        } else if (mode == 2) {
            options.c_lflag |= (ICANON | ECHO | ECHOE | ISIG);    //input
            options.c_oflag |= OPOST;                             //out put
        }
        if (tcsetattr(fd, TCSANOW, &options) != 0) {
            l81_err("The method tcsetattr exception!");
            return FALSE;
        }
    }
    return TRUE;
}

bool closePort() {
    if (!isClose) {
        close(fd);
        isClose = true;
    }
    l81_log("Close device!");
    return isClose;
}

int main_gee(res_read_callback rescallback, int_read_callback intcallback) {
    char data[20] = {'\0'};

    read_st *read_func = (read_st *) malloc(sizeof(read_st));

    pthread_t read_thread, write_thread;

    read_func->res_read = rescallback;
    read_func->int_read = intcallback;

//	memcpy(data, "AT+DateVer\\r\\n", strlen("AT+DateVer\\r\\n"));

    struct SerialPortConfig config = {
            .baudrate = 115200,
            .databits = 8,
            .stopbits = 1,
            .parity   = 'n',
    };

    int re = openSerialPort(config);
    if (re) {
        LOGD("串口打开成功");
    } else {
        LOGD("串口打开失败");
    }

    setMode(0);


//	readData((void*)read_func);

    pthread_create(&read_thread, NULL, readData, (void *) read_func);
    //ret = readData(recData, 1024);


//    pthread_create(&write_thread, NULL, writeData, (void *) data);
    //ret = writeData(data, strlen(data));
    //
//    while (1);

//    closePort();

    return re;
}
