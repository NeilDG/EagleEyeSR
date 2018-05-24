LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#opencv
OPENCVROOT:= C:\Users\NeilDG\Documents\MSCSGithubProjects\opencv-3.2.0-android-sdk\OpenCV-android-sdk
OPENCV_CAMERA_MODULES:=off
OPENCV_INSTALL_MODULES:=on
OPENCV_LIB_TYPE:=SHARED
include ${OPENCVROOT}/sdk/native/jni/OpenCV.mk

LOCAL_SRC_FILES := sr_jni.cpp
LOCAL_LDLIBS += -llog
LOCAL_MODULE := opencv_bridge

include $(BUILD_SHARED_LIBRARY)