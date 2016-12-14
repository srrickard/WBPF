package com.crusoeapps.whiteboardpicfix;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.crusoeapps.whiteboardpicfix.PicListActivity.*;

public class EffectsActivity extends AppCompatActivity {

    float saturation = 1;
    float brightness = 1;
    float contrast = 1;

    ColorMatrix matSat = new ColorMatrix();
    ColorMatrix matBright = new ColorMatrix();
    ColorMatrix matCont = new ColorMatrix();

    Drawable origDrawable, editedDrawable;
    ImageView img;

    Bitmap origBmp, currBmp;
    //Bitmap unsharpenedBmp, sharpenedBmp;
    String mCurrentPhotoPath;

    boolean isGrayscale = false;
    boolean isSharpened = false;
    boolean isFxSelected = false;

    Button currBtn, tempBtn;

    //Physical buttons
    Button btnInc;
    Button btnDec;
    Button btnSave;
    Button btnReset;

    Button btnBright;
    Button btnCont;
    Button btnGray;
    Button btnSat;
    Button btnSharp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_effects);

        Bundle bundle = getIntent().getExtras();
        Uri uri = Uri.parse(bundle.getString("path"));
        origBmp = BitmapFactory.decodeFile(uri.getPath());
        currBmp = origBmp.copy(origBmp.getConfig(), true);

        origDrawable = new BitmapDrawable(getResources(), origBmp);
        editedDrawable = new BitmapDrawable(getResources(), origBmp);

        img=(ImageView)findViewById(R.id.imageView);

        // init imageview to the drawable
        img.setImageDrawable(origDrawable);

        initMatrices();

        btnInc = (Button)findViewById(R.id.btnDecrease);
        btnDec = (Button)findViewById(R.id.btnIncrease);
        btnSave = (Button)findViewById(R.id.btnSave);
        btnReset = (Button)findViewById(R.id.btnReset);

        btnBright = (Button)findViewById(R.id.btnBrightness);
        btnCont = (Button)findViewById(R.id.btnContrast);
        btnGray = (Button)findViewById(R.id.btnGrayscale);
        btnSat = (Button)findViewById(R.id.btnSaturation);
        btnSharp = (Button)findViewById(R.id.btnSharpen);

        currBtn = btnSharp; // temp fix for null btn prob

        // init button background colors
        btnBright.setBackgroundColor(Color.LTGRAY);
        btnCont.setBackgroundColor(Color.LTGRAY);
        btnGray.setBackgroundColor(Color.LTGRAY);
        btnSat.setBackgroundColor(Color.LTGRAY);
        btnSharp.setBackgroundColor(Color.LTGRAY);
        // -- gray out btnSharp
        btnSharp.setTextColor(Color.GRAY);

        // ' + ' button
        btnInc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFxSelected) noEffectMsg(view);
                else incEffect();
            }
        });
        // ' - ' button
        btnDec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFxSelected) noEffectMsg(view);
                else decEffect();
            }
        });

        // SAVE
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });
        // RESET
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetEffects();
            }
        });

        //GRAYSCALE
        btnGray.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleButton(btnGray, isGrayscale);
            }
        });

        //BRIGHTNESS
        btnBright.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markCurrentBtn(btnBright);
            }
        });

        //CONTRAST
        btnCont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markCurrentBtn(btnCont);
            }
        });

        //SATURATION
        btnSat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markCurrentBtn(btnSat);
            }
        });

        //SHARPEN -- not yet implemented
        btnSharp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //toggleButton(btnSharp, isSharpened);
                Snackbar.make(view, "Sharpen feature coming soon!", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                //.setGravity(Gravity.CENTER_HORIZONTAL);
            }
        });
    }

    public void markCurrentBtn(Button pressedBtn) {
        if (!isFxSelected) isFxSelected = true;
        // reset other buttons
        btnSat.setBackgroundColor(Color.LTGRAY);
        btnBright.setBackgroundColor(Color.LTGRAY);
        btnCont.setBackgroundColor(Color.LTGRAY);

        currBtn = pressedBtn;
        currBtn.setBackgroundColor(Color.rgb(73,91,191));
    }

    public void save() {

        BitmapDrawable bmpDrawable = (BitmapDrawable)origDrawable;
        origBmp = bmpDrawable.getBitmap();

        Bitmap filterBmp = Bitmap.createBitmap(origBmp.getWidth(), origBmp.getHeight(), Bitmap.Config.ARGB_8888);

        Paint paint = new Paint();
        paint.setColorFilter(getColorMatrixColorFilter());

        Canvas canvas = new Canvas(filterBmp);
        canvas.drawBitmap(origBmp,0,0,paint);

        File dstFile = null;

        try {
            dstFile = createImageFile();
            FileOutputStream fOut = new FileOutputStream(dstFile);

            filterBmp.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            fOut.flush();
            fOut.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        Uri dstFileUri = Uri.fromFile(dstFile);
        addPicToGallery(dstFileUri);

        setResult(0);
        finish();
    }

    public void incEffect() {
        // SATURATION
        if (currBtn.equals(btnSat)) {
            if(saturation >= 0) saturation -= 0.1;
            setSaturation(saturation);
        }
        // BRIGHTNESS
        else if (currBtn.equals(btnBright)) {
            if(brightness >= -100) brightness -= 10;
            setBrightness(brightness);
        }
        // CONTRAST
        else if (currBtn.equals(btnCont)) {
            if(contrast >= 0) contrast -= 0.1;
            setContrast(contrast);
        }
    }

    public void decEffect() {
        // SATURATION
        if (currBtn.equals(btnSat)) {
            if(saturation <= 2) saturation += 0.1;
            setSaturation(saturation);
        }
        // BRIGHTNESS
        else if (currBtn.equals(btnBright)) {
            if(brightness <= 100) brightness += 10;
            setBrightness(brightness);
        }
        // CONTRAST
        else if (currBtn.equals(btnCont)) {
            if(contrast <= 2) contrast += 0.1;
            setContrast(contrast);
        }

    }

    public void toggleButton(Button btn, boolean on) {
        // if btn is currently off, apply effects and change button to on
        if (!on){
            if(btn.equals(btnGray)) {
                setSaturation(0);
                isGrayscale = !isGrayscale;
                if(currBtn.equals(btnSat)) {
                    //currBtn = tempBtn;
                    btnSat.setBackgroundColor(Color.LTGRAY);
                }

            }
            else if(btn.equals(btnSharp)) {
                //sharpenBmp(currBmp);
                isSharpened = !isSharpened;
            }

            //btn.setBackgroundColor(Color.rgb(70,85,190));
            btn.setBackgroundColor(Color.rgb(255,182,0));
        }
        else {
            if(btn.equals(btnGray)) {
                setSaturation(1);
                isGrayscale = !isGrayscale;
            }
            else if(btn.equals(btnSharp)) {
                //currBmp = unsharpenedBmp;
                isSharpened = !isSharpened;
            }

            btn.setBackgroundColor(Color.LTGRAY);
        }
    }

    public void applyColorMatrix() {
        ColorMatrix masterMatrix = new ColorMatrix();
        masterMatrix.postConcat(matBright);
        masterMatrix.postConcat(matCont);
        masterMatrix.postConcat(matSat);

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(masterMatrix);
        origDrawable.setColorFilter(filter);
    }

    public ColorMatrixColorFilter getColorMatrixColorFilter() {
        ColorMatrix masterMatrix = new ColorMatrix();
        masterMatrix.postConcat(matBright);
        masterMatrix.postConcat(matCont);
        masterMatrix.postConcat(matSat);

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(masterMatrix);
        return filter;
    }

    public void setSaturation(float sat)
    {
        matSat.setSaturation(sat);

        applyColorMatrix();
    }

    public void setBrightness(float brightness)
    {
        matBright.set(new float[] {1, 0, 0, 0, brightness,
                0, 1, 0, 0,brightness,
                0, 0, 1, 0, brightness,
                0, 0, 0, 1, 0});

        applyColorMatrix();
    }

    public void setContrast(float contrast)
    {
        matCont.set(new float[] {contrast, 0, 0, 0, 1,
                                0, contrast, 0, 0, 1,
                                0, 0, contrast, 0, 1,
                                0, 0, 0, 1, 0});

        applyColorMatrix();
    }

    // modified from http://stackoverflow.com/questions/6980418/how-to-sharpen-a-image-in-android?rq=1
    public Bitmap sharpenBmp(Bitmap bmp) {
        int width, height;
        height = bmp.getHeight();
        width = bmp.getWidth();
        int red, green, blue;
        int a1, a2, a3, a4, a5, a6, a7, a8, a9;
        Bitmap sharpBmp = Bitmap.createBitmap(width, height ,bmp.getConfig());

        Canvas canvas = new Canvas(sharpBmp);

        canvas.drawBitmap(bmp, 0, 0, null);
        for (int i = 1; i < width - 1; i++) {
            for (int j = 1; j < height - 1; j++) {

                a1 = bmp.getPixel(i - 1, j - 1);
                a2 = bmp.getPixel(i - 1, j);
                a3 = bmp.getPixel(i - 1, j + 1);
                a4 = bmp.getPixel(i, j - 1);
                a5 = bmp.getPixel(i, j);
                a6 = bmp.getPixel(i, j + 1);
                a7 = bmp.getPixel(i + 1, j - 1);
                a8 = bmp.getPixel(i + 1, j);
                a9 = bmp.getPixel(i + 1, j + 1);

                red = (Color.red(a1) + Color.red(a2) + Color.red(a3) + Color.red(a4) + Color.red(a6) + Color.red(a7) + Color.red(a8) + Color.red(a9)) *(-1)   + Color.red(a5)*9 ;
                green = (Color.green(a1) + Color.green(a2) + Color.green(a3) + Color.green(a4) + Color.green(a6) + Color.green(a7) + Color.green(a8) + Color.green(a9)) *(-1)  + Color.green(a5)*9 ;
                blue = (Color.blue(a1) + Color.blue(a2) + Color.blue(a3) + Color.blue(a4) + Color.blue(a6) + Color.blue(a7) + Color.blue(a8) + Color.blue(a9)) *(-1)   + Color.blue(a5)*9 ;

                sharpBmp.setPixel(i, j, Color.rgb(red, green, blue));
            }
        }
        return sharpBmp;
    }

    public void noEffectMsg(View view) {
        Snackbar.make(view, "No adjustable effect selected.", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
                //.setGravity(Gravity.CENTER_HORIZONTAL);

//        Toast.makeText(this, "No adjustable effect selected.", Toast.LENGTH_SHORT)
//                .show();
    }

    public void initMatrices() {
        float[] initMat = new float[] { 1, 0, 0, 0, 1,
                                        0, 1, 0, 0, 1,
                                        0, 0, 1, 0, 1,
                                        0, 0, 0, 1, 0};

        matBright.set(initMat);
        matCont.set(initMat);
        matSat.set(initMat);

        // also init params
        brightness = 1;
        contrast = 1;
        saturation = 1;
    }

    public void resetEffects() {
        initMatrices();
        applyColorMatrix();
    }

//    public String createImageFilePath() throws IOException {
//        // Create an image file name using the date
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String imageFileName = "JPEG_" + timeStamp;
//
//        // -This stores files in a directory accessible only to app
//        //File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//
//        // This stores file in public photos directory (pics not deleted if app is)
//        //File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
////        File temp = getExternalStorageDirectory();
//
//        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES), "WhiteboardPicFix");
//
//        System.out.println("PUBLIC STORAGE DIR: " + storageDir);
//
//
//        storageDir.mkdirs();
//
//        File image = File.createTempFile(
//                imageFileName,  /* prefix */
//                ".jpg",         /* suffix */
//                storageDir      /* directory */
//        );
//
//        // Save a file: path for use with ACTION_VIEW intents
//        //mCurrentPhotoPath = "file:" + image.getAbsolutePath();
//        return ("file:" + image.getAbsolutePath());
//    }

    public File createImageFile() throws IOException {
        // Create an image file name using the date
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp;

        // -This stores files in a directory accessible only to app
        //File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // This stores file in public photos directory (pics not deleted if app is)
        //File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//        File temp = getExternalStorageDirectory();

        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "WhiteboardPicFix");

        System.out.println("PUBLIC STORAGE DIR: " + storageDir);


        storageDir.mkdirs();

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private void addPicToGallery(Uri imageUri) {
        MediaScannerConnection.scanFile(EffectsActivity.this,
                new String[]{imageUri.getPath()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                    }
                });
    }
}
