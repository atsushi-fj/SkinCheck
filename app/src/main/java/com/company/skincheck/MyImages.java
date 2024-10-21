package com.company.skincheck;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

@Entity(tableName = "my_images")
public class MyImages {

    @PrimaryKey(autoGenerate = true)
    public int image_id;
    public String image_title;
    public int image_result;
    public float image_result_percentage;
    public String image_date;
    public byte[] image;

    public MyImages(String image_title,
                    int image_result,
                    float image_result_percentage,
                    String image_date,
                    byte[] image) {
        this.image_title = image_title;
        this.image_result = image_result;
        this.image_result_percentage = image_result_percentage;
        this.image_date = image_date;
        this.image = image;
    }

    public int getImage_id() {
        return image_id;
    }

    public String getImage_title() {
        return image_title;
    }

    public String getImage_date() {
        return image_date;
    }

    public int getImage_result() {
        return image_result;
    }

    public float getImage_result_percentage() {
        return image_result_percentage;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage_id(int image_id) {
        this.image_id = image_id;
    }
}
