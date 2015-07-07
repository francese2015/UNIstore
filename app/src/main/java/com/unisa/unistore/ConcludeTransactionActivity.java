package com.unisa.unistore;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.unisa.unistore.model.Annuncio;

import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static android.nfc.NdefRecord.createMime;

/**
 * Created by Daniele on 01/07/2015.
 */
public class ConcludeTransactionActivity extends AppCompatActivity
        implements NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback {
    private static final int WAIT_TIME = 2000;
    private static final int MESSAGE_SENT = 1;
    NfcAdapter mNfcAdapter;
    TextView textView;
    private ImageView imageView;
    private String idAutoreAnnuncio;
    private boolean canGoBack = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conclude_transaction_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.fragment_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar supportActionBar = getSupportActionBar();
            supportActionBar.setDisplayShowTitleEnabled(false);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        idAutoreAnnuncio = getIntent().getStringExtra(PurchaseAgreementFragment.NOTICE_AUTHOR_ID_TAG);
        textView = (TextView) findViewById(R.id.nfc_instruction);
        imageView = (ImageView) findViewById(R.id.ok_image);
        // Check for available NFC Adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // Register callback
        mNfcAdapter.setNdefPushMessageCallback(this, this);
        mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        String text = "";
        if(getIntent().getStringExtra(NoticeDetailActivity.BOOK_ID) != null)
            text = doParseQuery();

        NdefMessage msg = new NdefMessage(
                new NdefRecord[] { createMime(
                        "application/vnd.com.example.android.beam", text.getBytes())
                        /**
                         * The Android Application Record (AAR) is commented out. When a device
                         * receives a push with an AAR in it, the application specified in the AAR
                         * is guaranteed to run. The AAR overrides the tag dispatch system.
                         * You can add it back in to guarantee that this
                         * activity starts when receiving a beamed message. For now, this code
                         * uses the tag dispatch system.
                         */
                        //,NdefRecord.createApplicationRecord("com.example.android.beam")
                });

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(R.string.wait);
            }
        });

        return msg;
    }

    private String doParseQuery() {
        String result = "";
        String idLibro = getIntent().getStringExtra(NoticeDetailActivity.BOOK_ID);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Libri");
        query.whereEqualTo("objectId", idLibro);
        try {
            if (query.count() <= 0) {
                Log.d("ConcludeTransaction", "Nessun libro corrisponde all'id " + idLibro);
            } else {
                result = idLibro;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Log.d("Annunci", "Errore: " + e.getMessage());
        }

        if (result.isEmpty())
            result = getString(R.string.contact_administrator);

        return result;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            textView.setText(R.string.wait);
            processIntent(getIntent());
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub

                textView.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);

                try {
                    synchronized (this) {
                        wait(WAIT_TIME);
                        Intent intent = new Intent(ConcludeTransactionActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }

                Toast.makeText(ConcludeTransactionActivity.this, R.string.done, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        final NdefMessage msg = (NdefMessage) rawMsgs[0];

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Libri");
        String idLibro = new String(msg.getRecords()[0].getPayload());

        if (idLibro.equals("")) {
            Toast.makeText(this, R.string.nfc_error, Toast.LENGTH_LONG);
            intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        query.whereEqualTo("objectId", idLibro);

        try {
            List<ParseObject> list = query.find();

            if (list.size() <= 0) {
                Toast.makeText(this, R.string.contact_administrator, Toast.LENGTH_LONG);
                return;
            } else {
                ParseObject parseObject = list.get(0);
                if (parseObject != null) {
                    int idStatoTransazione = parseObject.getNumber("stato_transazione") != null ? parseObject.getNumber("stato_transazione").intValue() : -1;
                    if (idStatoTransazione == Annuncio.TRANSAZIONE_ACQUISTO_CONCORDATO) {
                        parseObject.put("stato_transazione", Annuncio.TRANSAZIONE_FINE);

                        parseObject.saveInBackground(new SaveCallback() {
                            @TargetApi(Build.VERSION_CODES.KITKAT)
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    //if(intent.getBooleanExtra(NoticeDetailActivity.IS_NOTICE_AUTHOR, false))

                                    // record 0 contains the MIME type, record 1 is the AAR, if present
                                    //textView.setText(new String(msg.getRecords()[0].getPayload()));
                                    textView.setVisibility(View.GONE);
                                    imageView.setVisibility(View.VISIBLE);

                                    try {
                                        synchronized (this) {
                                            mNfcAdapter.disableReaderMode(ConcludeTransactionActivity.this);
                                            wait(WAIT_TIME);
                                            Intent intent = new Intent(ConcludeTransactionActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }

                                    } catch (InterruptedException e1) {
                                        e1.printStackTrace();
                                    }

                                    Toast.makeText(ConcludeTransactionActivity.this, R.string.done, Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.e("ConcludeTransaction", e.getMessage());
                                    Toast.makeText(ConcludeTransactionActivity.this, R.string.contact_administrator, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(ConcludeTransactionActivity.this, "Il libro risulta gia' venduto!", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                } else {
                    Log.e("ConcludeTransaction", "parseObject è null");
                    Toast.makeText(ConcludeTransactionActivity.this, R.string.contact_administrator, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        if (!canGoBack) {
            Toast.makeText(this, R.string.wait, Toast.LENGTH_LONG);
            return;
        }

        super.onBackPressed();
        finish();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}