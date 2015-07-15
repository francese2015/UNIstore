package com.unisa.unistore;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.rey.material.widget.SnackBar;
import com.unisa.unistore.adapter.RVAdapter;
import com.unisa.unistore.model.ListaAnnunci;
import com.unisa.unistore.utilities.NetworkUtilities;
import com.unisa.unistore.utilities.Utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniele on 07/07/2015.
 */
public abstract class NoticeFragment extends Fragment {
    private static final int QUERY_LIMIT = 6;

    public static final String INPUT_TYPE_MESSAGE = "input type";
    private static final String TAG = "NoticeFragment";

    private static final int FAB_THRESHOLD = 1;

    Activity activity;

    private ActionBar supportActionBar;

    private boolean isRefreshing = false;
    TextView no_connection_message;

    FrameLayout wheel;
    private RecyclerView recyclerView;
    private LinearLayoutManager mLayoutManager;
    SwipeRefreshLayout mSwipeRefreshLayout;
    RVAdapter adapter;

    private boolean loading = true;
    boolean lastNoticeReached = false;
    private int pastVisiblesItems, visibleItemCount, totalItemCount;

    private Handler handler = new Handler();

    SnackBar mSnackBar;

    FloatingActionsMenu fab_menu;
    private FloatingActionButton fab_camera, fab_form;
    //private FloatingActionButton fab_line;

    private static NoticeFragment mInst;

    private static int queryCnt = 0;

    private int lastNoticesCnt = 0;

    ListaAnnunci LA;

    public static NoticeFragment instance() {
        return mInst;
    }

    public abstract void downloadAnnunci(boolean isRefreshing);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        activity = getActivity();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LA = new ListaAnnunci();

        queryCnt = 0;

        mSnackBar = new SnackBar(activity);

        no_connection_message = (TextView) getActivity().findViewById(R.id.no_connection_message);
    }


    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);
        // update the actionbar to show the up carat/affordance
        if(supportActionBar != null)
            supportActionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onStart() {
        super.onStart();

        mInst = this;
    }

    @Override
    public void onResume() {
        super.onResume();

        Utilities.setClicked(false);
        //refresh();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    void setFAB() {

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fab_menu = (FloatingActionsMenu) getActivity().findViewById(R.id.fab_menu);
                fab_menu.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        /*
                         * Se fab_menu e' aperto, quando si clicca su qualsiasi parte dello schermo
                         * (che non sia un elemento appartenente a fab_menu), lo stesso viene chiuso.
                         */
                        if (fab_menu.isExpanded()) {
                            fab_menu.collapse();
                            return true;
                        }

                        return false;
                    }

                });

                fab_camera = (FloatingActionButton) getActivity().findViewById(R.id.fab_camera);
                fab_camera.setTag("camera");
                fab_form = (FloatingActionButton) getActivity().findViewById(R.id.fab_form);
                fab_form.setTag("form");
                setFABListener(fab_form, fab_camera);
            }
        });


        /*
        fab_line = (FloatingActionButton) activity.findViewById(R.id.fab_line);
        fab_line.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab_line.setLineMorphingState((fab_line.getLineMorphingState() + 1) % 2, true);


                if (Utilities.isUserAuthenticated()) {
                    Intent intent = new Intent(activity, PubblicaAnnuncioActivity.class);
                    //activity.startActivityForResult(intent, 12345);
                } else {
                    Context context = activity.getApplicationContext();
                    CharSequence text = getString(R.string.profile_title_logged_out);
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }
        });
*/
    }

    private void setFABListener(final FloatingActionButton... fab_form) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int len = fab_form.length;
                FloatingActionButton tmp = null;

                fab_menu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
                    @Override
                    public void onMenuExpanded() {
                        fab_menu.setBackgroundColor(getResources().getColor(R.color.fab_background));
                    }

                    @Override
                    public void onMenuCollapsed() {
                        fab_menu.setBackgroundColor(getResources().getColor(R.color.transparent_fab_background));
                    }
                });

                for (int i = 0; i < len; i++) {
                    tmp = fab_form[i];

                    tmp.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            fab_menu.collapse();

                            if (Utilities.isUserAuthenticated()) {
                                Intent intent;
                                if (v.getTag().toString().equals("form")) {
                                    intent = new Intent(getActivity(), AddNoticeScreenSlideActivity.class);
                                    intent.putExtra(INPUT_TYPE_MESSAGE, true);
                                } else {
                                    intent = new Intent(getActivity(), PubblicaAnnuncioActivity.class);
                                }
                                getActivity().startActivityForResult(intent, MainActivity.PUBBLICA_ANNUNCIO_CALL);
                            } else {
                                Context context = getActivity().getApplicationContext();
                                CharSequence text = getString(R.string.profile_title_logged_out);
                                int duration = Toast.LENGTH_LONG;

                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();
                            }
                        }
                    });

                    final FloatingActionButton finalTmp = tmp;
                    tmp.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            return false;
                        }
                    });
                }
            }
        });
    }

    public boolean isFABMenuOpened() {
        return fab_menu != null ? fab_menu.isExpanded() : false;
    }

    public void closeFABMenu() {
        if(fab_menu != null)
            fab_menu.collapse();
    }

    public void setNoticeListLayout(ListaAnnunci LA, final boolean cancellaAnnuncioButtonVisible) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.annuncio_swipe_refresh_layout);
                recyclerView = (RecyclerView) getActivity().findViewById(R.id.recycler_view);
                // use a linear layout manager
                mLayoutManager = new LinearLayoutManager(getActivity());
                recyclerView.setLayoutManager(mLayoutManager);

                adapter = new RVAdapter(getActivity());
                adapter.setListaAnnunci(NoticeFragment.this.LA);
                adapter.setCancellaAnnuncioButtonVisible(cancellaAnnuncioButtonVisible);
                recyclerView.setAdapter(adapter);

                wheel = (FrameLayout) getActivity().findViewById(R.id.progress_wheel_layout);

                recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);

                        if (dy < -FAB_THRESHOLD && fab_menu.getVisibility() != View.VISIBLE) {
                            fab_menu.setVisibility(View.VISIBLE);
                            fab_menu.startAnimation(AnimationUtils.loadAnimation(activity,
                                    R.anim.jump_from_down));
                        } else if (dy > FAB_THRESHOLD && fab_menu.getVisibility() != View.GONE) {
                            fab_menu.setVisibility(View.GONE);
                            fab_menu.startAnimation(AnimationUtils.loadAnimation(activity,
                                    R.anim.jump_to_down));
                        }

                        visibleItemCount = mLayoutManager.getChildCount();
                        totalItemCount = mLayoutManager.getItemCount();
                        pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();
                        Log.d(TAG, "visibleItemCount (" + visibleItemCount +
                                ") + pastVisiblesItems (" + pastVisiblesItems + ") = " +
                                (visibleItemCount + pastVisiblesItems) +
                                "\ntotalItemCount = " + totalItemCount);

                        if (dy != 0 && loading && !lastNoticeReached) {
                            if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                                loading = false;
                                Log.d(TAG, "Ultimo annuncio caricato!");
                                downloadAnnunci(false);
                            }
                        } else {
                            if ((visibleItemCount + pastVisiblesItems) == totalItemCount - 1) {
                                if (!loading)
                                    loading = true;
                            }
                        }
                    }
                });

                mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refresh();
                    }
                });

                mSwipeRefreshLayout.setColorSchemeResources(R.color.primary_dark,
                        R.color.primary,
                        R.color.accent
                );

                recyclerView.setHasFixedSize(true);
            }
        });
    }

    public void refresh() {
        Log.d(TAG, "Comincio il refresh degli annunci");
        setRefreshing(true);
        downloadAnnunci(true);
        handler.post(refreshing);
    }

    private final Runnable refreshing = new Runnable(){
        public void run(){
            try {
                if(isRefreshing()){
                    // re run the verification after 1 second
                    no_connection_message.setVisibility(View.INVISIBLE);
                    Log.d(TAG, "Refreshing degli annunci");
                    handler.postDelayed(this, 1000);
                }else{
                    // stop the animation after the data is fully loaded
                    mSwipeRefreshLayout.setRefreshing(false);
                    notifyDataSetChanged();
                    Log.d(TAG, "Refresh degli annunci terminato");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void notifyDataSetChanged() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void setRefreshing(boolean isRefreshing) {
        this.isRefreshing = isRefreshing;
    }

    private boolean isRefreshing() {
        if(isRefreshing) {
            isRefreshing = false;
            return true;
        } else
            return false;
    }

    public void setToolbar(ActionBar supportActionBar) {
        this.supportActionBar = supportActionBar;
    }

    public void downloadAnnunci(boolean isRefreshing, final ParseQuery query) {
        if(!NetworkUtilities.checkConnection(activity)) {
            no_connection_message.setVisibility(View.VISIBLE);

            mSwipeRefreshLayout.setRefreshing(false);
            mSnackBar.text(R.string.activate_connection)
                    .applyStyle(R.style.SnackBarMultiLine)
                    .actionText("HO CAPITO")
                    .actionClickListener(new SnackBar.OnActionClickListener() {
                        @Override
                        public void onActionClick(SnackBar snackBar, int i) {
                            mSnackBar.dismiss();
                        }
                    });
            ViewGroup view = (ViewGroup) activity.findViewById(android.R.id.content);
            mSnackBar.show(view);

            return;
        }

        //activity.unregisterReceiver(receiver);
        /*
        if(isRefreshing)
            setRefreshing(true);
        */

        no_connection_message.setVisibility(View.INVISIBLE);

        int cntNewNotices = 0;
        ArrayList<String> deletedNotices = null;
        ParseObject user_online = null;
        if(Utilities.isUserAuthenticated()) {
            List<ParseObject> users_online;
            try {
                users_online = ParseQuery.getQuery("Users_Online").whereEqualTo("userId", ParseUser.getCurrentUser().getObjectId().toString()).find();
                if(!users_online.isEmpty()) {
                    user_online = users_online.get(0);
                    cntNewNotices = user_online.getNumber("contatore_nuovi_annunci").intValue();
                    deletedNotices = (ArrayList<String>) user_online.get("annunci_eliminati");
                    //Nel caso in cui l'utente fa uno swipe verso il basso per aggiornare la pagina,
                    //o viene pubblicato o cancellato un nuovo annuncio,
                    //e non ci sono nè nuovi annunci nè annunci da cancellare, il metodo non fa niente
                    if(isRefreshing && cntNewNotices == 0 && (deletedNotices == null || deletedNotices.isEmpty())) {
                        Log.d(TAG, "Non c'è bisogno di aggiornare");
                        if(deletedNotices == null) {
                            Log.d(TAG, "deletedNotices = null");
                        } else {
                            Log.d(TAG, "deletedNotices è vuoto");
                        }
                        return;
                    }

                }
            } catch (ParseException e) {
                Log.d(TAG, "downloadAnnunci()/Si è verificato un'errore: " + e.getMessage());
                e.printStackTrace();
            }
        } //TODO da gestire il caso in cui l'utente non è loggato

        deleteNotices(deletedNotices, user_online);

        if(isRefreshing) {//Lo tengo separato da quello di sopra per evitare di creare un
            //nuovo oggetto nel caso in cui non ci sia bisogno di aggiornare la pagina
            query.setLimit(cntNewNotices);
            lastNoticesCnt += cntNewNotices;
            final ParseObject finalUser_online = user_online;
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> scoreList, ParseException e) {
                    if (e == null) {
                        if (scoreList.size() > 0) {
                            LA.addAnnunci(scoreList, true);
                            finalUser_online.put("contatore_nuovi_annunci", 0);
                            finalUser_online.saveEventually();
                            queryCnt += scoreList.size();
                            setRefreshing(false);
                        }

                        Log.d(TAG, "Trovati " + scoreList.size() + " libri" +
                                "\nlastNoticesCnt = " + lastNoticesCnt +
                                "\nqueryCnt = " + queryCnt);
                    } else {
                        Log.d(TAG, "Errore: " + e.getMessage());
                    }
                }
            });
        } else {
            query.setLimit(QUERY_LIMIT);
            query.setSkip(LA.size());

            lastNoticesCnt = cntNewNotices;

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utilities.slideUP(activity, wheel);
                }
            });

            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> scoreList, ParseException e) {
                    if (e == null) {
                        if (scoreList.size() == 0) {
                            lastNoticeReached = true;
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utilities.slideDown(activity, wheel);
                                }
                            });
                            return;
                        }

                        if (scoreList.size() > 0) {
                            LA.addAnnunci(scoreList, false);
                            notifyDataSetChanged();
                            queryCnt += scoreList.size();
                        }

                        Log.d(TAG, "Trovati " + scoreList.size() + " libri" +
                                "\nlastNoticesCnt = " + lastNoticesCnt +
                                "\nqueryCnt = " + queryCnt);
                    } else {
                        Log.d(TAG, "Errore: " + e.getMessage());
                    }

                    Utilities.slideDown(activity, wheel);
                }
            });
        }
    }

    private void deleteNotices(final ArrayList<String> deletedNotices, final ParseObject user_online) {
        if(deletedNotices != null && deletedNotices.size() > 0) {//sono stati eliminati degli annunci
            Log.d(TAG, "Comincio l'eliminazione di " + deletedNotices.size() + " annunci");
            int cntDeletedNotices = LA.deleteNoticesById(deletedNotices);
            notifyDataSetChanged();
            Log.d(TAG, "Eliminazione degli annunci terminata");
            //TODO da verificare che funzioni correttamente
            user_online.remove("annunci_eliminati");
            user_online.saveEventually();
            queryCnt -= -cntDeletedNotices;
        } else {
            Log.d(TAG, "Non ci sono annunci da eliminare");
            if(deletedNotices == null)
                Log.d(TAG, "deletedNotices è null");
        }
    }
}
