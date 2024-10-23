package com.company.skincheck;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rv;
    private FloatingActionButton fab;

    private MyImagesViewModel myImagesViewModel;

    private ActivityResultLauncher<Intent> activityResultLauncherForAddImage;
    private ActivityResultLauncher<Intent> activityResultLauncherForUpdateImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // register activity
        registerActivityForAddImage();
        registerActivityForUpdateImage();

        rv = findViewById(R.id.rv);
        fab = findViewById(R.id.fab);

        rv.setLayoutManager(new LinearLayoutManager(this));

        MyImagesAdapter adapter = new MyImagesAdapter(this);
        rv.setAdapter(adapter);

        myImagesViewModel = new ViewModelProvider.AndroidViewModelFactory(getApplication())
                .create(MyImagesViewModel.class);

        myImagesViewModel.getAllImages().observe(MainActivity.this, new Observer<List<MyImages>>() {
            @Override
            public void onChanged(List<MyImages> myImages) {

                adapter.setImagesList(myImages);

            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, AddImageActivity.class);
                activityResultLauncherForAddImage.launch(intent);

            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0
                , ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                myImagesViewModel.delete(adapter.getPosition(viewHolder.getAdapterPosition()));

            }
        }).attachToRecyclerView(rv);

        adapter.setListener(new MyImagesAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(MyImages myImages) {

                Intent intent = new Intent(MainActivity.this, UpdateImageActivity.class);
                intent.putExtra("id", myImages.getImage_id());
                intent.putExtra("title", myImages.getImage_title());
                intent.putExtra("result", myImages.getImage_result());
                intent.putExtra("result_percentage", myImages.getImage_result_percentage());
                intent.putExtra("date", myImages.getImage_date());
                intent.putExtra("image", myImages.getImage());
                //activityResultLauncher
                activityResultLauncherForUpdateImage.launch(intent);

            }
        });
    }

    public void registerActivityForUpdateImage(){

        activityResultLauncherForUpdateImage
                = registerForActivityResult(new ActivityResultContracts.StartActivityForResult()
                , new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {

                        int resultCode = o.getResultCode();
                        Intent data = o.getData();

                        if (resultCode == RESULT_OK && data != null){

                            String title = data.getStringExtra("updateTitle");
                            int result = data.getIntExtra("updateResult", -1);
                            float resultPercentage = data.getFloatExtra("updateResultPercentage", -1);
                            String date = data.getStringExtra("updateDate");
                            byte[] image = data.getByteArrayExtra("updateImage");
                            int id = data.getIntExtra("id", -1);

                            MyImages myImages = new MyImages(title, result, resultPercentage, date, image);
                            myImages.setImage_id(id);
                            myImagesViewModel.update(myImages);
                        }
                    }
                });

    }
    
    public void registerActivityForAddImage(){

        activityResultLauncherForAddImage
                = registerForActivityResult(new ActivityResultContracts.StartActivityForResult()
                , new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        int resultCode = o.getResultCode();
                        Intent data = o.getData();

                        if (resultCode == RESULT_OK && data != null){
                            String title = data.getStringExtra("title");
                            int result = data.getIntExtra("result", 0);
                            float result_percentage = data.getFloatExtra("result_percentage", 0);
                            String date = data.getStringExtra("date");
                            byte[] image = data.getByteArrayExtra("image");

                            MyImages myImages = new MyImages(title, result, result_percentage, date, image);
                            myImagesViewModel.insert(myImages);

                        }

                    }
                });
    }
}