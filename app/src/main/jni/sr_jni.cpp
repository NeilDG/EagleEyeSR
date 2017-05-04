//
// Created by NeilDG on 5/4/2017.
//

#include <jni.h>
#include <stdio.h>
#include <opencv2/core.hpp>
#include "neildg_com_eagleeyesr_processing_jni_bridge_SuperResJNI.h"

using namespace cv;

JNIEXPORT jlong JNICALL Java_neildg_com_eagleeyesr_processing_jni_1bridge_SuperResJNI_n_1processMat
  (JNIEnv *env, jclass myClass, jlong matAddr1, jlong matAddr2, jlong outputAddr){

  Mat& mat1  = *(Mat*)matAddr1;
  Mat& mat2 = *(Mat*)matAddr2;
  Mat& outputMat = *(Mat*)outputAddr;

  outputMat = mat1 + mat2 + mat1 + mat2;
  //add(mat1, mat2, outputMat);

  return (jlong) &outputMat;
}

JNIEXPORT jint JNICALL Java_neildg_com_eagleeyesr_processing_jni_1bridge_SuperResJNI_n_1testSum
  (JNIEnv *env, jclass myClass, jint a, jint b) {
       jint result = a + b;
       printf("Native part. Answer is: %d", result);
       return result;
  }

