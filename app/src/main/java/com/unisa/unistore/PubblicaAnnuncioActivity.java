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

import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
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

    private Button scanBtn, saveCloudBtn;
    private TextView authorText, titleText, descriptionText, dateText;
    private ImageView thumbView;

    private String title = "", authors = "", publishedDate = "", description = "", thumbnailURL = "";
    private ParseObject parseObject;

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

                scanBtn.setVisibility(View.INVISIBLE);

                saveCloudBtn = (Button) findViewById(R.id.save_on_cloud_button);
                saveCloudBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if (ParseUser.getCurrentUser() != null) {
                            parseObject = new ParseObject("Libri");

                            parseObject.put("titolo", title);
                            parseObject.put("autori", authors);
                            parseObject.put("data_pubblicazione", publishedDate);
                            parseObject.put("descrizione", description);
                            parseObject.put("url_immagine_copertina", thumbnailURL);

                            ParseACL groupACL = new ParseACL();
                            groupACL.setPublicReadAccess(true);
                            groupACL.setPublicWriteAccess(false);

                            parseObject.setACL(groupACL);
                            parseObject.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        Log.d("AnnuncioParse", "Salvataggio dell'annuncio sul cloud avvenuto con successo!");
                                        Toast toast = Toast.makeText(getApplicationContext(),
                                                "Annuncio salvato", Toast.LENGTH_SHORT);
                                        toast.show();
                                    } else {
                                        Log.d("AnnuncioParse", "Problemi durante il salvataggio dell'annuncio sul cloud.");
                                        e.getStackTrace();
                                        Toast toast = Toast.makeText(getApplicationContext(),
                                                "Errore durante il salvataggio dell'annuncio", Toast.LENGTH_SHORT);
                                        toast.show();
                                    }

                                }
                            });

                            finish();
                        } else {
                            Context context = getApplicationContext();
                            CharSequence text = getString(R.string.profile_title_logged_out);
                            int duration = Toast.LENGTH_LONG;

                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        }
                    }
                });
                saveCloudBtn.setVisibility(View.VISIBLE);
                saveCloudBtn.setClickable(false);

                new GetBookInfo().execute(bookSearchString);
            }
            else {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Scan non valido!", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        else{
            //invalid scan data or scan canceled
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Problemi con la scanning del codice ISBN!", Toast.LENGTH_SHORT);
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

                try {
                    title = volumeObject.getString("title");
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
                    dateText.setText("PUBLISHED: " + publishedDate);
                } catch(JSONException jse){
                    dateText.setText("");
                    jse.printStackTrace();
                }

                try{
                    description = volumeObject.getString("description");
                    descriptionText.setText("DESCRIPTION: " + description);
                } catch(JSONException jse){
                    descriptionText.setText("");
                    jse.printStackTrace();
                }

                try{
                    JSONObject imageInfo = volumeObject.getJSONObject("imageLinks");
                    thumbnailURL = imageInfo.getString("thumbnail");
                    new GetBookThumb(thumbView).execute(thumbnailURL);
                }
                catch(JSONException jse){
                    thumbView.setImageBitmap(null);
                    jse.printStackTrace();
                }

                saveCloudBtn.setClickable(true);
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
