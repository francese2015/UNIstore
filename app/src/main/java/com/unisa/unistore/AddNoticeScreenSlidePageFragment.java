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

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;
import com.rey.material.widget.RadioButton;
import com.rey.material.widget.Spinner;

import java.io.ByteArrayOutputStream;

public class AddNoticeScreenSlidePageFragment extends Fragment {
    /**
     * The argument key for the page number this fragment represents.
     */
    public static final String ARG_PAGE = "page";
    static final int CAMERA_INTENT = 1989;
    private static final String TAG = "ScreenSlidePageFragment";

    /**
     * The fragment's page number, which is set to the argument value for {@link #ARG_PAGE}.
     */
    private int mPageNumber;
    TextView titleText;
    private TextView authorsText;
    ImageView photo;
    private TextView descriptionText;
    private TextView priceText;

    private Spinner languageSpinner;
    private RadioGroup stateGroup;
    private CompoundButton rb_new_state, rb_like_new_state, rb_used_state;
    private Spinner spinner;

    private boolean isFirstLaunch = true;
    private AddNoticeScreenSlideActivity activity;

    /**
     * Factory method for this fragment class. Constructs a new fragment for the given page number.
     */
    public static AddNoticeScreenSlidePageFragment create(int pageNumber) {
        AddNoticeScreenSlidePageFragment fragment = new AddNoticeScreenSlidePageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public AddNoticeScreenSlidePageFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt(ARG_PAGE);

        activity = (AddNoticeScreenSlideActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout containing a title and body text.
        ViewGroup rootView = null;
        switch(mPageNumber) {
            case 0:
                rootView = prepareScreenSlidePageTitleAndAuthorsFragment(inflater, container);
                break;
            case 1:
                rootView = prepareScreenSlidePagePhotoFragment(inflater, container);
                break;
            case 2:
                rootView = prepareScreenSlidePageDescriptionFragment(inflater, container);
                break;
            case 3:
                rootView = prepareScreenSlidePageGeneralInfoFragment(inflater, container);
                break;
            case 4:
                rootView = prepareScreenSlidePagePriceAndStateFragment(inflater, container);
                break;
        }

        return rootView;
    }

    private ViewGroup prepareScreenSlidePageTitleAndAuthorsFragment(LayoutInflater inflater, ViewGroup container) {
        ViewGroup rootView = (ViewGroup) inflater
                .inflate(R.layout.fragment_screen_slide_page_title_and_author, container, false);

        titleText = (TextView) rootView.findViewById(R.id.book_title);
        titleText.setTag("title");
        authorsText = (TextView) rootView.findViewById(R.id.book_authors);
        authorsText.setTag("authors");
        setTextViewListener(authorsText, titleText);

        return rootView;
    }

    private ViewGroup prepareScreenSlidePagePhotoFragment(LayoutInflater inflater, ViewGroup container) {
        ViewGroup rootView = (ViewGroup) inflater
                .inflate(R.layout.fragment_screen_slide_page_photo, container, false);

        // This will disable the Soft Keyboard from appearing by default
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        photo = (ImageView) rootView.findViewById(R.id.book_preview_image);
        Button takePhotoButton = (Button) rootView.findViewById(R.id.take_photo_button);
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(photoIntent, CAMERA_INTENT);
            }
        });

        return rootView;
    }

    private ViewGroup prepareScreenSlidePageDescriptionFragment(LayoutInflater inflater, ViewGroup container) {
        ViewGroup rootView = (ViewGroup) inflater
                .inflate(R.layout.fragment_screen_slide_page_description, container, false);

        descriptionText = (TextView) rootView.findViewById(R.id.book_description);

        return rootView;
    }

    private ViewGroup prepareScreenSlidePageGeneralInfoFragment(LayoutInflater inflater, ViewGroup container) {
        ViewGroup rootView = (ViewGroup) inflater
                .inflate(R.layout.fragment_screen_slide_page_general_info, container, false);

        titleText = (TextView) rootView.findViewById(R.id.book_edition);

        return rootView;
    }

    private ViewGroup prepareScreenSlidePagePriceAndStateFragment(LayoutInflater inflater, ViewGroup container) {
        ViewGroup rootView = (ViewGroup) inflater
                .inflate(R.layout.fragment_screen_slide_page_price_and_state, container, false);

        priceText = (TextView) rootView.findViewById(R.id.book_price);

        priceText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 0) {
                    priceText.setHintTextColor(getResources().getColor(android.R.color.holo_red_light));
                    priceText.setHint(R.string.insertPrice);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                activity.setBookPrice(priceText.getText().toString());
            }
        });

        priceText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String replaced = priceText.getText().toString().replace(getString(R.string.euro_symbol), "");

                    String value = getString(R.string.euro_symbol) + replaced;

                    priceText.setText(value);

                    activity.setBookPrice(priceText.getText().toString());
                }
            }
        });

        stateGroup = (RadioGroup) rootView.findViewById(R.id.book_state);
        priceText = (TextView) rootView.findViewById(R.id.book_price);

        rb_new_state = (RadioButton) rootView.findViewById(R.id.new_state);
        rb_like_new_state = (RadioButton) rootView.findViewById(R.id.like_new_state);
        rb_used_state = (RadioButton) rootView.findViewById(R.id.used_state);

        CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "radioButton = " + buttonView.getText().toString());
                if(isChecked){
                    rb_new_state.setChecked(rb_new_state == buttonView);
                    rb_like_new_state.setChecked(rb_like_new_state == buttonView);
                    rb_used_state.setChecked(rb_used_state == buttonView);
                }
            }

        };

        View.OnClickListener clickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "radioButton = " + ((RadioButton)v).getText().toString());
            }
        };

        rb_new_state.setOnCheckedChangeListener(listener);
        rb_new_state.setOnClickListener(clickListener);
        rb_like_new_state.setOnCheckedChangeListener(listener);
        rb_like_new_state.setOnClickListener(clickListener);
        rb_used_state.setOnCheckedChangeListener(listener);
        rb_used_state.setOnClickListener(clickListener);
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "" + requestCode);
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CAMERA_INTENT && resultCode == getActivity().RESULT_OK) {
            Bitmap bp = (Bitmap) data.getExtras().get("data");
            Bitmap mealImageScaled = Bitmap.createScaledBitmap(bp, 300, 300
                    * bp.getHeight() / bp.getWidth(), true);

            showAndSaveScaledPhoto(bp);
        }
    }

    /*
	 * ParseQueryAdapter loads ParseFiles into a ParseImageView at whatever size
	 * they are saved. Since we never need a full-size image in our app, we'll
	 * save a scaled one right away.
	 */
    private void showAndSaveScaledPhoto(Bitmap bitmap) {

        // Resize photo from camera byte array
        Bitmap bookImage = bitmap;
        Bitmap bookImageScaled = Bitmap.createScaledBitmap(bookImage, 300, 300
                * bookImage.getHeight() / bookImage.getWidth(), false);

        // Override Android default landscape orientation and save portrait
//        Matrix matrix = new Matrix();
//        matrix.postRotate(90);
//        Bitmap rotatedScaledMealImage = Bitmap.createBitmap(bookImageScaled, 0,
//                0, bookImageScaled.getWidth(), bookImageScaled.getHeight(),
//                matrix, true);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        rotatedScaledMealImage.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        bookImageScaled.compress(Bitmap.CompressFormat.JPEG, 100, bos);

        photo.setImageBitmap(bookImageScaled);

        byte[] scaledData = bos.toByteArray();

        // Save the scaled image to Parse
        final ParseFile photoFile = new ParseFile("book_photo.jpg", scaledData);
        photoFile.saveInBackground(new SaveCallback() {

            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(getActivity(),
                            "Error saving: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                } else {
                    activity.setBookPhotoFile(photoFile);
                }
            }
        });
    }

    //    private void addPhotoToNotice(ParseFile photoFile) {
//        ((AddNoticeViewPagerActivity) getActivity()).getCurrentNotice().getLibro().setFileFoto(
//                photoFile);
//    }
//
    private void setTextViewListener(TextView... textView) {
        for(final TextView tmp : textView) {
            tmp.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(isFirstLaunch)
                        isFirstLaunch = false;
                    else {
                        checkIsCorrectlyCompile(tmp, hasFocus);
                        if (!hasFocus) {
                            if (tmp.getTag().equals("title")) {
                                activity.setBookTitle(tmp.getText().toString());
                            } else {
                                activity.setBookAuthors(tmp.getText().toString());
                            }
                        }
                    }
                }
            });

            tmp.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (count == 0) {
                        tmp.setHintTextColor(getResources().getColor(android.R.color.holo_red_light));
                        if (tmp.getTag().equals("title")) {
                            tmp.setHint(R.string.insertTitle);
                        } else {
                            tmp.setHint(R.string.insertAuthors);
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    ((AddNoticeScreenSlideActivity) getActivity()).setCanProceed(
                            checkIsCorrectlyCompile(tmp, true));
                }
            });
        }
    }

    private boolean checkIsCorrectlyCompile(TextView textView, boolean hasFocus) {
        if((titleText.getText().toString().isEmpty()) &&
                (authorsText.getText().toString().isEmpty()) && !hasFocus) {
            return  false;
        } else if(textView.getTag().equals("title") && textView.getText().toString().isEmpty() && !hasFocus) {
            return false;
        } else if(textView.getTag().equals("authors") && textView.getText().toString().isEmpty() && !hasFocus) {
            return false;
        } else if(!titleText.getText().toString().isEmpty() &&
                !authorsText.getText().toString().isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * Returns the page number represented by this fragment object.
     */
    public int getPageNumber() {
        return mPageNumber;
    }
}
