package com.company.skincheck;

import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MyImagesAdapter extends RecyclerView.Adapter<MyImagesAdapter.MyImagesHolder>{

    List<MyImages> imagesList = new ArrayList<>();
    private OnImageClickListener listener;

    public void setListener(OnImageClickListener listener) {
        this.listener = listener;
    }

    public void setImagesList(List<MyImages> imagesList) {
        this.imagesList = imagesList;
        notifyDataSetChanged();
    }

    public interface OnImageClickListener{

        void onImageClick(MyImages myImages);

    }

    public MyImages getPosition(int position){

        return imagesList.get(position);

    }

    @NonNull
    @Override
    public MyImagesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_card,parent,false);

        return new MyImagesHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyImagesHolder holder, int position) {
        if (imagesList != null && position < imagesList.size()) {
            MyImages myImages = imagesList.get(position);
            holder.textViewTitle.setText(myImages.getImage_title());
            holder.textViewResult.setText(String.valueOf(myImages.getImage_result()));
            holder.textViewResultPercentage.setText(String.valueOf(myImages.getImage_result_percentage()));
            holder.textViewDate.setText(myImages.getImage_date());

            byte[] imageBytes = myImages.getImage();
            if (imageBytes != null) {
                holder.imageView.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
            } else {
                holder.imageView.setImageResource(R.drawable.photo);
            }
        }
    }

    @Override
    public int getItemCount() {
        return imagesList.size();
    }

    public class MyImagesHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        TextView textViewTitle, textViewResult, textViewResultPercentage, textViewDate;

        public MyImagesHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewResult = itemView.findViewById(R.id.textViewResult);
            textViewResultPercentage = itemView.findViewById(R.id.textViewResultPercentage);
            textViewDate = itemView.findViewById(R.id.textViewDate);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int position = getAdapterPosition();

                    if (listener != null && position != RecyclerView.NO_POSITION){

                        listener.onImageClick(imagesList.get(position));

                    }
                }
            });


        }
    }
}
