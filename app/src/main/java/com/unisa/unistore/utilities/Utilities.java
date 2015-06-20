package com.unisa.unistore.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.parse.ParseUser;
import com.unisa.unistore.R;

import java.io.InputStream;

/**
 * Created by Daniele on 20/04/2015.
 */
public class Utilities {

    public void scaleImage(ImageView imageView) {
        if(imageView != null) {
            // Get the ImageView and its bitmap
            Drawable drawing = imageView.getDrawable();
            if(drawing != null) {
                Bitmap bitmap = ((BitmapDrawable) drawing).getBitmap();
                if (bitmap != null) {
                    // Get current dimensions
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();

                    // Determine how much to scale: the dimension requiring less scaling is
                    // closer to the its side. This way the image always stays inside your
                    // bounding box AND either x/y axis touches it.

                    float xScale = ((float) width / width);
                    float yScale = ((float) height / height);
                    float scale = (xScale <= yScale) ? xScale : yScale;

                    // Create a matrix for the scaling and add the scaling data
                    Matrix matrix = new Matrix();
                    matrix.postScale(scale, scale);

                    // Create a new bitmap and convert it to a format understood by the ImageView
                    Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
                    BitmapDrawable result = new BitmapDrawable(scaledBitmap);
                    width = scaledBitmap.getWidth();
                    height = scaledBitmap.getHeight();

                    // Apply the scaled bitmap
                    imageView.setImageDrawable(result);

                    // Now change ImageView's dimensions to match the scaled image
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
                    params.width = width;
                    params.height = height;
                    imageView.setLayoutParams(params);
                }
            }
        }
    }

    public static boolean isUserAuthenticated() {
        if(ParseUser.getCurrentUser() != null)
            return true;

        return false;
    }

    public static String convertStreamToString(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static void slideUP(Context context, View view) {
        view.setVisibility(View.VISIBLE);
        view.startAnimation(AnimationUtils.loadAnimation(context,
                R.anim.slide_up));
    }

    public static void slideDown(Context context, View view) {
        view.startAnimation(AnimationUtils.loadAnimation(context,
                R.anim.slide_down));
        view.setVisibility(View.GONE);
    }
}
