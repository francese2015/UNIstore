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
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.transition.Transition;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.unisa.unistore.adapter.RVAdapter;
import com.unisa.unistore.utilities.ImageUtilities;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class NoticeDetailActivity extends ActionBarActivity {
    private static final long ANIM_DURATION = 1500;
    private static final long LOLLIPOP_ANIM_DURATION = 1000;
    private static final TimeInterpolator sDecelerator = new DecelerateInterpolator();
    private static final TimeInterpolator sAccelerator = new AccelerateInterpolator();

    private static final int HEIGHT = 325;
    private static final int WIDTH = 325;
    private static final String PACKAGE_NAME = "com.unisa.unistore.adapter";

    static float sAnimatorScale = 2;

    private View bgBookImage;
    private View bgBookDescription;

    private TextView titoloLibro;
    private TextView autoriLibro;
    private TextView prezzoLibro;

    private ImageView fotoLibro;
    private com.nostra13.universalimageloader.core.ImageLoader imageLoader;
    private DisplayImageOptions options;
    private int mLeftDelta;
    private int mTopDelta;
    private float mWidthScale;
    private float mHeightScale;
    private ColorDrawable mBackground;
    private FrameLayout mTopLevelLayout;

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

        setupLayout();

        // Only run the animation if we're coming from the parent activity, not if
        // we're recreated automatically by the window manager (e.g., device rotation)
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP &&
                savedInstanceState == null) {
            //mShadowLayout = (ShadowLayout) findViewById(R.id.background_book_description_detail);
            mBackground = new ColorDrawable(getResources().getColor(R.color.book_detail_background));
            bgBookImage.setBackground(mBackground);

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

        bgBookImage = findViewById(R.id.background_book_image_detail);
        bgBookDescription = findViewById(R.id.background_book_description_detail);

        this.titoloLibro = (TextView) findViewById(R.id.book_title_detail);
        this.titoloLibro.setText(intent.getStringExtra(RVAdapter.BOOK_TITLE_MESSAGE));
        this.autoriLibro = (TextView) findViewById(R.id.book_authors_detail);
        this.autoriLibro.setText(intent.getStringExtra(RVAdapter.BOOK_AUTHORS_MESSAGE));
        this.prezzoLibro = (TextView) findViewById(R.id.book_price_detail);
        this.prezzoLibro.setText(intent.getStringExtra(RVAdapter.BOOK_PRICE_MESSAGE));
        this.fotoLibro = (ImageView) findViewById(R.id.book_image_detail);
        new ImageUtilities(this, fotoLibro).displayImage(intent.getStringExtra(RVAdapter.BOOK_IMAGE_URL_MESSAGE));
        resizeImage();
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
                animateRevealShow(bgBookImage);
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
                animateRevealHide(bgBookImage);
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
}
