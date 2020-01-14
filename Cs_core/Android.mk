LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
OpenCV_INSTALL_MODULES := on
OpenCV_CAMERA_MODULES := off
OPENCV_LIB_TYPE :=STATIC

ifeq ("$(wildcard $(OPENCV_MK_PATH))","")
include D:\android_code\OpenCV-android-sdk\sdk\native\jni\OpenCV.mk
else
include $(OPENCV_MK_PATH)
endif

LOCAL_MODULE := ImgCs
LOCAL_SRC_FILES :=com_cs_cs_ImgEncrypt.cpp com_cs_cs_ImgDecrypt.cpp
LOCAL_CFLAGS  += -std=c++11
LOCAL_LDLIBS +=  -lm -llog


include $(BUILD_SHARED_LIBRARY)

