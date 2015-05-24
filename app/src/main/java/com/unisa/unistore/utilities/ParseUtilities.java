package com.unisa.unistore.utilities;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import bolts.Task;

/**
 * Created by Daniele on 17/05/2015.
 */
public class ParseUtilities {

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
}
