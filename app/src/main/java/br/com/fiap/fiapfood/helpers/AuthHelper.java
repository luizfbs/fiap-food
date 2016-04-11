package br.com.fiap.fiapfood.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.facebook.login.LoginManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.com.fiap.fiapfood.models.User;

public class AuthHelper {

    public static final String AUTH_PREFERENCE_KEY = "br.com.fiap.fiapfood.authdata";
    public static final String AUTH_PREFERENCE_USERID_KEY = "active-user";

    public static final String AUTH_PREFERENCE_KEEPCONNECTED_KEY = "auth-keep-connected";
    public static final String AUTH_PREFERENCE_ISSYNCHRONIZED_KEY = "auth-is-synchronized";

    public static boolean isAuthenticated(Context context){
        return getUser(context) != null;
    }

    public static User getUser(Context context){
        SharedPreferences pref = context.getSharedPreferences(AUTH_PREFERENCE_KEY, context.MODE_PRIVATE);
        long userId = pref.getLong(AUTH_PREFERENCE_USERID_KEY, -1);
        return User.get(userId);
    }

    public static boolean keepConnected(Context context){
        SharedPreferences pref = context.getSharedPreferences(AUTH_PREFERENCE_KEY, context.MODE_PRIVATE);
        return pref.getBoolean(AUTH_PREFERENCE_KEEPCONNECTED_KEY, false);
    }

    public static boolean isSynchronized(Context context){
        SharedPreferences pref = context.getSharedPreferences(AUTH_PREFERENCE_KEY, context.MODE_PRIVATE);
        return pref.getBoolean(AUTH_PREFERENCE_ISSYNCHRONIZED_KEY, false);
    }

    public static void SetSynchronized(Context context){
        SharedPreferences pref = context.getSharedPreferences(AUTH_PREFERENCE_KEY, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putBoolean(AUTH_PREFERENCE_ISSYNCHRONIZED_KEY, true);
        editor.commit();
    }

    public static void createSession(Context context, User user, boolean keepConnected){
        SharedPreferences pref = context.getSharedPreferences(AUTH_PREFERENCE_KEY, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putLong(AUTH_PREFERENCE_USERID_KEY, user.getId());
        editor.putBoolean(AUTH_PREFERENCE_KEEPCONNECTED_KEY, keepConnected);

        editor.commit();
    }

    public static void clearSession(Context context){
        LoginManager.getInstance().logOut();

        SharedPreferences pref = context.getSharedPreferences(AUTH_PREFERENCE_KEY, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.clear();
        editor.commit();
    }

}
