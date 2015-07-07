package com.unisa.unistore;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.transition.Transition;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.unisa.unistore.adapter.RVAdapter;
import com.unisa.unistore.model.Annuncio;
import com.unisa.unistore.utilities.ImageUtilities;
import com.unisa.unistore.utilities.ParseUtilities;

import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class NoticeDetailActivity extends AppCompatActivity implements View.OnClickListener {
    private static final long ANIM_DURATION = 1500;
    private static final long LOLLIPOP_ANIM_DURATION = 1000;
    private static final TimeInterpolator sDecelerator = new DecelerateInterpolator();
    private static final TimeInterpolator sAccelerator = new AccelerateInterpolator();

    private static final int HEIGHT = 525;
    private static final int WIDTH = 525;
    private static final String PACKAGE_NAME = "com.unisa.unistore.adapter";
    public static final String NOTICE_AUTHOR_ID_TAG = "notice author";
    public static final String BOOK_ID = "book id";
    public static final String IS_NOTICE_AUTHOR = "is notice author?";

    static float sAnimatorScale = 2;

    private Button contactAuthorButton;
    private Button buyButton;

    private View backgroundFotoLibro;
    private View bgBookDescription;

    private String idLibro;
    private TextView titoloLibro;
    private TextView autoriLibro;
    private TextView prezzoLibro;
    private TextView descrizioneLibro;
    private TextView statoLibro;
    private TextView linguaLibro;
    private TextView ISBNLibro;
    private TextView autoreAnnuncio;
    private TextView statoTransazione;
    private int idStatoTransazione = -1;
    private ImageView fotoLibro;

    private com.nostra13.universalimageloader.core.ImageLoader imageLoader;
    private DisplayImageOptions options;
    private int mLeftDelta;
    private int mTopDelta;
    private float mWidthScale;
    private float mHeightScale;
    private ColorDrawable mBackground;
    private FrameLayout mTopLevelLayout;
    private String objectIdAutoreAnnuncio = "";

    private ParseObject parseObject = null;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // inside your activity (if you did not enable transitions in your theme)
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            // set an enter transition
            getWindow().setEnterTransition(new Explode());
            // set an exit transition
            getWindow().setExitTransition(new Explode());
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_notice_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.fragment_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar supportActionBar = getSupportActionBar();
            supportActionBar.setDisplayShowTitleEnabled(false);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        contactAuthorButton = (Button) findViewById(R.id.contact_the_author_button);
        contactAuthorButton.setOnClickListener(this);
        buyButton = (Button) findViewById(R.id.buy_button);
        buyButton.setOnClickListener(this);

        setupLayout();

        // Only run the animation if we're coming from the parent activity, not if
        // we're recreated automatically by the window manager (e.g., device rotation)
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP &&
                savedInstanceState == null) {
            //mShadowLayout = (ShadowLayout) findViewById(R.id.background_book_description_detail);
            mBackground = new ColorDrawable(getResources().getColor(R.color.book_detail_background));
            backgroundFotoLibro.setBackground(mBackground);

            Bundle bundle = getIntent().getExtras();

            final int thumbnailTop = bundle.getInt(PACKAGE_NAME + ".top");
            final int thumbnailLeft = bundle.getInt(PACKAGE_NAME + ".left");
            final int thumbnailWidth = bundle.getInt(PACKAGE_NAME + ".width");
            final int thumbnailHeight = bundle.getInt(PACKAGE_NAME + ".height");

            ViewTreeObserver observer = fotoLibro.getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    fotoLibro.getViewTreeObserver().removeOnPreDrawListener(this);

                    // Figure out where the thumbnail and full size versions are, relative
                    // to the screen and each other
                    int[] screenLocation = new int[2];
                    fotoLibro.getLocationOnScreen(screenLocation);
                    mLeftDelta = thumbnailLeft - screenLocation[0];
                    mTopDelta = thumbnailTop - screenLocation[1];

                    // Scale factors to make the large version the same size as the thumbnail
                    mWidthScale = (float) thumbnailWidth / fotoLibro.getWidth();
                    mHeightScale = (float) thumbnailHeight / fotoLibro.getHeight();

                    runEnterAnimation();

                    return true;
                }
            });
        } else {
            setupWindowAnimations();

            ViewCompat.setTransitionName(titoloLibro, getString(R.string.title_detail_transition_name));
            ViewCompat.setTransitionName(autoriLibro, getString(R.string.authors_detail_transition_name));
            ViewCompat.setTransitionName(prezzoLibro, getString(R.string.price_detail_transition_name));
            ViewCompat.setTransitionName(fotoLibro, getString(R.string.image_detail_transition_name));
        }

    }

    private void setupLayout() {
        Intent intent = getIntent();


        //this.bgBookDescription = findViewById(R.id.background_book_description_detail);
        this.idLibro = intent.getStringExtra(RVAdapter.BOOK_ID_MESSAGE);
        doParseQuery();
        this.titoloLibro = (TextView) findViewById(R.id.book_title_detail);
        this.titoloLibro.setText(intent.getStringExtra(RVAdapter.BOOK_TITLE_MESSAGE));
        this.autoriLibro = (TextView) findViewById(R.id.book_authors_detail);
        this.autoriLibro.setText(intent.getStringExtra(RVAdapter.BOOK_AUTHORS_MESSAGE));
        this.prezzoLibro = (TextView) findViewById(R.id.book_price_detail);
        this.prezzoLibro.setText(intent.getStringExtra(RVAdapter.BOOK_PRICE_MESSAGE));
        this.descrizioneLibro = (TextView) findViewById(R.id.book_description_detail);
        this.statoLibro = (TextView) findViewById(R.id.book_state_detail);
        this.linguaLibro = (TextView) findViewById(R.id.book_language_detail);
        this.ISBNLibro = (TextView) findViewById(R.id.book_isbn_detail);
        this.backgroundFotoLibro = findViewById(R.id.background_book_image_detail);
        this.autoreAnnuncio = (TextView) findViewById(R.id.notice_author_detail);
        this.fotoLibro = (ImageView) findViewById(R.id.book_image_detail);
        new ImageUtilities(this, fotoLibro).displayImage(intent.getStringExtra(RVAdapter.BOOK_IMAGE_URL_MESSAGE));

        resizeImage();
    }

    private void doParseQuery() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Libri");
        query.whereEqualTo("objectId", idLibro);

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> scoreList, ParseException e) {
                if (e == null) {
                    if (scoreList.size() > 0) {
                        parseObject = scoreList.get(0);
                        String objectIdAcquirente = parseObject.getString("id_acquirente");
                        objectIdAutoreAnnuncio = parseObject.getString("autore_annuncio");
                        boolean isAcquirente = objectIdAcquirente != null ? objectIdAcquirente.equals(ParseUser.getCurrentUser().getObjectId()) : false;
                        boolean isAutoreAnnuncio = objectIdAutoreAnnuncio.equals(ParseUser.getCurrentUser().getObjectId());
                        statoTransazione = (TextView) findViewById(R.id.transaction_state_detail);
                        idStatoTransazione = parseObject.getNumber("stato_transazione") != null ? parseObject.getNumber("stato_transazione").intValue() : -1;

                        if(idStatoTransazione == Annuncio.TRANSAZIONE_INIZIO && isAutoreAnnuncio) {
                            buyButton.setVisibility(View.GONE);
                            contactAuthorButton.setVisibility(View.GONE);

                            statoTransazione.setVisibility(View.VISIBLE);
                            findViewById(R.id.transaction_state_detail_description).setVisibility(View.VISIBLE);
                            findViewById(R.id.transaction_state_detail_divider).setVisibility(View.VISIBLE);
                            statoTransazione.setText(R.string.notice_author_start_transaction);
                        } else if(idStatoTransazione == Annuncio.TRANSAZIONE_IN_TRATTATIVA) {
                            statoTransazione.setVisibility(View.VISIBLE);
                            findViewById(R.id.transaction_state_detail_description).setVisibility(View.VISIBLE);
                            findViewById(R.id.transaction_state_detail_divider).setVisibility(View.VISIBLE);

                            if(isAutoreAnnuncio) {
                                buyButton.setText(R.string.decide);
                                statoTransazione.setText(R.string.notice_author_in_negotiation_transaction);
                            } else if(isAcquirente) {
                                buyButton.setEnabled(false);
                                statoTransazione.setText(R.string.buyer_in_negotiation_transaction);
                            }

                        } else if(idStatoTransazione == Annuncio.TRANSAZIONE_ACQUISTO_CONCORDATO) {
                            buyButton.setEnabled(true);
                            buyButton.setText(R.string.conclude_transaction);

                            statoTransazione.setVisibility(View.VISIBLE);
                            findViewById(R.id.transaction_state_detail_description).setVisibility(View.VISIBLE);
                            findViewById(R.id.transaction_state_detail_divider).setVisibility(View.VISIBLE);
                            if(isAutoreAnnuncio) {
                                statoTransazione.setText(R.string.notice_author_accepted_purchase);
                            } else if(isAcquirente) {
                                statoTransazione.setText(R.string.buyer_accepted_purchase);
                            }
                        } else if(idStatoTransazione == Annuncio.TRANSAZIONE_FINE) {
                            buyButton.setVisibility(View.GONE);
                            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT, 2.0f);
                            contactAuthorButton.setLayoutParams(param);
                            statoTransazione.setVisibility(View.VISIBLE);
                            findViewById(R.id.transaction_state_detail_description).setVisibility(View.VISIBLE);
                            findViewById(R.id.transaction_state_detail_divider).setVisibility(View.VISIBLE);
                            statoTransazione.setText(R.string.transaction_concluded);
                        } else if(idStatoTransazione == -1) {
                            //ancora nessuno vuole acquistare il libro
                        }

                        if (parseObject != null && objectIdAutoreAnnuncio.length() == 10) {
                            try {
                                List<ParseUser> user = ParseUser.getQuery().whereEqualTo("objectId", objectIdAutoreAnnuncio).find();
                                if(user.size() > 0)
                                    autoreAnnuncio.setText(user.get(0).getString("name"));
                                else
                                    autoreAnnuncio.setText(R.string.not_found);
                            } catch (ParseException e1) {
                                e1.printStackTrace();
                            }
                        } else
                            autoreAnnuncio.setText(R.string.not_found);

                        if (parseObject != null && parseObject.getString("descrizione").length() > 18) {
                            descrizioneLibro.setText(parseObject.getString("descrizione"));
                            descrizioneLibro.setVisibility(View.VISIBLE);
                            findViewById(R.id.book_description_detail_description).setVisibility(View.VISIBLE);
                            findViewById(R.id.book_description_detail_divider).setVisibility(View.VISIBLE);
                        }

                        if (parseObject != null && parseObject.getString("stato") != null) {
                            statoLibro.setText(parseObject.getString("stato"));
                            statoLibro.setVisibility(View.VISIBLE);
                            findViewById(R.id.book_state_detail_description).setVisibility(View.VISIBLE);
                            findViewById(R.id.book_state_detail_divider).setVisibility(View.VISIBLE);
                        }

                        if (parseObject != null && parseObject.getString("lingua") != null) {
                            linguaLibro.setText(parseObject.getString("lingua"));
                            linguaLibro.setVisibility(View.VISIBLE);
                            findViewById(R.id.book_language_detail_description).setVisibility(View.VISIBLE);
                            findViewById(R.id.book_language_detail_divider).setVisibility(View.VISIBLE);
                        }

                        if (parseObject != null && parseObject.getString("isbn") != null && parseObject.getString("isbn").length() > 10) {
                            ISBNLibro.setText(parseObject.getString("isbn"));
                            ISBNLibro.setVisibility(View.VISIBLE);
                            findViewById(R.id.book_isbn_detail_description).setVisibility(View.VISIBLE);
                            findViewById(R.id.book_isbn_detail_divider).setVisibility(View.VISIBLE);
                        }

                        Log.d("Annunci", "Trovati " + scoreList.size() + " annunci");
                    } else {
                        Log.d("Annunci", "Errore: " + e.getMessage());
                    }
                }
            }
        });
    }

    private void setupWindowAnimations() {
        setupEnterAnimations();
        setupExitAnimations();
    }

    private void resizeImage() {
        fotoLibro.getLayoutParams().height += HEIGHT;
        fotoLibro.getLayoutParams().width += WIDTH;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupEnterAnimations() {
        Transition enterTransition = getWindow().getSharedElementEnterTransition();
        enterTransition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                animateRevealShow(backgroundFotoLibro);
            }

            @Override
            public void onTransitionCancel(Transition transition) {
            }

            @Override
            public void onTransitionPause(Transition transition) {
            }

            @Override
            public void onTransitionResume(Transition transition) {
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupExitAnimations() {
        Transition sharedElementReturnTransition = getWindow().getSharedElementReturnTransition();
        //sharedElementReturnTransition.setStartDelay(LOLLIPOP_ANIM_DURATION);

        Transition returnTransition = getWindow().getReturnTransition();
        returnTransition.setDuration(LOLLIPOP_ANIM_DURATION/4);
        returnTransition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                animateRevealHide(backgroundFotoLibro);
            }

            @Override
            public void onTransitionEnd(Transition transition) {
            }

            @Override
            public void onTransitionCancel(Transition transition) {
            }

            @Override
            public void onTransitionPause(Transition transition) {
            }

            @Override
            public void onTransitionResume(Transition transition) {
            }
        });
    }

    /**
     * The enter animation scales the picture in from its previous thumbnail
     * size/location, colorizing it in parallel. In parallel, the background of the
     * activity is fading in. When the pictue is in place, the text description
     * drops down.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void runEnterAnimation() {
        final long duration = (long) (ANIM_DURATION * sAnimatorScale);

        // Set starting values for properties we're going to animate. These
        // values scale and position the full size version down to the thumbnail
        // size/location, from which we'll animate it back up
        fotoLibro.setPivotX(0);
        fotoLibro.setPivotY(0);
        fotoLibro.setTranslationX(mLeftDelta);
        fotoLibro.setTranslationY(mTopDelta);
        final Animation anim = new ScaleAnimation(
                0.6f, 1f, // Start and end values for the X axis scaling
                0.6f, 1f); // Start and end values for the Y axis scaling
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(250);

        // We'll fade the text in later
        titoloLibro.setAlpha(0);

        // Animate scale and translation to go from thumbnail to full size
        fotoLibro.animate().setDuration(duration / 2).
                translationX(0).translationY(0).
                setInterpolator(sDecelerator).
                withEndAction(new Runnable() {
                    public void run() {
                        fotoLibro.startAnimation(anim);
                        // Animate the description in after the image animation
                        // is done. Slide and fade the text in from underneath
                        // the picture.
                        titoloLibro.setTranslationY(-titoloLibro.getHeight());
                        titoloLibro.animate().setDuration(duration/2).
                                translationY(0).alpha(1).
                                setInterpolator(sDecelerator);
                    }
                });

        // Fade in the black background
        ObjectAnimator bgAnim = ObjectAnimator.ofInt(mBackground, "alpha", 0, 255);
        bgAnim.setDuration(duration);
        bgAnim.start();

        // Animate a color filter to take the image from grayscale to full color.
        // This happens in parallel with the image scaling and moving into place.
        ObjectAnimator colorizer = ObjectAnimator.ofFloat(this,
                "saturation", 0, 1);
        colorizer.setDuration(duration);
        colorizer.start();

        // Animate a drop-shadow of the image
        /*
        ObjectAnimator shadowAnim = ObjectAnimator.ofFloat(mShadowLayout, "shadowDepth", 0, 1);
        shadowAnim.setDuration(duration);
        shadowAnim.start();
        */
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void animateRevealShow(View viewRoot) {
        int cx = (viewRoot.getLeft() + viewRoot.getRight()) / 2;
        int cy = (viewRoot.getTop() + viewRoot.getBottom()) / 2;
        int finalRadius = Math.max(viewRoot.getWidth(), viewRoot.getHeight());

        Animator anim = ViewAnimationUtils.createCircularReveal(viewRoot, cx, cy, 0, finalRadius);
        viewRoot.setVisibility(View.VISIBLE);
        anim.setDuration(LOLLIPOP_ANIM_DURATION);
        anim.start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void animateRevealHide(final View viewRoot) {
        int cx = (viewRoot.getLeft() + viewRoot.getRight()) / 2;
        int cy = (viewRoot.getTop() + viewRoot.getBottom()) / 2;
        int initialRadius = viewRoot.getWidth();

        Animator anim = ViewAnimationUtils.createCircularReveal(viewRoot, cx, cy, initialRadius, 0);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                viewRoot.setVisibility(View.INVISIBLE);
            }
        });
        anim.setDuration(LOLLIPOP_ANIM_DURATION/5);
        anim.start();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Get item selected and deal with it
        switch (item.getItemId()) {
            case android.R.id.home:
                //called when the up affordance/carat in actionbar is pressed
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.contact_the_author_button) {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra(NOTICE_AUTHOR_ID_TAG, objectIdAutoreAnnuncio);
            startActivity(intent);
        } else if(v.getId() == R.id.buy_button) {
            //Intent intent = new Intent(this, ChatActivity.class);
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Libri");
            query.whereEqualTo("objectId", idLibro);

            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> scoreList, ParseException e) {
                    if (e == null) {
                        if (scoreList.size() > 0) {
                            parseObject = scoreList.get(0);
                            if (parseObject != null) {
                                try {
                                    if(idStatoTransazione == Annuncio.TRANSAZIONE_INIZIO) {
                                        final ParseUser autore = ParseUser.getQuery().whereEqualTo("objectId", parseObject.get("autore_annuncio")).find().get(0);
                                        parseObject.put("stato_transazione", Annuncio.TRANSAZIONE_IN_TRATTATIVA);
                                        parseObject.put("id_acquirente", ParseUser.getCurrentUser().getObjectId());

                                        parseObject.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    ParseUtilities.sendNotificationToNoticeAuthor(
                                                            autore, ParseUser.getCurrentUser().get("name") + " " +
                                                                    getString(R.string.request_for_purchase_without_name));
                                                    buyButton.setEnabled(false);

                                                    statoTransazione.setVisibility(View.VISIBLE);
                                                    findViewById(R.id.transaction_state_detail_description).setVisibility(View.VISIBLE);
                                                    findViewById(R.id.transaction_state_detail_divider).setVisibility(View.VISIBLE);
                                                    statoTransazione.setText(R.string.buyer_in_negotiation_transaction);

                                                    Toast.makeText(NoticeDetailActivity.this, R.string.done, Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Log.e("NoticeDetailActivity", e.getMessage());
                                                    Toast.makeText(NoticeDetailActivity.this, R.string.contact_administrator, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    } else if(idStatoTransazione == Annuncio.TRANSAZIONE_IN_TRATTATIVA) {
                                        final ParseUser acquirente = ParseUser.getQuery().whereEqualTo("objectId", parseObject.get("id_acquirente")).find().get(0);
                                        parseObject.put("stato_transazione", Annuncio.TRANSAZIONE_ACQUISTO_CONCORDATO);

                                        parseObject.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    ParseUtilities.sendNotificationToNoticeAuthor(acquirente,
                                                            getString(R.string.accepted_purchase));
                                                    buyButton.setText(R.string.conclude_transaction);

                                                    statoTransazione.setVisibility(View.VISIBLE);
                                                    findViewById(R.id.transaction_state_detail_description).setVisibility(View.VISIBLE);
                                                    findViewById(R.id.transaction_state_detail_divider).setVisibility(View.VISIBLE);
                                                    statoTransazione.setText(getString(R.string.notice_author_purchase_agreement_transaction) + " " + acquirente.get("name"));

                                                    Toast.makeText(NoticeDetailActivity.this, R.string.done, Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Log.e("NoticeDetailActivity", e.getMessage());
                                                    Toast.makeText(NoticeDetailActivity.this, R.string.contact_administrator, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    } else if(idStatoTransazione == Annuncio.TRANSAZIONE_ACQUISTO_CONCORDATO) {
                                        Intent intent = new Intent(NoticeDetailActivity.this, ConcludeTransactionActivity.class);

                                        boolean isAutoreAnnuncio = objectIdAutoreAnnuncio.equals(ParseUser.getCurrentUser().getObjectId());
                                        if(isAutoreAnnuncio)
                                            intent.putExtra(NoticeDetailActivity.IS_NOTICE_AUTHOR, true);
                                        else
                                            intent.putExtra(NoticeDetailActivity.IS_NOTICE_AUTHOR, false);

                                        intent.putExtra(NoticeDetailActivity.BOOK_ID, parseObject.getObjectId());

                                        startActivity(intent);
                                    }
                                } catch (ParseException e1) {
                                    e1.printStackTrace();
                                }
                                finish();
                            } else {
                                Log.e("NoticeDetailActivity", "parseObject è null");
                                Toast.makeText(NoticeDetailActivity.this, R.string.contact_administrator, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            /*
            intent.putExtra(NOTICE_AUTHOR_ID_TAG, authorId);
            startActivity(intent);
            */
            });
        }
    }

}
