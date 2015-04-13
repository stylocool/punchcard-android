package com.punchcard.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;

//import com.dlazaro66.qrcodereaderview.QRCodeReaderView;

public class BarcodeActivity extends Activity { //implements QRCodeReaderView.OnQRCodeReadListener {
    /*
    private static String TAG = "BarcodeActivity";

    private QRCodeReaderView mydecoderview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode);

        mydecoderview = (QRCodeReaderView) findViewById(R.id.qrcode_reader_view);
        mydecoderview.setOnQRCodeReadListener(this);

        //ImageView line_image = (ImageView) findViewById(R.id.qrcode_reader_green_line);
        //TranslateAnimation mAnimation = new TranslateAnimation(
        //        TranslateAnimation.ABSOLUTE, 0f,
        //        TranslateAnimation.ABSOLUTE, 0f,
        //        TranslateAnimation.RELATIVE_TO_PARENT, 0f,
        //        TranslateAnimation.RELATIVE_TO_PARENT, 0.85f);
        //mAnimation.setDuration(2000);
        //mAnimation.setRepeatCount(-1);
        //mAnimation.setRepeatMode(Animation.REVERSE);
        //mAnimation.setInterpolator(new LinearInterpolator());
        //line_image.setAnimation(mAnimation);
    }


    // Called when a QR is decoded
    // "text" : the text encoded in QR
    // "points" : points where QR control points are placed
    @Override
    public void onQRCodeRead(String text, PointF[] points) {
        Log.d(TAG, "Barcode scanned: " + text);
        Intent i = getIntent();
        i.putExtra("SCAN_RESULT", text);
        setResult(RESULT_OK, i);
        finish();
    }


    // Called when your device have no camera
    @Override
    public void cameraNotFound() {

    }

    // Called when there's no QR codes in the camera preview image
    @Override
    public void QRCodeNotFoundOnCamImage() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mydecoderview.getCameraManager().startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mydecoderview.getCameraManager().stopPreview();
    }
    */
}
