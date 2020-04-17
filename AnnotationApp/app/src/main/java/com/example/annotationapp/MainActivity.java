package com.example.annotationapp;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import android.Manifest;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class MainActivity extends AppCompatActivity implements OnItemSelectedListener
{
    ArrayList<ArrayList<Float>> result;
    Button button,btnsubmit,uploadimage,annotate;
    ImageView imview;
    EditText getnumber;
    String pathToFile=null;
    int numberOfAnnotations=0;
    final int RESULT_LOAD_IMAGE=2;
    float downx = 0,downy = 0,upx = 0,upy = 0;
    Canvas canvas;
    Paint paint;
    float projectedX,projectedY;
    float width,height;
    Spinner spinner;
    List<String> Items=new ArrayList<>();
    List<String> itemsSelected=new ArrayList<>();

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
        btnsubmit = findViewById(R.id.btnsubmit);
        getnumber = findViewById(R.id.getnumber);
        uploadimage = findViewById(R.id.uploadimage);
        annotate = findViewById(R.id.annotate);
        annotate.setVisibility(View.GONE);
        spinner =findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        Items.add("Iron");
        Items.add("Plastic");
        Items.add("Rubber");
        Items.add("Cloth");
        Items.add("Waste Food");
        Items.add("car");
        Items.add("car Number plate");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Items);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        annotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setVisibility(View.VISIBLE);
                uploadimage.setVisibility(View.VISIBLE);
                JSONObject jsonObject = new JSONObject();
                String file = filename();
                File myDir = new File(Environment.getExternalStorageDirectory()+"/Download/"+file);
                myDir.mkdir();
                try {

                    jsonObject.put("name", ""+file);
                    JSONObject wid_hei = new JSONObject();
                    wid_hei.put("width",width);
                    wid_hei.put("height",height);
                    jsonObject.put("size", wid_hei);
                    ArrayList<JSONObject> obj = new ArrayList<>();
                    for(int i=0;i<result.size();i++) {
                        JSONObject temp = new JSONObject();
                        temp.put("bitmap",null);
                        temp.put("classTitle",itemsSelected.get(i));
                        JSONObject points = new JSONObject();
                        ArrayList<ArrayList<Float>> left_top_right_bottom = new ArrayList<>();
                        for(int j=0;j<4;j+=2) {
                            ArrayList<Float> t = new ArrayList<>();
                            t.add(result.get(i).get(j));
                            t.add(result.get(i).get(j+1));
                            left_top_right_bottom.add(t);
                        }
                        points.put("exterior",left_top_right_bottom);
                        points.put("interior",new ArrayList<>());
                        temp.put("points",points);
                        obj.add(temp);
                    }
                    jsonObject.put("objects",obj);
                    } catch (JSONException e) {
                    e.printStackTrace();
                }
                File f = new File(myDir+"/"+file+".json");
                Bitmap bitmap=BitmapFactory.decodeFile(pathToFile);
                File f2 = new File(myDir+"/"+file+".png");
                Writer output= null;
                try {
                    f.createNewFile();
                    f2.createNewFile();
                    output = new BufferedWriter(new FileWriter(f));
                    output.write(jsonObject.toString());
                    output.close();
                    OutputStream stream = new FileOutputStream(f2);
                    bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
                    stream.flush();
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(MainActivity.this,"json file created",Toast.LENGTH_SHORT).show();
                imview.setImageBitmap(null);

            }
        });

        btnsubmit.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ShowToast")
            @Override
            public void onClick(View v) {
                int value = Integer.parseInt(getnumber.getText().toString().trim());
                if(value<=0 || value> numberOfAnnotations)
                {
                    Toast.makeText(MainActivity.this,"Enter a Valid Annotation number!",Toast.LENGTH_LONG).show();
                }
                else
                {
                    numberOfAnnotations--;
                    Toast.makeText(MainActivity.this,"Annotation deleted!",Toast.LENGTH_SHORT).show();
                    result.remove(value-1);
                    itemsSelected.remove(value-1);
                    Bitmap bitmap= BitmapFactory.decodeFile(pathToFile);
                    Bitmap alteredBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
                    canvas = new Canvas(alteredBitmap);
                    Matrix matrix = new Matrix();
                    canvas.drawBitmap(bitmap, matrix, paint);
                    for(int i=0;i<numberOfAnnotations;i++)
                    {
                        redraw(i);
                        canvas.drawText(itemsSelected.get(i)+(i+1),(result.get(i).get(0)+result.get(i).get(2))/2,result.get(i).get(1)+10,paint);
                        imview.invalidate();
                    }
                    imview.setImageBitmap(alteredBitmap);
                }

            }
        });
        uploadimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setVisibility(View.GONE);
                numberOfAnnotations=0;
                result=new ArrayList<>();
                itemsSelected =new ArrayList<>();
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadimage.setVisibility(View.GONE);
                result=new ArrayList<>();
                numberOfAnnotations=0;
                itemsSelected =new ArrayList<>();
                try {
                    dispatchPictureTakerAction();
                } catch (IOException e) {
                    Log.d("error","err"+e.toString());
                }
            }
        });
    }
    @Override
    public void onItemSelected(AdapterView<?> parent,View view, int position, long id) {
        String item = parent.getItemAtPosition(position).toString();
        if(itemsSelected.size() < numberOfAnnotations) {
            itemsSelected.add(item);
            canvas.drawText(item,(result.get(numberOfAnnotations-1).get(0)+result.get(numberOfAnnotations-1).get(2))/2,result.get(numberOfAnnotations-1).get(1),paint);
            imview.invalidate();
        }
        else
        {
            Toast.makeText(MainActivity.this, "Please Annotate and then select !", Toast.LENGTH_LONG).show();
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK)
        {
            if(requestCode==1 && numberOfAnnotations==itemsSelected.size())
            {
                Bitmap bitmap= BitmapFactory.decodeFile(pathToFile);
                width=bitmap.getWidth();
                height=bitmap.getHeight();
                Bitmap alteredBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
                canvas = new Canvas(alteredBitmap);
                paint = new Paint();
                paint.setColor(Color.GREEN);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(10);
                paint.setTextSize(100f);
                Matrix matrix = new Matrix();
                canvas.drawBitmap(bitmap, matrix, paint);
                imview.setImageBitmap(alteredBitmap);

                imview.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int action = event.getAction();
                        switch (action) {
                            case MotionEvent.ACTION_DOWN:
                                annotate.setVisibility(View.VISIBLE);
                                numberOfAnnotations++;
                                downx = event.getX();
                                downy = event.getY();
                                downx = (float) ((double)downx * ((double)bitmap.getWidth()/(double)imview.getWidth()));
                                downy = (float) ((double)downy * ((double)bitmap.getHeight()/(double)imview.getHeight()));
                                break;
                            case MotionEvent.ACTION_UP:
                                upx = event.getX();
                                upy = event.getY();
                                if(upx<0) upx=0;
                                if(upx>imview.getWidth()) upx = imview.getWidth();
                                if(upy < 0) upy =0;
                                if(upy >imview.getHeight() ) upy=imview.getHeight();
                                projectedX = (float)((double)upx * ((double)bitmap.getWidth()/(double)imview.getWidth()));
                                projectedY = (float)((double)upy * ((double)bitmap.getHeight()/(double)imview.getHeight()));if(numberOfAnnotations-itemsSelected.size() == 1) onDrawRect(downx,downy,projectedX,projectedY,paint);
                                else numberOfAnnotations --;
                                imview.invalidate();
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });

            }
            if (requestCode == RESULT_LOAD_IMAGE && null != data) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                pathToFile = cursor.getString(columnIndex);
                cursor.close();
                Bitmap bitmap= BitmapFactory.decodeFile(pathToFile);
                width=bitmap.getWidth();
                height=bitmap.getHeight();
                Bitmap alteredBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
                canvas = new Canvas(alteredBitmap);
                paint = new Paint();
                paint.setColor(Color.GREEN);
                paint.setTextSize(100f);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(10);
                Matrix matrix = new Matrix();
                canvas.drawBitmap(bitmap, matrix, paint);
                imview.setImageBitmap(alteredBitmap);
                imview.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int action = event.getAction();
                        switch (action) {
                            case MotionEvent.ACTION_DOWN:
                                annotate.setVisibility(View.VISIBLE);
                                numberOfAnnotations++;
                                downx = event.getX();
                                downy = event.getY();
                                downx = (float) ((double) downx * ((double) bitmap.getWidth() / (double) imview.getWidth()));
                                downy = (float) ((double) downy * ((double) bitmap.getHeight() / (double) imview.getHeight()));
                                break;
                            case MotionEvent.ACTION_UP:
                                upx = event.getX();
                                upy = event.getY();
                                if (upx < 0) upx = 0;
                                if (upx > imview.getWidth()) upx = imview.getWidth();
                                if (upy < 0) upy = 0;
                                if (upy > imview.getHeight()) upy = imview.getHeight();
                                projectedX = (float) ((double) upx * ((double) bitmap.getWidth() / (double) imview.getWidth()));
                                projectedY = (float) ((double) upy * ((double) bitmap.getHeight() / (double) imview.getHeight()));
                                if(numberOfAnnotations-itemsSelected.size() == 1) onDrawRect(downx, downy, projectedX, projectedY, paint);
                                else numberOfAnnotations--;
                                imview.invalidate();
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

    public void redraw(int i)
    {
        canvas.drawRect(result.get(i).get(0), result.get(i).get(1), result.get(i).get(2), result.get(i).get(3),paint);
    }
    public String filename()
    {
        String answer="";
        for(int i=pathToFile.length()-1;i>=0;i--)
        {
            if(pathToFile.charAt(i)=='/') {
                answer = pathToFile.substring(i+1,pathToFile.length()-4);
                break;
            }
        }
        return answer;
    }
    public void onDrawRect(float x,float y, float x1,float y1 ,Paint paint)
    {
        ArrayList<Float> ans=new ArrayList<>();
        ans.add(x);
        ans.add(y);
        ans.add(x1);
        ans.add(y1);
        canvas.drawRect(x, y, x1, y1,paint);
        canvas.drawText(""+numberOfAnnotations,(x+x1)/2,y+10,paint);
        imview.invalidate();
        result.add(ans);
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

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
