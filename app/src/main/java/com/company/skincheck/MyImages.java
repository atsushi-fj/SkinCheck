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
    @TypeConverters({Converters.class})
    public Date image_date;
    public String image_result;
    public byte[] image;

    public MyImages(String image_title, Date image_date, String image_result, byte[] image) {
        this.image_title = image_title;
        this.image_date = image_date;
        this.image_result = image_result;
        this.image = image;
    }

    public int getImage_id() {
        return image_id;
    }

    public String getImage_title() {
        return image_title;
    }

    public Date getImage_date() {
        return image_date;
    }

    public String getImage_result() {
        return image_result;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage_id(int image_id) {
        this.image_id = image_id;
    }
}
