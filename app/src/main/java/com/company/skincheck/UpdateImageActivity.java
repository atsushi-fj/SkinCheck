package com.company.skincheck;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;

public class UpdateImageActivity extends AppCompatActivity {

    private ImageView imageViewUpdateImage;
    private EditText editTextUpdateTitle;
    private TextView textViewUpdateResult, textViewUpdateResultPercentage, textViewUpdateDate;
    private Button buttonUpdate;

    private String title, date;
    private int id, result;
    private float result_percentage;
    private byte[] image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Update Image");
        setContentView(R.layout.activity_update_image);

        imageViewUpdateImage = findViewById(R.id.imageViewUpdateImage);
        editTextUpdateTitle = findViewById(R.id.editTextUpdateTitle);
        textViewUpdateResult = findViewById(R.id.textViewUpdateResult);
        textViewUpdateResultPercentage = findViewById(R.id.textViewUpdateResultPercentage);
        textViewUpdateDate = findViewById(R.id.textViewUpdateDate);
        buttonUpdate = findViewById(R.id.buttonUpdate);

        id = getIntent().getIntExtra("id", -1);
        title = getIntent().getStringExtra("title");
        result = getIntent().getIntExtra("result", -1);
        result_percentage = getIntent().getFloatExtra("result_percentage", -1);
        date = getIntent().getStringExtra("date");
        image = getIntent().getByteArrayExtra("image");

        editTextUpdateTitle.setText(title);
        textViewUpdateResult.setText("クラス: " + result);
        textViewUpdateResultPercentage.setText("確率: " + (result_percentage * 100) + "%");
        textViewUpdateDate.setText(date);
        imageViewUpdateImage.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length));

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                updateData();

            }
        });

    }

    public void updateData(){

        if (id == -1) {
            Toast.makeText(UpdateImageActivity.this
                    , "問題があります", Toast.LENGTH_SHORT).show();
        } else {
            String updateTitle = editTextUpdateTitle.getText().toString();

            Intent intent = new Intent();
            intent.putExtra("id", id);
            intent.putExtra("updateTitle", updateTitle);
            intent.putExtra("updateResult", result);
            intent.putExtra("updateResultPercentage", result_percentage);
            intent.putExtra("updateDate", date);
            intent.putExtra("updateImage", image);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}