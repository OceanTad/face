#include <android/bitmap.h>
#include <android/log.h>
#include <jni.h>
#include <string>
#include <vector>
#include "UltraFace.hpp"
#include <opencv2/opencv.hpp>


#define TAG "FaceSDKNative"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

using namespace std;

static UltraFace *ultra;
bool detection_sdk_init_ok = false;

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_hxrainbow_facedetect_FaceSDKNative_FaceDetectionModelInit(JNIEnv *env, jobject instance,
                                                      jstring faceDetectionModelPath_) {
    LOGD("JNI init native sdk");
    if (detection_sdk_init_ok) {
        LOGD("sdk already init");
        return true;
    }
    jboolean tRet = false;
    if (NULL == faceDetectionModelPath_) {
        LOGD("model dir is empty");
        return tRet;
    }

    //获取模型的绝对路径的目录（不是/aaa/bbb.bin这样的路径，是/aaa/)
    const char *faceDetectionModelPath = env->GetStringUTFChars(faceDetectionModelPath_, 0);
    if (NULL == faceDetectionModelPath) {
        LOGD("model dir is empty");
        return tRet;
    }

    string tFaceModelDir = faceDetectionModelPath;
    string tLastChar = tFaceModelDir.substr(tFaceModelDir.length()-1, 1);

    //RFB-320-quant-ADMM-32
    //RFB-320-quant-KL-5792
    string str = tFaceModelDir + "slim-320.mnn";

    //ultra = new UltraFace(tFaceModelDir+ "RFB-320.bin", tFaceModelDir+ "RFB-320.param",  320, 4, 0.7);
    ultra = new  UltraFace(str, 240, 160, 8, 0.65 ); // config model input

    env->ReleaseStringUTFChars(faceDetectionModelPath_, faceDetectionModelPath);
    detection_sdk_init_ok = true;
    tRet = true;

    return tRet;
}

JNIEXPORT jintArray JNICALL
Java_com_hxrainbow_facedetect_FaceSDKNative_FaceDetect(JNIEnv *env, jobject instance, jbyteArray imageDate_,
                                          jint imageWidth, jint imageHeight, jint imageChannel) {
    if(!detection_sdk_init_ok){
        LOGD("sdk not init");
        return NULL;
    }

//    int tImageDateLen = env->GetArrayLength(imageDate_);
//    if(imageChannel == tImageDateLen / imageWidth / imageHeight){
//        LOGD("imgW=%d, imgH=%d,imgC=%d",imageWidth,imageHeight,imageChannel);
//    }
//    else{
//        LOGD("img data format error");
//        return NULL;
//    }

    jbyte *imageDate = env->GetByteArrayElements(imageDate_, NULL);
    if (NULL == imageDate){
        LOGD("img data is null");
        return NULL;
    }

    if(imageWidth<20||imageHeight<20){
        LOGD("img is too small");
        return NULL;
    }

    std::vector<FaceInfo> face_info;
    cv::Mat image(imageHeight + imageHeight / 2, imageWidth, CV_8UC1, (unsigned char *) imageDate);
    cv::Mat mBgr;
//    cv::cvtColor(image, mBgr, CV_YUV2RGB);
    cv::cvtColor(image, mBgr, CV_YUV2RGB_NV21);
    ultra ->detect(mBgr.data , imageWidth, imageHeight, 3,  face_info );

//    std::vector<FaceInfo> face_info;
//
//    ultra ->detect((unsigned char*)imageDate , imageWidth, imageHeight, imageChannel,  face_info );

    int32_t num_face = static_cast<int32_t>(face_info.size());

    int out_size = 1+num_face*9;
    int *allfaceInfo = new int[out_size];
    allfaceInfo[0] = num_face;
    for (int i=0; i<num_face; i++) {

        allfaceInfo[9*i+1] = face_info[i].x1;//left
        allfaceInfo[9*i+2] = face_info[i].y1;//top
        allfaceInfo[9*i+3] = face_info[i].x2;//right
        allfaceInfo[9*i+4] = face_info[i].y2;//bottom

    }

    jintArray tFaceInfo = env->NewIntArray(out_size);
    env->SetIntArrayRegion(tFaceInfo, 0, out_size, allfaceInfo);
    env->ReleaseByteArrayElements(imageDate_, imageDate, 0);


    delete [] allfaceInfo;

    return tFaceInfo;
}

JNIEXPORT jboolean JNICALL
Java_com_hxrainbow_facedetect_FaceSDKNative_FaceDetectionModelUnInit(JNIEnv *env, jobject instance) {

    jboolean tDetectionUnInit = false;

    if (!detection_sdk_init_ok) {
        LOGD("sdk not inited, do nothing");
        return true;
    }

    delete ultra;

    detection_sdk_init_ok = false;

    tDetectionUnInit = true;

    LOGD("sdk release ok");

    return tDetectionUnInit;
}

}