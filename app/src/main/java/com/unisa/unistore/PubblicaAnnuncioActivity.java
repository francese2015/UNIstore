package com.unisa.unistore;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.rey.material.widget.RadioButton;
import com.unisa.unistore.android.IntentIntegrator;
import com.unisa.unistore.android.IntentResult;
import com.unisa.unistore.utilities.ImageUtilities;
import com.unisa.unistore.utilities.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Calendar;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PubblicaAnnuncioActivity extends ActionBarActivity
        implements View.OnClickListener, NumberPicker.OnValueChangeListener {
    private static final int MIN_YEAR = 1900;
    private static final int MAX_YEAR = Calendar.getInstance().get(Calendar.YEAR);

    private Button scanBtn, saveCloudBtn;

    private TextView authorText, titleText, descriptionText, dateText;
    private ImageView thumbView;

    private com.rey.material.widget.Spinner spinner;
    private DatePicker datePicker;
    private Calendar calendar;
    private TextView dateView;
    private int year, month, day;


    private String title = "", authors = "", publishedDate = "", description = "", thumbnailURL = "";
    private ParseObject parseObject;
    private CompoundButton rb_new_state, rb_like_new_state, rb_used_state;

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
        dateText.setEnabled(true);
        dateText.setInputType(InputType.TYPE_NULL);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        dateText.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //show();
                return false;
            }

        });

        thumbView = (ImageView)findViewById(R.id.book_image);

        rb_new_state = (RadioButton) findViewById(R.id.new_state);
        rb_like_new_state = (RadioButton) findViewById(R.id.like_new_state);
        rb_used_state = (RadioButton) findViewById(R.id.used_state);

        CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    rb_new_state.setChecked(rb_new_state == buttonView);
                    rb_like_new_state.setChecked(rb_like_new_state == buttonView);
                    rb_used_state.setChecked(rb_used_state == buttonView);
                }
            }

        };

        rb_new_state.setOnCheckedChangeListener(listener);
        rb_like_new_state.setOnCheckedChangeListener(listener);
        rb_used_state.setOnCheckedChangeListener(listener);

        spinner = (com.rey.material.widget.Spinner) findViewById(R.id.book_language_spinner);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, R.layout.row_spn, getResources().getStringArray(R.array.language_array));
        spinnerAdapter.setDropDownViewResource(R.layout.row_spn_dropdown);
        spinner.setAdapter(spinnerAdapter);

/*
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.language_array, R.layout.spinner_row);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_list);
        spinner.setAdapter(spinnerAdapter);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.language_array, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the spinnerAdapter to the spinner
        spinner.setAdapter(spinnerAdapter);
        */

    }

    public void show()
    {
        final Dialog dialog = new Dialog(PubblicaAnnuncioActivity.this);
        dialog.setTitle(getString(R.string.choose_published_year_hint));
        dialog.setContentView(R.layout.dialog);
        Button ok = (Button) dialog.findViewById(R.id.button1);
        Button cancel = (Button) dialog.findViewById(R.id.button2);

        final NumberPicker numberPicker = (NumberPicker) dialog.findViewById(R.id.numberPicker1);
        numberPicker.setMaxValue(year);
        numberPicker.setMinValue(1900);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setValue(year);
        numberPicker.setOnValueChangedListener(this);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateText.setText(String.valueOf(numberPicker.getValue()));
                dialog.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
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
                new GetBookInfo().execute(bookSearchString);
                saveCloudBtn.setVisibility(View.VISIBLE);
                saveCloudBtn.setClickable(false);
                findViewById(R.id.book_info).setVisibility(View.VISIBLE);
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
        if(v.getId() == R.id.scan_button){
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();
        } else if(v.getId() == R.id.save_on_cloud_button) {
            
        }
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        return;
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

                    data = Utilities.convertStreamToString(stream);

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
                    new ImageUtilities(PubblicaAnnuncioActivity.this, thumbView).displayImage(thumbnailURL);
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
