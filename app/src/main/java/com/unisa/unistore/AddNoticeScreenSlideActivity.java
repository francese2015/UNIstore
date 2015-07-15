/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.unisa.unistore;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.rey.material.widget.SnackBar;
import com.unisa.unistore.model.Annuncio;

import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Demonstrates a "screen-slide" animation using a {@link ViewPager}. Because {@link ViewPager}
 * automatically plays such an animation when calling {@link ViewPager#setCurrentItem(int)}, there
 * isn't any animation-specific code in this sample.
 *
 * <p>This sample shows a "next" button that advances the user to the next step in a wizard,
 * animating the current screen out (to the left) and the next screen in (from the right). The
 * reverse animation is played when the user presses the "previous" button.</p>
 *
 * @see AddNoticeScreenSlidePageFragment
 */
public class AddNoticeScreenSlideActivity extends AppCompatActivity {
    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 5;
    private static final String TAG = "AddNoticeScreenSlideActivity";

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;
    private boolean canProceed = false;
    private Toolbar toolbar;
    private ActionBar supportActionBar;
    private SnackBar mSnackBar;
    private Annuncio notice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_slide);

        notice = new Annuncio();

        mSnackBar = new SnackBar(this);

        toolbar = (Toolbar) findViewById(R.id.fragment_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            supportActionBar = getSupportActionBar();
            supportActionBar.setDisplayShowTitleEnabled(false);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            //Nascondo la scritta UNIstore della toolbar
            toolbar.getChildAt(0).setVisibility(View.GONE);
        }

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new AddNoticeScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When changing pages, reset the action bar actions since they are dependent
                // on which page is currently active. An alternative approach is to have each
                // fragment expose actions itself (rather than the activity exposing actions),
                // but for simplicity, the activity provides the actions in this sample.
                invalidateOptionsMenu();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_screen_slide, menu);

        menu.findItem(R.id.action_previous).setEnabled(mPager.getCurrentItem() > 0);

        // Add either a "next" or "finish" button to the action bar, depending on which page
        // is currently selected.
        MenuItem item = menu.add(Menu.NONE, R.id.action_next, Menu.NONE,
                (mPager.getCurrentItem() == mPagerAdapter.getCount() - 1)
                        ? R.string.action_finish
                        : R.string.action_next);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "item = " + getString(item.getItemId()));
        switch (item.getItemId()) {
            case android.R.id.home:
                // Navigate "up" the demo structure to the launchpad activity.
                // See http://developer.android.com/design/patterns/navigation.html for more.
                NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
                return true;

            case R.id.action_previous:
            // Go to the previous step in the wizard. If there is no previous step,
            // setCurrentItem will do nothing.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
            return true;

            case R.id.action_next:
                if(!canProceed) {
                    mSnackBar.text(R.string.compile_fields)
                            .applyStyle(R.style.SnackBarMultiLine)
                            .actionText("HO CAPITO")
                            .actionClickListener(new SnackBar.OnActionClickListener() {
                                @Override
                                public void onActionClick(SnackBar snackBar, int i) {
                                    mSnackBar.dismiss();
                                }
                            });
                    mSnackBar.show(this);
                    return false;
                } else if(item.toString().equals(getString(R.string.action_finish))) {
                    saveOnCloud();
                }
                // Advance to the next step in the wizard. If there is no next step, setCurrentItem
                // will do nothing.
                mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setBookTitle(String bookTitle) {
        notice.getLibro().setTitoloLibro(bookTitle);
    }

    public void setBookAuthors(String bookAuthors) {
        notice.getLibro().addAutoriLibro(bookAuthors);
    }

    public void setBookPhotoFile(ParseFile bookPhotoFile) {
        notice.getLibro().setFileFoto(bookPhotoFile);
    }

    public void setBookPrice(String bookPrice) {
        String stringPrice = bookPrice.replace(getString(R.string.euro_symbol), "");
        if(!stringPrice.isEmpty())
        notice.setPrezzo(Double.parseDouble(stringPrice));
    }

    private void saveOnCloud() {
        if(ParseUser.getCurrentUser() != null) {
            ParseObject bookParseObject = new ParseObject("Libri");

            bookParseObject.put("titolo", notice.getLibro().getTitoloLibro());
            //TODO da aggiustare una volta che è stato sostituito su parse con una array
            bookParseObject.put("autori", notice.getLibro().getAutoriLibro().get(0));

            final String dataPubblicazioneAnnuncio = notice.getLibro().getDataPubblicazioneAnnuncio();
            if(dataPubblicazioneAnnuncio != null)
                bookParseObject.put("data_pubblicazione", dataPubblicazioneAnnuncio);

            final String descrizione = notice.getLibro().getDescrizione();
            if(descrizione != null)
                bookParseObject.put("descrizione", descrizione);

            final ParseFile fileFoto = notice.getLibro().getFileFoto();
            if(fileFoto != null)
                bookParseObject.put("file_foto", fileFoto);

//            bookParseObject.put("isbn", ISBNText.getText().toString());
            bookParseObject.put("lingua", "Italiano");
            // TODO da aggiustare anche qui
            // bookParseObject.put("lingua", languageSpinner.getSelectedItem().toString());
            bookParseObject.put("autore_annuncio", ParseUser.getCurrentUser().getObjectId());
            String bookState = "Nuovo";
//            if(((RadioButton)findViewById(stateGroup.getCheckedRadioButtonId())) != null) {
//                ((RadioButton)findViewById(stateGroup.getCheckedRadioButtonId())).getText().toString();
//            }
            bookParseObject.put("stato", bookState);
            final Number price = notice.getPrezzo();
            if(notice.getPrezzo() == null || price.intValue() <= 0) {
                mSnackBar.text("Inserire un prezzo di vendita")
                        .applyStyle(R.style.SnackBarMultiLine)
                        .actionText("HO CAPITO")
                        .actionClickListener(new SnackBar.OnActionClickListener() {
                            @Override
                            public void onActionClick(SnackBar snackBar, int i) {
                                mSnackBar.dismiss();
                            }
                        });
                mSnackBar.show(this);

                return;
            }
            bookParseObject.put("prezzo_annuncio", price);
            bookParseObject.put("stato_transazione", 0);


            ParseACL groupACL = new ParseACL();
            groupACL.setPublicReadAccess(true);
            groupACL.setPublicWriteAccess(true);
            //groupACL.setPublicWriteAccess(false);
            //groupACL.setWriteAccess(ParseUser.getCurrentUser(), true);

            bookParseObject.setACL(groupACL);
            bookParseObject.saveEventually(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        List<ParseObject> users_online = null;
                        try {
                            users_online = ParseQuery.getQuery("Users_Online").find();
                            for(ParseObject user_online : users_online) {
                                user_online.increment("contatore_nuovi_annunci");
                                if(user_online.getObjectId().equals(ParseUser.getCurrentUser().getObjectId()))
                                    user_online.save();
                                else
                                    user_online.saveInBackground();
                            }
                        } catch (ParseException e1) {
                            Log.d(TAG, "bookParseObject.saveInBackground()/Si è verificato un'errore: " + e.getMessage());
                            e1.printStackTrace();
                        }

                        Log.d("AnnuncioParse", "Salvataggio dell'annuncio sul cloud avvenuto con successo!");
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Annuncio salvato", Toast.LENGTH_SHORT);
                        toast.show();

                        finish();
                    } else {
                        Log.d("AnnuncioParse", "Problemi durante il salvataggio dell'annuncio sul cloud.");
                        e.getStackTrace();
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Errore durante il salvataggio dell'annuncio", Toast.LENGTH_SHORT);
                        toast.show();

                        finish();
                    }

                }
            });
        } else {
            Context context = getApplicationContext();
            CharSequence text = getString(R.string.profile_title_logged_out);
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "" + requestCode);
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == AddNoticeScreenSlidePageFragment.CAMERA_INTENT && resultCode == RESULT_OK) {
            Bitmap bp = (Bitmap) data.getExtras().get("data");
            Bitmap mealImageScaled = Bitmap.createScaledBitmap(bp, 300, 300
                    * bp.getHeight() / bp.getWidth(), true);

            ((AddNoticeScreenSlidePageFragment)((AddNoticeScreenSlidePagerAdapter) mPagerAdapter)
                    .getItem(1)).photo.setImageBitmap(mealImageScaled);
        }
    }

    public void setCanProceed(boolean canProceed) {
        this.canProceed = canProceed;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    /**
     * A simple pager adapter that represents 5 {@link AddNoticeScreenSlidePageFragment} objects, in
     * sequence.
     */
    private class AddNoticeScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public AddNoticeScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return AddNoticeScreenSlidePageFragment.create(position);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}
