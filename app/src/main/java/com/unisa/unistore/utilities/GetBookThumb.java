package com.unisa.unistore.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Daniele on 16/05/2015.
 */
public class GetBookThumb extends AsyncTask<String, Void, String> {
    private final ImageView thumbView;

    private Bitmap thumbImg;

    public GetBookThumb(ImageView thumbView) {
        this.thumbView = thumbView;
    }

    @Override
    protected String doInBackground(String... thumbURLs) {
        try{
            URL thumbURL = new URL(thumbURLs[0]);
            URLConnection thumbConn = thumbURL.openConnection();
            thumbConn.connect();

            InputStream thumbIn = thumbConn.getInputStream();
            BufferedInputStream thumbBuff = new BufferedInputStream(thumbIn);

            thumbImg = BitmapFactory.decodeStream(thumbBuff);
            thumbBuff.close();
            thumbIn.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    protected void onPostExecute(String result) {
        thumbView.setImageBitmap(thumbImg);
    }
}