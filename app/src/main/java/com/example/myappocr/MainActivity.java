package com.example.myappocr;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.pdf.PdfDocument;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.SparseArray;
import android.view.View;
import android.webkit.PermissionRequest;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.BreakIterator;
import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity {

    ImageView preview;
    Button select_image;
    EditText result;
    ImageButton share, search, pdf_btn, cam_btn;
    ConnectivityManager cm;

    String[] storagePermission;
    private static final int IMG_CODE = 5;
    private static final int REQUEST_CAM=7;
    private static final int STORAGE_REQUEST_CODE = 10;
    private final String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/myCamera/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preview = findViewById(R.id.image);
        select_image = findViewById(R.id.select);
        result = findViewById(R.id.text);
        share = findViewById(R.id.share);
        search = findViewById(R.id.search);
        pdf_btn = findViewById(R.id.pdf_btn);
        cam_btn = findViewById(R.id.cam_btn);

        storagePermission = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.CAMERA};
        cm = (ConnectivityManager) MainActivity.this.getSystemService(CONNECTIVITY_SERVICE);

        select_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkStoragePermission()) {
                    requestStoragePermission();
                } else {
                    pickGallery();
                }
            }

            private void requestStoragePermission() {
                ActivityCompat.requestPermissions(MainActivity.this, storagePermission, STORAGE_REQUEST_CODE);
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_CAM);

            }

            private boolean checkStoragePermission() {
                return ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)

                        == PackageManager.PERMISSION_GRANTED;

            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = result.getText().toString();
                if (text.length() != 0) {
                    Intent i = new Intent();
                    i.setAction(Intent.ACTION_SEND);
                    i.putExtra(Intent.EXTRA_TEXT, text);
                    i.setType("text/plain");
                    startActivity(Intent.createChooser(i, "Share with"));
                } else {
                    Toast.makeText(MainActivity.this, "Field empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = result.getText().toString();
                if (text.length() != 0) {
                    Search();
                } else {
                    Toast.makeText(MainActivity.this, "Field empty", Toast.LENGTH_SHORT).show();
                }
            }

            private void Search() {
                if (cm.getActiveNetworkInfo() != null) {
                    String text = result.getText().toString();
                    if (text.length() != 0) {
                        Intent i = new Intent();
                        i.setAction(Intent.ACTION_WEB_SEARCH);
                        i.putExtra(SearchManager.QUERY, text);
                        startActivity(i);


                    } else {
                        Toast.makeText(MainActivity.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
        cam_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == STORAGE_REQUEST_CODE) {
            if(grantResults.length > 0) {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickGallery();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void pickGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), IMG_CODE);
    }
    private void selectImage() {
        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setActivityMenuIconColor(getResources().getColor(R.color.colorAccent))
//                .setBackgroundColor(getResources().getColor(android.R.color.white))
//                .setActivityTitle("")
                .setFixAspectRatio(true)
                .setAspectRatio(1, 1)
                .start(this);
    }

    public void convertButton(View view){
        String file = directory + "imageName";
        Bitmap bitmap = BitmapFactory.decodeFile(file);

        PdfDocument pdfDocument = new PdfDocument();                        //image dimension
        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(960,1280,1).create();
        PdfDocument.Page page = pdfDocument.startPage(myPageInfo);

        page.getCanvas().drawBitmap(bitmap,0,0, null);
        pdfDocument.finishPage(page);

        String pdfFile = directory + "/myPDFFile.pdf";
        File myPDFFile = new File(pdfFile);

        try {
            pdfDocument.writeTo(new FileOutputStream(myPDFFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

        pdfDocument.close();

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMG_CODE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            CropImage.activity(uri)
                    .setActivityTitle("OCR Crop")
                    .setCropMenuCropButtonTitle("Crop")
                    .setMultiTouchEnabled(true)
                    .setAllowRotation(true)
                    .setAllowCounterRotation(true)
                    .setAllowFlipping(true)
                    .setAutoZoomEnabled(true)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(MainActivity.this);
        }
        else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult uri = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                preview.setImageURI(uri.getUri());

                BitmapDrawable bitmapDrawable = (BitmapDrawable) preview.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();

                TextRecognizer textRecognizer = new TextRecognizer.Builder(MainActivity.this).build();

                if (textRecognizer.isOperational()) {
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> items = textRecognizer.detect(frame);

                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < items.size(); i++) {
                        TextBlock myItem = items.valueAt(i);
                        stringBuilder.append(myItem.getValue());
                        if (i != items.size() - 1) {
                            stringBuilder.append("\n");
                        }
                    }
                    result.setText(stringBuilder.toString());


                }
                else {
                    Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}











