#include <jni.h>
#include "neildg_com_eagleeyesr_MainActivity.h"

JNIEXPORT jstring JNICALL Java_neildg_com_eagleeyesr_MainActivity_hello
  (JNIEnv * env, jobject obj){
    return (*env)->NewStringUTF(env, "Hello from JNI world!");
  }
