package com.company.skincheck;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Locale;

public class AddImageActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final String TAG = "AddImageActivity";

    private ImageView imageViewAddImage;
    private EditText editTextAddTitle;
    private TextView textViewAddResult, textViewAddResultPercentage, textViewAddFeedback, textViewAddDate;
    private Button buttonSave;
    private Bitmap capturedImage;
    private Bitmap scaledImage;
    private int maxIndex;
    private float maxValue;
    private String capturedDate;

    private Interpreter tflite;

    ActivityResultLauncher<Intent> takePictureLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Add Image");
        setContentView(R.layout.activity_add_image);

        imageViewAddImage = findViewById(R.id.imageViewAddImage);
        editTextAddTitle = findViewById(R.id.editTextAddTitle);
        textViewAddResult = findViewById(R.id.textViewAddResult);
        textViewAddResultPercentage = findViewById(R.id.textViewAddResultPercentage);
        textViewAddFeedback = findViewById(R.id.textViewAddFeedback);
        textViewAddDate = findViewById(R.id.textViewAddDate);
        buttonSave = findViewById(R.id.buttonSave);

        try {
            tflite = new Interpreter(loadModelFile("model.tflite"));
        } catch (IOException e) {
            Log.e("AddImageActivity", "Error loading model", e);
        }

        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Bundle extras = result.getData().getExtras();
                        capturedImage = (Bitmap) extras.get("data");
                        imageViewAddImage.setImageBitmap(capturedImage);

                        classifyImage(capturedImage);
                    }
                }
        );

        imageViewAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(AddImageActivity.this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AddImageActivity.this,
                            new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                } else {
                    openCamera();
                }
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (capturedImage == null) {
                    Toast.makeText(AddImageActivity.this
                            , "写真を撮ってください!", Toast.LENGTH_SHORT).show();
                } else {

                    String title = editTextAddTitle.getText().toString();
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    scaledImage = makeSmall(capturedImage, 300);
                    scaledImage.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
                    byte[] image = outputStream.toByteArray();

                    Intent intent = new Intent();
                    intent.putExtra("title", title);
                    intent.putExtra("result", maxIndex);
                    intent.putExtra("result_percentage", maxValue);
                    intent.putExtra("date", capturedDate);
                    intent.putExtra("image", image);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureLauncher.launch(takePictureIntent);
    }

    private void classifyImage(Bitmap bitmap) {
        float[][][][] inputTensor = convertBitmapToTensor(bitmap);
        float[][] result = new float[1][7];

        tflite.run(inputTensor, result);

        maxIndex = 0;
        maxValue = result[0][0];

        for (int i = 1; i < result[0].length; i++) {
            if (result[0][i] > maxValue) {
                maxValue = result[0][i];
                maxIndex = i;
            }
        }

        capturedDate = java.text.DateFormat
                .getDateInstance(java.text.DateFormat.LONG, Locale.JAPAN)
                .format(new java.util.Date());

        String[] resultArray = getResources().getStringArray(R.array.result_array);
        String[] feedbackArray = getResources().getStringArray(R.array.feedback_array);

        textViewAddResult.setText("クラス: " + resultArray[maxIndex]);
        textViewAddResultPercentage.setText("確率: " + (maxValue * 100) + "%");
        textViewAddFeedback.setText("説明: " + feedbackArray[maxIndex]);
        textViewAddDate.setText(capturedDate);
    }

    private float[][][][] convertBitmapToTensor(Bitmap bitmap) {
        int width = 224;
        int height = 224;
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        float[][][][] inputTensor = new float[1][height][width][3];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = resizedBitmap.getPixel(x, y);
                inputTensor[0][y][x][0] = ((Color.red(pixel) / 255.0f));
                inputTensor[0][y][x][1] = ((Color.green(pixel) / 255.0f));
                inputTensor[0][y][x][2] = ((Color.blue(pixel) / 255.0f));
            }
        }
        return inputTensor;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Camera permission granted");
                openCamera();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    Log.w(TAG, "Camera permission denied. Showing rationale.");
                    showPermissionDeniedExplanation();
                } else {
                    Log.w(TAG, "Camera permission permanently denied. Showing settings option.");
                    showSettingsRedirect();
                }
            }
        }
    }

    private void showPermissionDeniedExplanation() {
        new AlertDialog.Builder(this)
                .setTitle("カメラ権限が必要です")
                .setMessage("アプリで写真を撮影するにはカメラの権限が必要です。設定から権限を許可してください。")
                .setPositiveButton("設定へ", (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", getPackageName(), null));
                    startActivity(intent);
                })
                .setNegativeButton("キャンセル", null)
                .show();
    }

    private void showSettingsRedirect() {
        new AlertDialog.Builder(this)
                .setTitle("カメラ権限が無効です")
                .setMessage("カメラ権限が無効になっています。設定画面で権限を有効にしてください。")
                .setPositiveButton("設定へ", (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", getPackageName(), null));
                    startActivity(intent);
                })
                .setNegativeButton("キャンセル", null)
                .show();
    }


    private MappedByteBuffer loadModelFile(String modelFileName) throws IOException {
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd(modelFileName);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tflite != null) {
            tflite.close();
        }
    }

    public Bitmap makeSmall(Bitmap image, int maxSize){

        int width = image.getWidth();
        int height = image.getHeight();

        float ratio = (float) width / (float) height;

        if (ratio > 1){

            width = maxSize;
            height = (int) (width / ratio);

        } else {

            height = maxSize;
            width = (int) (height * ratio);

        }

        return Bitmap.createScaledBitmap(image,width,height,true);
    }
}