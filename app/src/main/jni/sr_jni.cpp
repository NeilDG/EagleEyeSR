//
// Created by NeilDG on 5/4/2017.
//

#include <jni.h>
#include <stdio.h>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/highgui.hpp>
#include <string>
#include <android/log.h>
#include "neildg_com_eagleeyesr_processing_jni_bridge_SuperResJNI.h"

using namespace cv;
using namespace std;

#define  LOG_TAG    "SuperResJNI_Native"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)


JNIEXPORT jlong JNICALL Java_neildg_com_eagleeyesr_processing_jni_1bridge_SuperResJNI_n_1processMat
  (JNIEnv *env, jclass myClass, jlong matAddr1, jlong matAddr2, jlong outputAddr){

  Mat& mat1  = *(Mat*)matAddr1;
  Mat& mat2 = *(Mat*)matAddr2;
  Mat& outputMat = *(Mat*)outputAddr;

  outputMat = mat1 + mat2 + mat1 + mat2;
  //add(mat1, mat2, outputMat);

  return (jlong) &outputMat;
}

JNIEXPORT jlong JNICALL Java_neildg_com_eagleeyesr_processing_jni_1bridge_SuperResJNI_n_1meanFusion
  (JNIEnv *env, jclass myClass, jint j_scaleFactor, jlong inputAddr, jobjectArray objectArr, jlong outputAddr) {

    int size = env->GetArrayLength(objectArr);

    Mat& initialMat = *(Mat*) inputAddr;
    Mat& outputMat = *(Mat*)outputAddr;
    int scaleFactor = (int) j_scaleFactor;

    initialMat.convertTo(initialMat, CV_16UC3);

    //create initial HR grid
    Mat sumMat(initialMat.rows * scaleFactor, initialMat.cols * scaleFactor, CV_16UC3);
    cv::resize(initialMat, sumMat, sumMat.size(), scaleFactor, scaleFactor, INTER_LINEAR);

    initialMat.release();

    for (int i=0; i < size; i++)
    {
        jstring jStringObj = (jstring) env->GetObjectArrayElement(objectArr, i);
        const char* myarray = env->GetStringUTFChars(jStringObj, 0);

        //perform bicubic interpolation
        initialMat = imread(myarray);
        LOGI("Path: %s", myarray);
         if(! initialMat.data )                              // Check for invalid input
         {
                LOGI("Could not open or find the image");
         }


        Mat resizedMat(initialMat.rows * scaleFactor, initialMat.cols * scaleFactor, CV_16UC3);
        cv::resize(initialMat, resizedMat, resizedMat.size(), scaleFactor, scaleFactor, INTER_LINEAR);

        //accumulate to HR grid
        //create mask
        Mat mask = Mat::ones(resizedMat.rows, resizedMat.cols, CV_8UC1);
        cv::add(sumMat, resizedMat, sumMat, mask, CV_16UC3);

        mask.release();
        initialMat.release();

        env->ReleaseStringUTFChars(jStringObj, myarray);
        env->DeleteLocalRef(jStringObj);
    }

     //perform per-element division after accumulating to HR grid
     cv::divide(sumMat, size + 1, sumMat);
     sumMat.convertTo(outputMat, CV_8UC3);
     sumMat.release();

    return (jlong) &outputMat;
  }

JNIEXPORT jint JNICALL Java_neildg_com_eagleeyesr_processing_jni_1bridge_SuperResJNI_n_1testSum
  (JNIEnv *env, jclass myClass, jint a, jint b) {
       jint result = a + b;
       printf("Native part. Answer is: %d", result);
       return result;
  }

