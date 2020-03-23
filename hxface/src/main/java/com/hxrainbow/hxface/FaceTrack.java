package com.hxrainbow.hxface;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

public class FaceTrack {

    private static volatile FaceTrack instance;

    private FaceTrack() {

    }

    public static FaceTrack getInstance() {
        if (instance == null) {
            synchronized (FaceTrack.class) {
                if (instance == null) {
                    instance = new FaceTrack();
                }
            }
        }
        return instance;
    }

    private FaceTracking faceTracking;

    public List<Face> faceDetect(byte[] data, int height, int width) {
        long currentTime = System.currentTimeMillis();
        if (faceTracking == null) {
            faceTracking = new FaceTracking(Environment.getExternalStorageDirectory() + File.separator + "FaceTracking" + File.separator + "models");
            faceTracking.FaceTrackingInit(data, height, width);
        } else {
            faceTracking.Update(data, height, width);
        }
        List<Face> faces = faceTracking.getTrackingInfo();
        if (faces != null) {
            Log.e("lhtNL", "lenth:" + faces.size());
            for (int i = 0; i < faces.size(); i++) {
                Log.e("lhtNF", i + "---" + "left:" + faces.get(i).left + ",right:" + faces.get(i).right + ",top:" + faces.get(i).top + ",bottom:" + faces.get(i).bottom);
            }
        }
        Log.e("lhtN", "time:" + (System.currentTimeMillis() - currentTime));
        return faces;
    }

    public void init(Context context) {
        copyFilesFromAssets(context, "FaceTracking", Environment.getExternalStorageDirectory() + File.separator + "FaceTracking");
    }

    public void release() {
        if (faceTracking != null) {
            faceTracking.release();
        }
    }

    private void copyFilesFromAssets(Context context, String oldPath, String newPath) {
        try {
            String[] fileNames = context.getAssets().list(oldPath);
            if (fileNames.length > 0) {
                // directory
                File file = new File(newPath);
                if (!file.mkdir()) {
                    Log.d("mkdir", "can't make folder");
                }

                for (String fileName : fileNames) {
                    copyFilesFromAssets(context, oldPath + "/" + fileName,
                            newPath + "/" + fileName);
                }
            } else {
                // file
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount;
                while ((byteCount = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, byteCount);
                }
                fos.flush();
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
