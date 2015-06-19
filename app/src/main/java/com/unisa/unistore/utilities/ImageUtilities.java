package com.unisa.unistore.utilities;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.unisa.unistore.R;

/**
 * Created by Daniele on 16/05/2015.
 */
public class ImageUtilities {
    private Activity activity;
    private ImageView imageView;

    private DisplayImageOptions options;
    private ImageLoader imageLoader;

    public ImageUtilities(Activity activity, ImageView imageView) {
        this.activity = activity;
        this.imageView = imageView;
    }

    public void displayImage(String URLImmagineCopertina) {
        initImageLoader();

        imageLoader.displayImage(String.valueOf(URLImmagineCopertina), imageView, options);
    }

    private void initImageLoader() {
        // Load image, decode it to Bitmap and display Bitmap in ImageView (or any other view
        //  which implements ImageAware interface)
        ImageView view = (ImageView) activity.findViewById(R.id.loading_image);
        AnimationDrawable mAnimation = new AnimationDrawable();
        if(view != null)
            mAnimation = (AnimationDrawable) view.getDrawable();

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(mAnimation)
                .showImageForEmptyUri(R.drawable.image_not_found)
                .showImageOnFail(R.drawable.image_not_found)
                        //.delayBeforeLoading(2000) //da decrementare (serve per i test dell'animazione)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true).build();

        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(activity));
    }
}