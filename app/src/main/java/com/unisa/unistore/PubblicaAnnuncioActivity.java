package com.unisa.unistore;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;
import com.unisa.unistore.android.IntentIntegrator;
import com.unisa.unistore.android.IntentResult;
import com.unisa.unistore.utilities.GetBookThumb;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PubblicaAnnuncioActivity extends ActionBarActivity implements View.OnClickListener {
    private String url1 = "https://www.googleapis.com/books/v1/volumes?q=isbn:";

    private Button scanBtn;
    private TextView authorText, titleText, descriptionText, dateText;

    private ImageView thumbView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pubblica_annuncio);

        Toolbar toolbar = (Toolbar) findViewById(R.id.fragment_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar supportActionBar = getSupportActionBar();
            supportActionBar.setDisplayShowTitleEnabled(false);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        scanBtn = (Button)findViewById(R.id.scan_button);
        scanBtn.setOnClickListener(this);

        authorText = (TextView)findViewById(R.id.book_authors);
        titleText = (TextView)findViewById(R.id.book_title);
        descriptionText = (TextView)findViewById(R.id.book_description);
        dateText = (TextView)findViewById(R.id.book_date);
        thumbView = (ImageView)findViewById(R.id.thumb);

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //retrieve result of scanning - instantiate ZXing object
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        //check we have a valid result
        if (scanningResult != null) {
            //get content from Intent Result
            String scanContent = scanningResult.getContents();
            //get format name of data scanned
            String scanFormat = scanningResult.getFormatName();

            if(scanContent!=null && scanFormat!=null && scanFormat.equalsIgnoreCase("EAN_13")){
                String bookSearchString = "https://www.googleapis.com/books/v1/volumes?"+
                        "q=isbn:"+scanContent+"&key=" + getString(R.string.google_isbn_search_api_key);

                ImageView barImg = (ImageView) findViewById(R.id.barcode_image);
                barImg.setVisibility(View.INVISIBLE);

                TextView barInstr = (TextView) findViewById(R.id.barcode_instruction);
                barInstr.setVisibility(View.INVISIBLE);

                Button scanBtn = (Button) findViewById(R.id.scan_button);
                scanBtn.setVisibility(View.INVISIBLE);

                new GetBookInfo().execute(bookSearchString);
            }
            else {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Not a valid scan!", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        else{
            //invalid scan data or scan canceled
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No book scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.scan_button){
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();
        }
    }

    static String convertStreamToString(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private class GetBookInfo extends AsyncTask<String, Void, String> {

        private ParseObject parseObject;
        private String title;
        private String authors;
        private String publishedDate;
        private String description;
        private String thumbnailURL;

        @Override
        protected String doInBackground(String... bookURLs) {
            String data = "";
            for (String bookSearchURL : bookURLs) {
                try {
                    URL url = new URL(bookSearchURL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    // Starts the query
                    conn.connect();
                    InputStream stream = conn.getInputStream();

                    data = convertStreamToString(stream);

                    stream.close();

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Log.v("SCAN", "bookBuilder: " + data);
            return data;
        }

        protected void onPostExecute(String result) {
            try {
                JSONObject resultObject = new JSONObject(result);
                JSONArray bookArray = resultObject.getJSONArray("items");
                JSONObject bookObject = bookArray.getJSONObject(0);
                JSONObject volumeObject = bookObject.getJSONObject("volumeInfo");
                parseObject = new ParseObject("Libri");

                try {
                    title = volumeObject.getString("title");
                    parseObject.put("titolo", title);
                    titleText.setText("TITLE: " + title);
                } catch(JSONException jse){
                    titleText.setText("");
                    jse.printStackTrace();
                }

                StringBuilder authorBuild = new StringBuilder("");
                try {
                    JSONArray authorArray = volumeObject.getJSONArray("authors");
                    for(int a = 0; a < authorArray.length(); a++) {
                        if(a > 0)
                            authorBuild.append(", ");
                        authors = authorArray.getString(a);
                        parseObject.put("autori", authors);
                        authorBuild.append(authors);
                    }
                    authorText.setText("AUTHOR(S): " + authorBuild.toString());
                }
                catch(JSONException jse) {
                    authorText.setText("");
                    jse.printStackTrace();
                }

                try {
                    publishedDate = volumeObject.getString("publishedDate");
                    parseObject.put("data_pubblicazione", publishedDate);
                    dateText.setText("PUBLISHED: " + publishedDate);
                } catch(JSONException jse){
                    dateText.setText("");
                    jse.printStackTrace();
                }

                try{
                    description = volumeObject.getString("description");
                    parseObject.put("descrizione", description);
                    descriptionText.setText("DESCRIPTION: " + description);
                } catch(JSONException jse){
                    descriptionText.setText("");
                    jse.printStackTrace();
                }

                try{
                    JSONObject imageInfo = volumeObject.getJSONObject("imageLinks");
                    thumbnailURL = imageInfo.getString("thumbnail");
                    parseObject.put("url_immagine_copertina", thumbnailURL);
                    new GetBookThumb(thumbView).execute(thumbnailURL);
                }
                catch(JSONException jse){
                    thumbView.setImageBitmap(null);
                    jse.printStackTrace();
                }

                parseObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e == null)
                            Log.d("Parse", "Salvataggio dati sul cloud avvenuto con successo!");
                        else {
                            Log.d("Parse", "Problemi durante il salvataggio dati sul cloud.");
                            e.getStackTrace();
                        }

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                titleText.setText("NOT FOUND");
                authorText.setText("");
                descriptionText.setText("");
                dateText.setText("");
                thumbView.setImageBitmap(null);
            }
        }
    }
}
