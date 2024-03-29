package com.hxrainbow.faceapplication;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import com.hxrainbow.facedetect.FaceDetectUtil;

public class FaceCameraView extends FrameLayout implements SurfaceHolder.Callback {

    private SurfaceView surfaceView;

    private Camera camera;
    private float screenProp = 1.0f;

    private int pWidth = 320, pHeight = 480;

    private byte[] yvu;
    private Object object;
    private HandlerThread handlerThread;
    private Handler handler;

    private long currentTime;

    public FaceCameraView(@NonNull Context context) {
        this(context, null);
    }

    public FaceCameraView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FaceCameraView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (surfaceView != null) {
            float widthSize = surfaceView.getMeasuredWidth();
            float heightSize = surfaceView.getMeasuredHeight();
            screenProp = heightSize / widthSize;
        } else {
            screenProp = Util.getScreenHeight() / Util.getScreenWidth();
        }
    }

    public void initView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.face_camera_view, this);
        surfaceView = view.findViewById(R.id.sv_camera);

        surfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);
        surfaceView.getHolder().setKeepScreenOn(true);
        surfaceView.getHolder().addCallback(this);

        object = new Object();
        handlerThread = new HandlerThread("face_demo");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1000) {
                    currentTime = System.currentTimeMillis();
                    synchronized (object) {
                        byte[] temp = Util.decodeYUV(yvu, pWidth, pHeight);
                        FaceDetectUtil.Detect(temp, pWidth, pHeight);
                        int lenth = FaceDetectUtil.GetResultLength();
                        Log.e("lht", "leght:" + lenth);
                    }
                    Log.e("lhtF", "time:" + (System.currentTimeMillis() - currentTime));
                }
            }
        };

    }

    private void createCamera() {
        if (camera == null) {
            camera = Camera.open(0);
        }
    }

    private void openCamera(SurfaceHolder holder) {
        if (camera != null) {
            try {
                Camera.Parameters parameters = camera.getParameters();
                Camera.Size previewSize = CameraParamUtil.getInstance().getPreviewSize(parameters.getSupportedPreviewSizes(), 1000, screenProp);
                Camera.Size pictureSize = CameraParamUtil.getInstance().getPictureSize(parameters.getSupportedPictureSizes(), 1200, screenProp);
                parameters.setPreviewSize(previewSize.width, previewSize.height);
                parameters.setPictureSize(pictureSize.width, pictureSize.height);
                parameters.setJpegQuality(100);
                if (CameraParamUtil.getInstance().isSupportedFocusMode(parameters.getSupportedFocusModes(), Camera.Parameters.FOCUS_MODE_AUTO)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                }
                if (CameraParamUtil.getInstance().isSupportedPictureFormats(parameters.getSupportedPictureFormats(), ImageFormat.JPEG)) {
                    parameters.setPictureFormat(ImageFormat.JPEG);
                    parameters.setJpegQuality(100);
                }
                camera.setParameters(parameters);
                camera.setPreviewDisplay(holder);
                camera.setDisplayOrientation(0);

                pHeight = previewSize.height;
                pWidth = previewSize.width;
                Log.e("lhtZ", "height:" + pHeight + ",width:" + pWidth);
                yvu = new byte[pHeight * pWidth * 2];

                camera.setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(final byte[] data, Camera camera) {

//                        Log.e("lhtC", "&&&&&&&&&&&&&&&&&&&&");

//                        currentTime = System.currentTimeMillis();

//                        synchronized (yvu) {
//                            System.arraycopy(data, 0, yvu, 0, data.length);
//                        }
//                        Log.e("lhtF", "time:" + (System.currentTimeMillis() - currentTime));
//                        handler.removeCallbacksAndMessages(null);
//                        handler.sendEmptyMessage(1000);
//                        Executors.newCachedThreadPool().execute(new Runnable() {
//                            @Override
//                            public void run() {
//                                synchronized (object) {
//                                    byte[] temp = Util.decodeYUV(yvu, pWidth, pHeight);
//                                    FaceDetectUtil.Detect(temp, pWidth, pHeight);
//                                    int lenth = FaceDetectUtil.GetResultLength();
//                                    Log.e("lht", "leght:" + lenth);
//                                    FaceTrack.getInstance().faceDetect(yvu, pHeight, pWidth);
//                                }
//                            }
//                        });

//                        byte[] temp = Util.decodeYUV(data, pWidth, pHeight);
//                        Log.e("lhtF", "time:" + (System.currentTimeMillis() - currentTime));
                        FaceDetectUtil.Detect(data, pWidth, pHeight);
                        int lenth = FaceDetectUtil.GetResultLength();
                        Log.e("lht", "leght:" + lenth);
//                        Log.e("lhtF", "time:" + (System.currentTimeMillis() - currentTime));

                    }
                });
                camera.startPreview();
            } catch (Exception e) {

            }
        }
    }

    private void closeCamera() {
        if (camera != null) {
            try {
                camera.setPreviewCallback(null);
            } catch (Exception e) {
                Log.e("aliyun", "camera error : " + e.getClass() + "---" + e.getMessage());
            }
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        createCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        openCamera(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        closeCamera();
    }

}
