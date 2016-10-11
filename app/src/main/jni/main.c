#include <jni.h>
#include "neildg_com_megatronsr_MainActivity.h"

JNIEXPORT jstring JNICALL Java_neildg_com_megatronsr_MainActivity_hello
  (JNIEnv * env, jobject obj){
    return (*env)->NewStringUTF(env, "Hello from JNI");
  }
