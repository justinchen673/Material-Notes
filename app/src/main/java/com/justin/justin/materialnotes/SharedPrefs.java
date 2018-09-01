package com.justin.justin.materialnotes;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

public class SharedPrefs {
    public class SharedKeys {
        final static String notes = "notes";
        final static String titles = "titles";
    }
    public void storeSet(Context context, String key, Set<String> data) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(key, data);
        editor.commit();
    }

    public Set<String> getSet(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getStringSet(key, new HashSet<String>());
    }
}
