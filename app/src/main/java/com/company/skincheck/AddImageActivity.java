package com.company.skincheck;

import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddImageActivity extends AppCompatActivity {

    private ImageView imageViewAddImage;
    private EditText editTextAddTitle;
    private TextView textViewAddResult, textViewAddResultPercentage, textViewAddDate;
    private Button buttonSave;

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

        imageViewAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });





    }
}