package com.company.skincheck;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
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

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class AddImageActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private ImageView imageViewAddImage;
    private EditText editTextAddTitle;
    private TextView textViewAddResult, textViewAddResultPercentage, textViewAddDate;
    private Button buttonSave;
    private Bitmap bitmap;

    private Interpreter tflite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Add Image");
        setContentView(R.layout.activity_add_image);

        imageViewAddImage = findViewById(R.id.imageViewAddImage);
        editTextAddTitle = findViewById(R.id.editTextAddTitle);
        textViewAddResult = findViewById(R.id.textViewAddResult);
        textViewAddResultPercentage = findViewById(R.id.textViewAddResultPercentage);
        textViewAddDate = findViewById(R.id.textViewAddDate);
        buttonSave = findViewById(R.id.buttonSave);

        try {
            tflite = new Interpreter(loadModelFile("model.tflite")); // モデルファイル名を指定
        } catch (IOException e) {
            Log.e("AddImageActivity", "Error loading model", e);
        }

        imageViewAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // カメラのパーミッションを確認
                if (ContextCompat.checkSelfPermission(AddImageActivity.this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // パーミッションがない場合、リクエストする
                    ActivityCompat.requestPermissions(AddImageActivity.this,
                            new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                } else {
                    // パーミッションが既に付与されている場合、カメラを起動
                    openCamera();
                }
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            bitmap = (Bitmap) extras.get("data");
            imageViewAddImage.setImageBitmap(bitmap);
            classifyImage(bitmap); // 画像を分類
        }
    }

    private void classifyImage(Bitmap bitmap) {
        // 画像をTensorFlow Liteモデルに入力するための前処理
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true); // モデルの入力サイズに合わせる
        float[][] result = new float[1][3]; // 出力の形状に合わせる（クラス数に応じて変更）

        // 画像をモデルに入力
        tflite.run(resizedBitmapToByteArray(resizedBitmap), result);

        // 結果を表示
        textViewAddResult.setText("クラス: " + result[0][0]);
        textViewAddResultPercentage.setText("確率: " + result[0][1] * 100 + "%");
        // 日付を表示
        textViewAddDate.setText(java.text.DateFormat.getDateInstance().format(new java.util.Date()));
    }

    private byte[][][] resizedBitmapToByteArray(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        byte[][][] input = new byte[1][width][height]; // モデルの入力サイズに合わせる
        // バイト配列に変換する処理を追加（モデルの入力形式に応じて調整）
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int pixel = bitmap.getPixel(i, j);
                input[0][i][j] = (byte) ((pixel >> 16) & 0xFF); // R
                input[0][i][j] = (byte) ((pixel >> 8) & 0xFF);  // G
                input[0][i][j] = (byte) (pixel & 0xFF);         // B
            }
        }
        return input;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // パーミッションが付与された場合、カメラを起動
                openCamera();
            } else {
                // パーミッションが拒否された場合の処理
                // ユーザーに説明するか、他のアクションを考慮する
            }
        }
    }

    private MappedByteBuffer loadModelFile(String modelFileName) throws IOException {
        // モデルファイルをアセットから読み込む処理を追加
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
}