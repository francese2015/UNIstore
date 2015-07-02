package com.unisa.unistore;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ui.ParseOnLoginSuccessListener;
import com.unisa.unistore.utilities.Utilities;

import java.util.List;

/**
 * Created by Daniele on 23/06/2015.
 */
public class AccountManagementActivity extends ActionBarActivity implements View.OnClickListener,
        ParseOnLoginSuccessListener {

    private boolean flagError = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_management);

        findViewById(R.id.logout_btn).setOnClickListener(this);
        findViewById(R.id.delete_account_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final Intent intent = getIntent();

        if(v.getId() == R.id.logout_btn){
            if (Utilities.isUserAuthenticated()) {
                ParseUser.logOut();

                Context context = getApplicationContext();
                CharSequence text = getString(R.string.is_logout);
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                intent.putExtra("action", "logout");
                setResult(RESULT_OK, intent);
                finish();
            }
        } else if(v.getId() == R.id.delete_account_btn){
            if (Utilities.isUserAuthenticated()) {
                final ParseUser currentUser = ParseUser.getCurrentUser();
                final String id = currentUser.getObjectId();
                final Context context = getApplicationContext();
                final int duration = Toast.LENGTH_LONG;

                ParseQuery<ParseObject> query = ParseQuery.getQuery("Libri");
                query.whereEqualTo("autore_annuncio", id);

                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> list, ParseException e) {
                        if (e == null) {
                            int size = list.size();
                            for (int i = 0; i < size; i++) {
                                list.get(i).deleteInBackground(new DeleteCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        CharSequence text = "";

                                        if (e == null) {
                                            Log.d("Account", getString(R.string.account_deleted));
                                            text = getString(R.string.account_deleted);
                                            ParseUser.logOut();

                                            intent.putExtra("action", "delete");
                                            setResult(RESULT_OK, intent);
                                            finish();
                                        } else {
                                            Toast.makeText(context, R.string.contact_administrator, duration).show();
                                            Log.d("CancellazioneAnnunci", "Errore: " + e.getMessage());
                                            flagError = true;
                                        }

                                        Toast toast = Toast.makeText(context, text, duration);
                                        toast.show();
                                    }
                                });
                            }
                            //TODO da rimettere la negazione
                            if (flagError) {
                                currentUser.deleteInBackground(new DeleteCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        CharSequence text = "";

                                        if (e == null) {
                                            Log.d("Account", getString(R.string.account_deleted));
                                            text = getString(R.string.account_deleted);
                                            ParseUser.logOut();

                                            intent.putExtra("action", "delete");
                                            setResult(RESULT_OK, intent);
                                            finish();
                                        } else {
                                            Log.d("Account", "Errore: " + e.getMessage());
                                            text = getString(R.string.message_error);
                                        }

                                        Toast toast = Toast.makeText(context, text, duration);
                                        toast.show();
                                    }
                                });
                            } else {
                                Toast.makeText(context, R.string.contact_administrator, duration).show();
                                Log.d("CancellazioneAnnunci", "Errore: cancellazione dell'account non riuscita");
                                return;
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onLoginSuccess() {

    }
}
