package com.hxrainbow.faceapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import static android.graphics.Bitmap.createBitmap;

public class FaceCameraView extends FrameLayout implements SurfaceHolder.Callback {

    private SurfaceView surfaceView;

    private Camera camera;
    private ICameraCallBack cameraCallBack;
    private int cameraPosition = 1;
    private float screenProp = 1.0f;

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
    }

    private void createCamera(int cameraPosition) {
        if (camera == null) {
            camera = Camera.open(cameraPosition);
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
                camera.setDisplayOrientation(90);
                camera.startPreview();
            } catch (Exception e) {
                if (cameraCallBack != null) {
                    cameraCallBack.onOpenError();
                }
            }
        } else {
            if (cameraCallBack != null) {
                cameraCallBack.onOpenError();
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

    public void takePhoto() {
        if (camera != null) {
            camera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    Matrix matrix = new Matrix();
                    if (cameraPosition == 1) {
                        matrix.setRotate(270);
                        matrix.postScale(-1, 1);
                    } else if (cameraPosition == 0) {
                        matrix.setRotate(90);
                    }
                    bitmap = createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//                    float realWidth = bitmap.getWidth() * width / Util.getScreenWidth();
//                    float realHeight = bitmap.getHeight() * height / Util.getScreenHeight();
//                    if (realHeight > realWidth) {
//                        realHeight = realWidth;
//                    } else {
//                        realWidth = realHeight;
//                    }
//                    int startX = (int) ((bitmap.getWidth() - realWidth) / 2);
//                    int startY = (int) (bitmap.getHeight() / Util.getScreenHeight());
//                    bitmap = createBitmap(bitmap, startX, startY, (int) realWidth, (int) realHeight, null, false);
                    if (cameraCallBack != null) {
                        if (bitmap != null) {
                            cameraCallBack.onTakePhoto(bitmap);
                        } else {
                            cameraCallBack.onTakeError();
                        }
                    }
                    camera.startPreview();
                }
            });
        } else {
            if (cameraCallBack != null) {
                cameraCallBack.onOpenError();
            }
        }
    }

    public void changeCamera() {
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT && cameraPosition == 0) {
                closeCamera();
                createCamera(i);
                if (surfaceView != null) {
                    openCamera(surfaceView.getHolder());
                }
                cameraPosition = 1;
                break;
            }
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK && cameraPosition == 1) {
                closeCamera();
                createCamera(i);
                if (surfaceView != null) {
                    openCamera(surfaceView.getHolder());
                }
                cameraPosition = 0;
                break;
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        createCamera(cameraPosition);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        openCamera(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        closeCamera();
    }

    public void setCameraCallBack(ICameraCallBack cameraCallBack) {
        this.cameraCallBack = cameraCallBack;
    }

    public interface ICameraCallBack {

        void onTakePhoto(Bitmap bitmap);

        void onOpenError();

        void onTakeError();

    }

}
