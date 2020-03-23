package com.hxrainbow.faceapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.hxrainbow.facedetect.FaceDetectUtil;

public class MainActivity extends AppCompatActivity {

    private boolean permission = false;

    private FaceCameraView faceCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FaceDetectUtil.Init(this);
//        FaceTrack.getInstance().init(this);
        setContentView(R.layout.activity_main);
        checkPermission();
    }

    private void checkPermission() {
        String[] needPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        boolean result = true;
        for (int i = 0; i < needPermission.length; i++) {
            boolean permissionResult = checkCallingOrSelfPermission(needPermission[i]) == PackageManager.PERMISSION_GRANTED;
            result = result & permissionResult;
        }
        if (result) {
            initView();
        } else {
            ActivityCompat.requestPermissions(this, needPermission, 1001);
        }
    }

    private void initView() {
        faceCameraView = findViewById(R.id.fcv_camera);
        faceCameraView.initView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permission = true;
        if (requestCode == 1001) {
            for (int i : grantResults) {
                if (i != PackageManager.PERMISSION_GRANTED) {
                    permission = false;
                    break;
                }
            }
            if (permission) {
                initView();
            } else {
                Log.e("lht", "**********");
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        FaceDetectUtil.UnInit();
//        FaceTrack.getInstance().release();
        super.onDestroy();
    }
}
