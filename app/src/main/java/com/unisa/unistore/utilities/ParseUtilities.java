package com.unisa.unistore.utilities;

import android.content.Context;
import android.util.Log;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import bolts.Task;

/**
 * Created by Daniele on 17/05/2015.
 */
public class ParseUtilities {
    private static final String TAG = "ParseUtilities";

    public static void sendNotificationToNoticeAuthor(ParseUser destinatario, String messaggio) {
        ParseQuery query = ParseInstallation.getQuery();
        query.whereEqualTo("user", destinatario.getObjectId());

        ParsePush push = new ParsePush();
        push.setQuery(query);
        push.setMessage(messaggio);
        push.sendInBackground();
    }

    public boolean salvaSulCloud(String nomeOggetto, String id, Map<String, Object> key_value) {
        String key = "";
        Object value = new Object();

        ParseObject parseObject = new ParseObject(nomeOggetto);

        for(Iterator<String> i = key_value.keySet().iterator(); i.hasNext(); ) {
            key = i.next();
            value = new Object();
            if(key_value.get(key) != null) {
                value = key_value.get(key);
                parseObject.put(key, value);
            }
        }

        Task<Void> task = parseObject.saveInBackground();

        if(task.isCompleted())
            return true;
        else {
            parseObject.put("score", 1337);
            parseObject.put("playerName", "Sean Plott");
            parseObject.put("cheatMode", false);
            parseObject.setObjectId("prova");
            parseObject.saveInBackground();

            return false;
        }
    }

    public ArrayList<Object> caricaDalCloud(String id){
        ArrayList<Object> listaDati = new ArrayList<>();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("GameScore");
        query.getInBackground(id, new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    object.get("");
                } else {
                    // something went wrong
                }
            }
        });

        return listaDati;
    }

    public static void connectUser(ParseUser currentUser) {
        if(Utilities.isUserAuthenticated()) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Users_Online");
            try {
                List<ParseObject> users_online = query.whereEqualTo("userId", currentUser.getObjectId().toString()).find();
                ParseObject usersOnlineObject = null;
                if(!users_online.isEmpty()) {
                    usersOnlineObject = users_online.get(0);
                    usersOnlineObject.put("online", true);
                    usersOnlineObject.put("contatore_nuovi_annunci", 0);
                    usersOnlineObject.remove("annunci_eliminati");
                } else {
                    usersOnlineObject = new ParseObject("Users_Online");
                    usersOnlineObject.put("userId", currentUser.getObjectId().toString());
                    usersOnlineObject.put("online", true);
                    usersOnlineObject.put("contatore_nuovi_annunci", 0);
                    usersOnlineObject.remove("annunci_eliminati");
                }
                usersOnlineObject.saveEventually(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e == null) {
                            Log.d(TAG, "connectUser/Salvattaggio effettuato con successo");

                        } else {
                            Log.d(TAG, "connectUser/Si è verificato un'errore: " + e.getMessage());
                        }
                    }
                });
            } catch (ParseException e) {
                Log.d(TAG, "onCreate/Si è verificato un'errore: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void disconnectUser(Context context) {
        if(Utilities.isUserAuthenticated()) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Users_Online");
            try {
                List<ParseObject> users_online = query.whereEqualTo("userId", ParseUser.getCurrentUser().getObjectId().toString()).find();
                ParseObject usersOnlineObject = users_online.get(0);
                usersOnlineObject.put("online", false);
                usersOnlineObject.put("contatore_nuovi_annunci", 0);

                usersOnlineObject.save();
            } catch (ParseException e) {
                Log.d(TAG, "disconnectUser/Si è verificato un'errore: " + e.getMessage());
                if(NetworkUtilities.checkConnection(context)) {
                    Log.d(TAG, "La connessione funziona");
                } else {
                    Log.d(TAG, "La connessione NON funziona");
                }
                e.printStackTrace();
            }
        }
    }
}
