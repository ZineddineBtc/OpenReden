package com.example.openreden.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.openreden.model.Photo;

import java.util.ArrayList;

public class GalleryAdapter extends PagerAdapter {

    private Context context;
    private ArrayList<Photo> photos;

    public GalleryAdapter(Context context, ArrayList<Photo> photos){
        this.context = context;
        this.photos = photos;
    }
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setImageBitmap(photos.get(position).getBitmap());
        container.addView(imageView, 0);
        return imageView;
    }
    @Override
    public int getCount() {
        return photos.size();
    }
    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ImageView) object);
    }
}












