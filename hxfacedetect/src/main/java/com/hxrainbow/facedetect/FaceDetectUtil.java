package com.hxrainbow.facedetect;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static android.content.ContentValues.TAG;

public class FaceDetectUtil {

    private static FaceSDKNative faceSDKNative = new FaceSDKNative();
    private static int[] faceInfo = new int[]{};

    public static void Init(Context context) {
        //copy model
        try {
//            copyBigDataToSD(context,"RFB-320.mnn");
            copyBigDataToSD(context, "slim-320.mnn");
        } catch (IOException e) {
            e.printStackTrace();
        }

        initFaceNative();
    }

    public static void UnInit() {
        if (faceSDKNative != null) {
            faceSDKNative.FaceDetectionModelUnInit();
        }
        faceSDKNative = null;
    }

    public static void Detect(byte[] imageData, int width, int height) {
        long timeDetectFace = System.currentTimeMillis();
        //do FaceDetect
        if (faceSDKNative == null) {
            initFaceNative();
        }
        faceInfo = faceSDKNative.FaceDetect(imageData, width, height, 4);
        timeDetectFace = System.currentTimeMillis() - timeDetectFace;
        Log.d(TAG, "DetectTime：" + timeDetectFace + ", ResultLength: " + GetResultLength());
    }

    public static int GetResultLength() {
        if (faceInfo != null) {
            return faceInfo.length;
        }
        return 0;
    }

    public static int GetResultByIndex(int index) {
        if (faceInfo != null && faceInfo.length > index) {
            return faceInfo[index];
        }
        return 0;
    }

    private static void copyBigDataToSD(Context context, String strOutFileName) throws IOException {
        Log.i(TAG, "start copy file " + strOutFileName);
        File sdDir = Environment.getExternalStorageDirectory();//get root dir
        File file = new File(sdDir.toString() + "/facesdk/");
        if (!file.exists()) {
            file.mkdir();
        }

        String tmpFile = sdDir.toString() + "/facesdk/" + strOutFileName;
        File f = new File(tmpFile);
        if (f.exists()) {
            Log.i(TAG, "file exists " + strOutFileName);
            return;
        }
        InputStream myInput;
        java.io.OutputStream myOutput = new FileOutputStream(sdDir.toString() + "/facesdk/" + strOutFileName);
        myInput = context.getAssets().open(strOutFileName);
        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while (length > 0) {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }
        myOutput.flush();
        myInput.close();
        myOutput.close();
        Log.i(TAG, "end copy file " + strOutFileName);
    }

    private static void initFaceNative() {
        File sdDir = Environment.getExternalStorageDirectory();//get model store dir
        String sdPath = sdDir.toString() + "/facesdk/";

        if (faceSDKNative == null) {
            faceSDKNative = new FaceSDKNative();
        }
        faceSDKNative.FaceDetectionModelInit(sdPath);
    }

}
