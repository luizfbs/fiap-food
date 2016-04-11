package br.com.fiap.fiapfood;

import android.support.multidex.MultiDexApplication;
import com.activeandroid.ActiveAndroid;

public class Application extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        ActiveAndroid.initialize(this);
    }

}
