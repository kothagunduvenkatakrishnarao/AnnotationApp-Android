package com.example.annotationapp;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import static android.os.Environment.getExternalStoragePublicDirectory;


public class MainActivity extends AppCompatActivity
{
    Button button;
    ImageView imview;
    String pathToFile=null;
    float downx = 0,downy = 0,upx = 0,upy = 0;
    Canvas canvas;
    Paint paint;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(Build.VERSION.SDK_INT>=23)
        {
            requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
        }
        button = findViewById(R.id.button);
        imview =findViewById(R.id.imview);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    dispatchPictureTakerAction();
                } catch (IOException e) {
                    Log.d("error","err"+e.toString());
                }
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK)
        {
            if(requestCode==1)
            {
                Bitmap bitmap= BitmapFactory.decodeFile(pathToFile);
                Bitmap alteredBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                        .getHeight(), bitmap.getConfig());
                canvas = new Canvas(alteredBitmap);
                paint = new Paint();
                paint.setColor(Color.GREEN);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(10);
                Matrix matrix = new Matrix();
                canvas.drawBitmap(bitmap, matrix, paint);
                canvas.drawRect(100, 100, 100, 100,paint);
                imview.setImageBitmap(alteredBitmap);
//                imview.setImageBitmap(bitmap);
//                canvas = new Canvas(bitmap);
//                canvas.drawRect(100,100,100,100,paint);
//                imview.setImageBitmap(bitmap);
                Log.i("krishna","demo");
                imview.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int action = event.getAction();
                        switch (action) {
                            case MotionEvent.ACTION_DOWN:
                                downx = event.getX();
                                downy = event.getY();
//                                Log.d("downx",""+downx);
//                                Log.d("downy",""+downy);
                                break;
                            case MotionEvent.ACTION_MOVE:
                                upx = event.getX();
                                upy = event.getY();
                                imview.setImageBitmap(alteredBitmap);
                                canvas.drawRect(downx, downy, 400-upx, 400-upy,paint);
                                imview.invalidate();
                                break;
                            case MotionEvent.ACTION_UP:
                                upx = event.getX();
                                upy = event.getY();
//                                Log.i("upx",""+upx);
//                                Log.i("upy",""+upy);
                                canvas.drawRect(downx, downy, upx, upy,paint);
                                imview.invalidate();
                                break;
                            case MotionEvent.ACTION_CANCEL:
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });

            }
        }
    }

    private void dispatchPictureTakerAction() throws IOException {
        Intent takepic= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takepic.resolveActivity(getPackageManager())!=null)
        {
            File photofile=null;
            photofile=createPhotoFile();
            if(photofile!=null)
            {
                pathToFile = photofile.getAbsolutePath();
                Uri photoURI= FileProvider.getUriForFile(MainActivity.this,"com.example.annotationapp.fileprovider",photofile);
                takepic.putExtra(MediaStore.EXTRA_OUTPUT,photoURI);
                startActivityForResult(takepic, 1);
            }
        }
    }

    private File createPhotoFile() throws IOException {
        String name=new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir= getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image=null;
        try {
            image= File.createTempFile(name,".jpg",storageDir);
        }
        catch (IOException e)
        {
            Log.d("mylog","Exep"+e.toString());
        }
        return image;
    }
}
